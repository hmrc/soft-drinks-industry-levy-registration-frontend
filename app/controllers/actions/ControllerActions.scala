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

package controllers.actions

import com.google.inject.{Inject, Singleton}
import connectors.SoftDrinksIndustryLevyConnector
import controllers.routes
import handlers.ErrorHandler
import models.RegisterState._
import models.requests.{DataRequest, DataRequestForApplicationSubmitted, DataRequestForEnterBusinessDetails, OptionalDataRequest}
import models.{RegisterState, RosmWithUtr, UserAnswers}
import pages.EnterBusinessDetailsPage
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc.{ActionBuilder, ActionRefiner, AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utilities.GenericLogger

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ControllerActions @Inject()(identify: IdentifierAction,
                                  getData: DataRetrievalAction,
                                  dataRequired: DataRequiredAction,
                                  sdilConnector: SoftDrinksIndustryLevyConnector,
                                  genericLogger: GenericLogger,
                                  errorHandler: ErrorHandler)(implicit ec: ExecutionContext) {

  def withUserWhoCanRegister: ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen dataRequired
  }

  def withUserWhoHasSubmittedRegistrationAction: ActionBuilder[DataRequestForApplicationSubmitted, AnyContent] = {
    identify andThen getData andThen dataRequiredForUserWhoHasSubmittedApplicationAction
  }

  def withAlreadyRegisteredAction: ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen dataRequiredForUserWhoCannotRegisterAction(AlreadyRegistered)
  }

  def withPendingRegistrationAction: ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen dataRequiredForUserWhoCannotRegisterAction(RegistrationPending)
  }

  def withRegisterApplicationAcceptedAction: ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen dataRequiredForUserWhoCannotRegisterAction(RegisterApplicationAccepted)
  }

  def withRequiresBusinessDetailsAction: ActionBuilder[DataRequestForEnterBusinessDetails, AnyContent] = {
    identify andThen getData andThen dataRequiredForRequiresBusinessDetailsAction
  }

  private def dataRequiredForRequiresBusinessDetailsAction: ActionRefiner[OptionalDataRequest, DataRequestForEnterBusinessDetails] = {
    new ActionRefiner[OptionalDataRequest, DataRequestForEnterBusinessDetails] {
      override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequestForEnterBusinessDetails[A]]] = {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        request.userAnswers match {
          case Some(userAnswers) if userAnswers.submittedOn.isDefined => Future.successful(Left(Redirect(routes.RegistrationConfirmationController.onPageLoad)))
          case Some(userAnswers) if canAccessEnterBusinessDetails(userAnswers) =>
            Future.successful(Right(DataRequestForEnterBusinessDetails(request = request.request, internalId = request.internalId,
              hasCTEnrolment = request.hasCTEnrolment, authUtr = request.authUtr, userAnswers = userAnswers)))
          case Some(userAnswers) =>
            val call = ActionHelpers.getRouteForRegisterState(userAnswers.registerState)
            Future.successful(Left(Redirect(call)))
          case _ =>
            genericLogger.logger.info(s"User has no user answers ${hc.requestId}")
            Future.successful(Left(Redirect(routes.RegistrationController.start)))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }
  }

  private def dataRequiredForUserWhoCannotRegisterAction(registerState: RegisterState): ActionRefiner[OptionalDataRequest, DataRequest] = {
    new ActionRefiner[OptionalDataRequest, DataRequest] {
      override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        request.userAnswers match {
          case Some(userAnswers) if userAnswers.submittedOn.isDefined => Future.successful(Left(Redirect(routes.RegistrationConfirmationController.onPageLoad)))
          case Some(userAnswers) if userAnswers.registerState.toString == registerState.toString =>
            getRosmData(userAnswers, request).map{
              case Right(rosmWithUtr) => Right(DataRequest(request, request.internalId, request.hasCTEnrolment, request.authUtr, userAnswers, rosmWithUtr))
              case Left(result) => Left(result)
            }
          case Some(userAnswers) =>
            val call = ActionHelpers.getRouteForRegisterState(userAnswers.registerState)
            Future.successful(Left(Redirect(call)))
          case _ =>
            genericLogger.logger.info(s"User has no user answers ${hc.requestId}")
            Future.successful(Left(Redirect(routes.RegistrationController.start)))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }
  }

  private def dataRequiredForUserWhoHasSubmittedApplicationAction: ActionRefiner[OptionalDataRequest, DataRequestForApplicationSubmitted] = {
    new ActionRefiner[OptionalDataRequest, DataRequestForApplicationSubmitted] {
      override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequestForApplicationSubmitted[A]]] = {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        request.userAnswers match {
          case Some(userAnswers) if userAnswers.submittedOn.isDefined =>
            getRosmData(userAnswers, request).map {
              case Right(rosmWithUtr) => Right(DataRequestForApplicationSubmitted(request, request.internalId, userAnswers, rosmWithUtr, userAnswers.submittedOn.get))
              case Left(result) => Left(result)
            }
          case Some(userAnswers) if RegisterState.canRegister(userAnswers.registerState) =>
            Future.successful(Left(Redirect(routes.CheckYourAnswersController.onPageLoad)))
          case Some(userAnswers) => val call = ActionHelpers.getRouteForRegisterState(userAnswers.registerState)
            Future.successful(Left(Redirect(call)))
          case _ => genericLogger.logger.info(s"User has no user answers ${hc.requestId}")
            Future.successful(Left(Redirect(routes.RegistrationController.start)))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }
  }

  private def getUtr[A](userAnswers: UserAnswers, request: OptionalDataRequest[A]): Option[String] = {
      userAnswers.get(EnterBusinessDetailsPage).map(_.utr).orElse(request.authUtr)
  }

  private def getRosmData[A](userAnswers: UserAnswers, request: OptionalDataRequest[A])
                            (implicit hc: HeaderCarrier): Future[Either[Result, RosmWithUtr]] = {
    getUtr(userAnswers, request) match {
      case Some(utr) =>
        sdilConnector.retreiveRosmSubscription(utr, request.internalId).value.map {
          case Right(rosmWithUtr) => Right(rosmWithUtr)
          case Left(_) => Left(InternalServerError(errorHandler.internalServerErrorTemplate(request)))
        }
      case None =>
        genericLogger.logger.error(s"User has no utr when required for register state ${userAnswers.registerState}")
        Future.successful(Left(InternalServerError(errorHandler.internalServerErrorTemplate(request))))
    }
  }

}
