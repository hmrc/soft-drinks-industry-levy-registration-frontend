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
import com.google.inject.{Inject, Singleton}
import connectors._
import errors._
import models.RegisterState._
import models.backend.{Subscription, UkAddress}
import models.requests.{DataRequest, IdentifierRequest}
import models.{Identify, RegisterState, RosmWithUtr, UserAnswers}
import play.api.mvc.AnyContent
import service.RegistrationResult
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier
import utilities.GenericLogger

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class RegistrationOrchestrator @Inject()(sdilConnector: SoftDrinksIndustryLevyConnector,
                                         sessionService: SessionService,
                                         genericLogger: GenericLogger) {

  def handleRegistrationRequest(implicit request: IdentifierRequest[AnyContent],
                                hc: HeaderCarrier,
                                ec: ExecutionContext): RegistrationResult[RegisterState] = {
    val internalId = request.internalId
    val registerState = request.optUTR match {
      case Some(utr) if request.isRegistered => determineRegisterStateForRegisteredUsers(utr, internalId).value
      case Some(utr) => determineRegisterStateForNoneRegisteredUsers(utr, internalId).value
      case _ => Future(Right(RequiresBusinessDetails))
    }

    for {
      regState <- EitherT(registerState)
      _ <- sessionService.set(UserAnswers(internalId, regState))
    } yield regState

  }
  def checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers(identify: Identify, internalId: String)
                                                             (implicit hc: HeaderCarrier,
                                                              ec: ExecutionContext): RegistrationResult[RegisterState] = {

    for {
      rosmData <- sdilConnector.retreiveRosmSubscription(identify.utr, internalId)
      _ <- postcodesMatch(rosmData.rosmRegistration.address, identify)
      subscriptionStatus <- sdilConnector.checkPendingQueue(identify.utr)
    } yield getRegistrationStateFromSubscriptionStatus(subscriptionStatus, true)

  }

  private def determineRegisterStateForNoneRegisteredUsers(utr: String, internalId: String)
                                                      (implicit hc: HeaderCarrier,
                                                       ec: ExecutionContext): RegistrationResult[RegisterState] = EitherT {
    val subStatus = for {
      _ <- sdilConnector.retreiveRosmSubscription(utr, internalId)
      subscriptionStatus <- sdilConnector.checkPendingQueue(utr)
    } yield getRegistrationStateFromSubscriptionStatus(subscriptionStatus)

    subStatus.value.map{
      case Right(status) => Right(status)
      case Left(NoROSMRegistration) => Right(RequiresBusinessDetails)
      case Left(error) => Left(error)
    }
  }

  private def determineRegisterStateForRegisteredUsers(utr: String, internalId: String)
                                                            (implicit hc: HeaderCarrier,
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

    for {
      subscription <- EitherT(Future(createSubscriptionFromUserAnswers(updatedUserAnswers, request.rosmWithUtr)))
      created <- sdilConnector.createSubscription(subscription, request.rosmWithUtr.rosmRegistration.safeId)
      _ <- sessionService.set(updatedUserAnswers)
    } yield created
  }

  private def createSubscriptionFromUserAnswers(userAnswers: UserAnswers, rosmWithUtr: RosmWithUtr): Either[RegistrationErrors, Subscription] = {
   Try {
     Subscription.generate(userAnswers, rosmWithUtr)
   } match {
     case Success(sub) =>
       Right(sub)
     case Failure(ex) =>
       genericLogger.logger.error(s"unable to create subscription as ${ex.getMessage}")
     Left(MissingRequiredUserAnswers)
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
