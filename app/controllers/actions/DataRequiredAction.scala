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
import models.UserAnswers
import models.requests.{DataRequest, OptionalDataRequest}
import pages.IdentifyPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionImpl @Inject()(sdilConnector: SoftDrinksIndustryLevyConnector)
                                      (implicit val executionContext: ExecutionContext) extends DataRequiredAction  {

  private def findRosm[A](utr: String, request: OptionalDataRequest[A], data: UserAnswers): Future[Either[Result, DataRequest[A]]] ={
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    sdilConnector.retreiveRosmSubscription(utr, request.internalId).map(result => result match {
      case Some(result) =>
        Right(DataRequest(request, request.internalId, request.hasCTEnrolment, request.authUtr, data, result))
      case None =>
        Left(Redirect(routes.IndexController.onPageLoad()))
    })
  }

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    request.userAnswers match {
              case None =>
                Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))
              case Some(data) =>
                val manualUtr = data.get(IdentifyPage).map(answers => answers.utr)
                (manualUtr, request.authUtr) match {
                  case (None, Some(utr)) => findRosm(utr,  request, data)
                  case (Some(utr),  _) => findRosm(utr,  request, data)
                  case (None, None) =>
                    Future.successful(Left(Redirect(routes.IndexController.onPageLoad())))
      }
    }
  }
}

trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
