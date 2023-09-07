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

import connectors.SoftDrinksIndustryLevyConnector
import controllers.routes
import handlers.ErrorHandler
import models.RegisterState.RegisterWithOtherUTR
import models.requests.{DataRequest, OptionalDataRequest}
import models.{RegisterState, UserAnswers}
import pages.EnterBusinessDetailsPage
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utilities.GenericLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionImpl @Inject()(sdilConnector: SoftDrinksIndustryLevyConnector,
                                       genericLogger: GenericLogger,
                                       errorHandler: ErrorHandler)
                                      (implicit val executionContext: ExecutionContext) extends DataRequiredAction  {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    request.userAnswers match {
      case Some(useranswers) if useranswers.submittedOn.isDefined =>
        Future.successful(Left(Redirect(routes.RegistrationConfirmationController.onPageLoad)))
      case Some(useranswers) if RegisterState.canRegister(useranswers.registerState)=>
        getUtrFromUserAnswers(useranswers, request) match {
          case Some(utr) =>
            sdilConnector.retreiveRosmSubscription(utr, request.internalId).value.map{
              case Right(rosmWithUtr) => Right(DataRequest(request, request.internalId, request.hasCTEnrolment, request.authUtr, useranswers, rosmWithUtr))
              case Left(_) => Left(InternalServerError(errorHandler.internalServerErrorTemplate(request)))
            }
          case None =>
            genericLogger.logger.error(s"User has no utr when required for register state ${useranswers.registerState}")
            Future.successful(Left(InternalServerError(errorHandler.internalServerErrorTemplate(request))))
        }
      case Some(useranswers) =>
        val call = ActionHelpers.getRouteForRegisterState(useranswers.registerState)
        Future.successful(Left(Redirect(call)))
      case _ =>
        genericLogger.logger.info(s"User has no user answers ${hc.requestId}")
        Future.successful(Left(Redirect(routes.RegistrationController.start)))
    }
  }

  private def getUtrFromUserAnswers[A](userAnswers: UserAnswers, request: OptionalDataRequest[A]): Option[String] = {
    if (userAnswers.registerState == RegisterWithOtherUTR) {
      userAnswers.get(EnterBusinessDetailsPage).map(_.utr)
    } else {
      request.authUtr
    }
  }
}

trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
