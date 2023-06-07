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

import base.SpecBase
import connectors.SoftDrinksIndustryLevyConnector
import models.requests.{DataRequest, OptionalDataRequest}
import models.{LitresInBands, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.HowManyContractPackingPage
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRequiredActionSpec extends SpecBase with MockitoSugar {

  class Harness(connector: SoftDrinksIndustryLevyConnector) extends DataRequiredActionImpl(connector) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "Data Required Action" - {

    "when there is no data in the cache" - {

      "must set Rosm to 'None' in the request with no user answers" in {

        implicit val hc: HeaderCarrier = HeaderCarrier()

        val connecor = mock[SoftDrinksIndustryLevyConnector]
        when(connecor.retreiveRosmSubscription(sdilNumber, "internalId")) thenReturn Future.successful(None)
        val action = new Harness(connecor)

        val result = action.callRefine(OptionalDataRequest(FakeRequest(), "internalId", userAnswers = None)).futureValue

        result.map(_.rosmRegistration).isLeft mustBe true
      }
    }

    "when there is no data in the cache" - {

      "must set Rosm to 'None' in the request with no user answers but no matching utr for the rosm" in {

        implicit val hc: HeaderCarrier = HeaderCarrier()

        val connecor = mock[SoftDrinksIndustryLevyConnector]
        when(connecor.retreiveRosmSubscription(sdilNumber, "internalId")) thenReturn Future.successful(None)
        val action = new Harness(connecor)
        val userAnswers = UserAnswers(identifier).set(HowManyContractPackingPage, LitresInBands(100, 200)).success.value
        val result = action.callRefine(OptionalDataRequest(FakeRequest(), "internalId", userAnswers = Some(userAnswers))).futureValue

        result.map(_.rosmRegistration).isLeft mustBe true
      }
    }

    "when there is no data in the cache" - {

      "must set Rosm in the request when there is user answers and a  matching utr for the rosm" in {

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val userAnswers = UserAnswers(identifier).set(HowManyContractPackingPage, LitresInBands(100, 200)).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers), rosmRegistration = rosmRegistration).build()



        running(application) {
          val connecor = mock[SoftDrinksIndustryLevyConnector]

          when(connecor.retreiveRosmSubscription(sdilNumber, "internalId")) thenReturn Future.successful(Some(rosmRegistration))

          val action = new Harness(connecor)

          val result = action.callRefine(OptionalDataRequest(FakeRequest(), "internalId", userAnswers = Some(userAnswers))).futureValue
          result.map(_.rosmRegistration).isRight mustBe false
        }
      }
    }

  }
}
