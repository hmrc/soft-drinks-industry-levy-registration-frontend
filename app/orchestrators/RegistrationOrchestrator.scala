/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package orchestrators

import cats.data.EitherT
import cats.implicits._
import com.google.inject.{ Inject, Singleton }
import connectors._
import errors._
import models.RegisterState._
import models._
import models.backend.{ Subscription, UkAddress }
import models.requests.{ DataRequest, IdentifierRequest }
import pages.HowManyLitresGloballyPage
import play.api.mvc.AnyContent
import repositories.{ SDILSessionCache, SDILSessionKeys }
import service.RegistrationResult
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier
import utilities.GenericLogger

import java.time.Instant
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

@Singleton
class RegistrationOrchestrator @Inject() (
  sdilConnector: SoftDrinksIndustryLevyConnector,
  sessionService: SessionService,
  sdilSessionCache: SDILSessionCache,
  genericLogger: GenericLogger) {

  def handleRegistrationRequest(implicit
    request: IdentifierRequest[AnyContent],
    hc: HeaderCarrier,
    ec: ExecutionContext): RegistrationResult[UserAnswers] = {
    val internalId = request.internalId
    def registerState(optUserAnswers: Option[UserAnswers]): Future[Either[RegistrationErrors, RegisterState]] = {
      optUserAnswers match {
        case Some(userAnswers) if userAnswers.submittedOn.isDefined => Future.successful(Left(RegistrationAlreadySubmitted))
        case Some(userAnswers) => Future.successful(Right(userAnswers.registerState))
        case None => request.optUTR match {
          case Some(utr) if request.isRegistered => determineRegisterStateForRegisteredUsers(utr, internalId).value
          case Some(utr) => determineRegisterStateForNoneRegisteredUsers(utr, internalId).value
          case _ => Future(Right(RequiresBusinessDetails))
        }
      }
    }

    for {
      optUserAnswers <- sessionService.get(internalId)
      regState <- EitherT(registerState(optUserAnswers))
      ua <- EitherT.right[RegistrationErrors](Future.successful(
        optUserAnswers.fold[UserAnswers](UserAnswers(internalId, regState))(ua => ua.copy(registerState = regState))
      ))
      _ <- sessionService.set(ua)
    } yield ua

  }
  def checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers(identify: Identify, internalId: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext): RegistrationResult[RegisterState] = {

    for {
      rosmData <- sdilConnector.retreiveRosmSubscription(identify.utr, internalId)
      _ <- postcodesMatch(rosmData.rosmRegistration.address, identify)
      subscriptionStatus <- sdilConnector.checkPendingQueue(identify.utr)
    } yield getRegistrationStateFromSubscriptionStatus(subscriptionStatus, true)

  }

  private def determineRegisterStateForNoneRegisteredUsers(utr: String, internalId: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext): RegistrationResult[RegisterState] = EitherT {
    val subStatus = for {
      _ <- sdilConnector.retreiveRosmSubscription(utr, internalId)
      subscriptionStatus <- sdilConnector.checkPendingQueue(utr)
    } yield getRegistrationStateFromSubscriptionStatus(subscriptionStatus)

    subStatus.value.map {
      case Right(status) => Right(status)
      case Left(NoROSMRegistration) => Right(RequiresBusinessDetails)
      case Left(error) => Left(error)
    }
  }

  private def determineRegisterStateForRegisteredUsers(utr: String, internalId: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext): RegistrationResult[RegisterState] = EitherT {
    sdilConnector.retreiveRosmSubscription(utr, internalId).value.map {
      case Right(_) => Right(AlreadyRegistered)
      case Left(NoROSMRegistration) => Left(AuthenticationError)
      case Left(error) => Left(error)
    }
  }

  def createSubscriptionAndUpdateUserAnswers(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): RegistrationResult[Unit] = {
    val submittedDateTime = Instant.now
    val updatedUserAnswers = request.userAnswers.copy(submittedOn = Some(submittedDateTime))
    val internalId = request.internalId

    for {
      createdSubscriptionAndAmountProducedGlobally <- EitherT(Future(getSubscriptionAndHowManyLitresGlobally(updatedUserAnswers, request.rosmWithUtr)))
      created <- sdilConnector.createSubscription(createdSubscriptionAndAmountProducedGlobally.subscription, request.rosmWithUtr.rosmRegistration.safeId)
      _ <- sessionService.set(updatedUserAnswers)
      _ <- EitherT.right(sdilSessionCache.save[CreatedSubscriptionAndAmountProducedGlobally](internalId, SDILSessionKeys.CREATED_SUBSCRIPTION_AND_AMOUNT_PRODUCED_GLOBALLY, createdSubscriptionAndAmountProducedGlobally))
    } yield created
  }

  def getSubscriptionAndHowManyLitresGlobally(
    userAnswers: UserAnswers,
    rosmWithUtr: RosmWithUtr): Either[RegistrationErrors, CreatedSubscriptionAndAmountProducedGlobally] = {
    userAnswers.get(HowManyLitresGloballyPage)
      .fold[Either[RegistrationErrors, CreatedSubscriptionAndAmountProducedGlobally]](
        Left(MissingRequiredUserAnswers)) {
          howManyProducedGlobally =>
            Try {
              Subscription.generate(userAnswers, rosmWithUtr)
            } match {
              case Success(sub) => Right(CreatedSubscriptionAndAmountProducedGlobally(sub, howManyProducedGlobally))
              case Failure(ex) =>
                genericLogger.logger.error(s"unable to create subscription as ${ex.getMessage}")
                Left(MissingRequiredUserAnswers)
            }
        }
  }

  private def postcodesMatch(rosmAddress: UkAddress, identify: Identify): RegistrationResult[Boolean] = EitherT {
    val rosmPostCode = removeWhitespace(rosmAddress.postCode)
    val enteredPostcode = removeWhitespace(identify.postcode)
    if (rosmPostCode.equalsIgnoreCase(enteredPostcode)) {
      Future.successful(Right(true))
    } else {
      Future.successful(Left(EnteredBusinessDetailsDoNotMatch))
    }
  }

  def getRegistrationStateFromSubscriptionStatus(subStatus: SubscriptionStatus, utrEntered: Boolean = false): RegisterState = {
    subStatus match {
      case Pending => RegistrationPending
      case Registered => RegisterApplicationAccepted
      case DoesNotExist if utrEntered => RegisterWithOtherUTR
      case _ => RegisterWithAuthUTR
    }
  }

  private def removeWhitespace(value: String): String = value.replaceAll(" ", "")
}
