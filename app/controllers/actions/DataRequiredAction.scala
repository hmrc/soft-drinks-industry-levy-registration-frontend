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

import connectors.{DoesNotExist, Pending, Registered, SoftDrinksIndustryLevyConnector}
import controllers.routes
import models.requests.{DataRequest, OptionalDataRequest}
import models.{NormalMode, UserAnswers}
import pages.EnterBusinessDetailsPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utilities.GenericLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionImpl @Inject()(sdilConnector: SoftDrinksIndustryLevyConnector, genericLogger: GenericLogger)
                                      (implicit val executionContext: ExecutionContext) extends DataRequiredAction  {

  private def findRosm[A](utr: String, request: OptionalDataRequest[A], data: UserAnswers)(implicit hc: HeaderCarrier): Future[Either[Result, DataRequest[A]]] ={

    sdilConnector.retreiveRosmSubscription(utr, request.internalId).map{
      case Some(result) =>
        Right(DataRequest(request, request.internalId, request.hasCTEnrolment, request.authUtr, data, result))
      case None =>
        println("NO ROSM")
        genericLogger.logger.warn(s"User has no rosm data for UTR on auth or from Identify ${hc.requestId}")
        Left(Redirect(routes.IndexController.onPageLoad()))
    }
  }

  def checkPendingSubscriptions(utr: String)(implicit hc: HeaderCarrier): Future[Either[Result, Unit]] = {
    sdilConnector.checkPendingQueue(utr).map {
      case Registered =>
        genericLogger.logger.info(s"User already registered on pending queue ${hc.requestId}")
        Left(Redirect(routes.IndexController.onPageLoad()))
      case Pending =>
        genericLogger.logger.info(s"User already pending subscription on pending queue ${hc.requestId}")
        Left(Redirect(routes.RegistrationPendingController.onPageLoad.url))
      case DoesNotExist => Right((): Unit)
    }
  }

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    def checkPendingAndCallRosm(utr: String, data: UserAnswers): Future[Either[Result, DataRequest[A]]] = {
      checkPendingSubscriptions(utr).flatMap {
        case Left(result) => Future.successful(Left(result))
        case Right(_) => findRosm(utr, request, data)
      }
    }

    request.userAnswers match {
              case None =>
                genericLogger.logger.info(s"User has no user answers ${hc.requestId}")
                Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))
              case Some(data) =>
                val manualUtr = data.get(EnterBusinessDetailsPage).map(answers => answers.utr)
                (manualUtr, request.authUtr) match {
                  case (None, Some(utr)) => checkPendingAndCallRosm(utr, data)
                  case (Some(utr),  _) => checkPendingAndCallRosm(utr, data)
                  case (None, None) =>
                    genericLogger.logger.info(s"User has no utr in auth or from Identify ${hc.requestId}")
                    Future.successful(Left(Redirect(routes.EnterBusinessDetailsController.onPageLoad(NormalMode))))
      }
    }
  }
}

trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
