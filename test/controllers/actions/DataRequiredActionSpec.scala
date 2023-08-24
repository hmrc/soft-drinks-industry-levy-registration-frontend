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
import connectors.{DoesNotExist, Pending, Registered, SoftDrinksIndustryLevyConnector}
import models.requests.{DataRequest, OptionalDataRequest}
import models.{Identify, NormalMode}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.EnterBusinessDetailsPage
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import utilities.GenericLogger

import scala.concurrent.Future

class DataRequiredActionSpec extends SpecBase with MockitoSugar {

  class Harness(connector: SoftDrinksIndustryLevyConnector) extends DataRequiredActionImpl(connector, application.injector.instanceOf[GenericLogger]) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }
  val connector = mock[SoftDrinksIndustryLevyConnector]
  val request = FakeRequest()

  "Data Required Action" - {

    "when there is no user answers, user is redirected away" in {

      val action = new Harness(connector)
      val result = action.callRefine(OptionalDataRequest(request, "internalId", userAnswers = None)).futureValue
      result.left.toOption.get mustBe Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
    "when there are user answers but user has not answered Identify Page, and has a utr in auth" - {
      s"should redirect away when pending queue returns $Registered" in {
        val internalId = "foo"
        val utr = "bar"

        when(connector.checkPendingQueue(ArgumentMatchers.eq(utr))(ArgumentMatchers.any())) thenReturn Future.successful(Registered)
        val action = new Harness(connector)
        val result = action.callRefine(OptionalDataRequest(request, internalId, authUtr = Some(utr), userAnswers = Some(emptyUserAnswers))).futureValue

        result.left.toOption.get mustBe Redirect(controllers.routes.ApplicationAlreadySubmittedController.onPageLoad)
      }
      s"should redirect away when pending queue returns $Pending" in {
        val internalId = "foo"
        val utr = "bar"

        when(connector.checkPendingQueue(ArgumentMatchers.eq(utr))(ArgumentMatchers.any())) thenReturn Future.successful(Pending)
        val action = new Harness(connector)
        val result = action.callRefine(OptionalDataRequest(request, internalId, authUtr = Some(utr), userAnswers = Some(emptyUserAnswers))).futureValue

        result.left.toOption.get mustBe Redirect(controllers.routes.RegistrationPendingController.onPageLoad)
      }
      s"should call rosm when pending queue returns $DoesNotExist but redirect away when rosm returns None" in {
        val internalId = "id"
        val utr = "bar"

        when(connector.checkPendingQueue(ArgumentMatchers.eq(utr))(ArgumentMatchers.any())) thenReturn Future.successful(DoesNotExist)
        when(connector.retreiveRosmSubscription(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(internalId))(ArgumentMatchers.any())) thenReturn Future.successful(None)
        val action = new Harness(connector)
        val result = action.callRefine(OptionalDataRequest(request, internalId, authUtr = Some(utr), userAnswers = Some(emptyUserAnswers))).futureValue

        result.left.toOption.get mustBe Redirect(controllers.routes.IndexController.onPageLoad)
      }
      s"should call rosm when pending queue returns $DoesNotExist and return success with rosm subscription" in {
        val internalId = "id"
        val utr = "bar"

        when(connector.checkPendingQueue(ArgumentMatchers.eq(utr))(ArgumentMatchers.any())) thenReturn Future.successful(DoesNotExist)
        when(connector.retreiveRosmSubscription(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(internalId))(ArgumentMatchers.any())) thenReturn Future.successful(Some(rosmRegistration))
        val action = new Harness(connector)
        val result = action.callRefine(OptionalDataRequest(request, internalId, authUtr = Some(utr), userAnswers = Some(emptyUserAnswers))).futureValue

        val rightResult = result.toOption.get
        rightResult.rosmWithUtr mustBe rosmRegistration
        rightResult.userAnswers mustBe emptyUserAnswers
        rightResult.authUtr mustBe Some(utr)
      }
    }
    s"when there are user answers but user has answered the $EnterBusinessDetailsPage and nothing exists in auth for utr" - {
      val utr = "foobar"
      val userAnswersWithEnterBusinessDetailsPage = emptyUserAnswers.set(EnterBusinessDetailsPage, Identify(utr, "foo")).success.value

      s"should redirect away when pending queue returns $Registered" in {
        val internalId = "foo"

        when(connector.checkPendingQueue(ArgumentMatchers.eq(utr))(ArgumentMatchers.any())) thenReturn Future.successful(Registered)
        val action = new Harness(connector)
        val result = action.callRefine(OptionalDataRequest(request, internalId, authUtr = None, userAnswers = Some(userAnswersWithEnterBusinessDetailsPage))).futureValue

        result.left.toOption.get mustBe Redirect(controllers.routes.ApplicationAlreadySubmittedController.onPageLoad)
      }
      s"should redirect away when pending queue returns $Pending" in {
        val internalId = "foo"

        when(connector.checkPendingQueue(ArgumentMatchers.eq(utr))(ArgumentMatchers.any()))thenReturn Future.successful(Pending)
        val action = new Harness(connector)
        val result = action.callRefine(OptionalDataRequest(request, internalId, authUtr = None, userAnswers = Some(userAnswersWithEnterBusinessDetailsPage))).futureValue

        result.left.toOption.get mustBe Redirect(controllers.routes.RegistrationPendingController.onPageLoad)
      }
      s"should call rosm when pending queue returns $DoesNotExist but redirect away when rosm returns None" in {
        val internalId = "foo"

        when(connector.checkPendingQueue(ArgumentMatchers.eq(utr))(ArgumentMatchers.any())) thenReturn Future.successful(DoesNotExist)
        when(connector.retreiveRosmSubscription(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(internalId))(ArgumentMatchers.any())) thenReturn Future.successful(None)
        val action = new Harness(connector)
        val result = action.callRefine(OptionalDataRequest(request, internalId, authUtr = None, userAnswers = Some(userAnswersWithEnterBusinessDetailsPage))).futureValue

        result.left.toOption.get mustBe Redirect(controllers.routes.IndexController.onPageLoad)
      }
      s"should call rosm when pending queue returns $DoesNotExist and return success with rosm subscription" in {
        val internalId = "foo"
        when(connector.checkPendingQueue(ArgumentMatchers.eq(utr))(ArgumentMatchers.any()))  thenReturn Future.successful(DoesNotExist)
        when(connector.retreiveRosmSubscription(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(internalId))(ArgumentMatchers.any())) thenReturn Future.successful(Some(rosmRegistration))
        val action = new Harness(connector)
        val result = action.callRefine(OptionalDataRequest(request, internalId, authUtr = None, userAnswers = Some(userAnswersWithEnterBusinessDetailsPage))).futureValue

        val rightResult = result.toOption.get
        rightResult.rosmWithUtr mustBe rosmRegistration
        rightResult.userAnswers mustBe userAnswersWithEnterBusinessDetailsPage
        rightResult.authUtr mustBe None
      }
    }
    s"when user has user answers but no utr and has NOT answered the $EnterBusinessDetailsPage, redirect away" in {
      val internalId = "foo"
      val action = new Harness(connector)
      val result = action.callRefine(OptionalDataRequest(request, internalId, authUtr = None, userAnswers = Some(emptyUserAnswers))).futureValue

      result.left.toOption.get mustBe Redirect(controllers.routes.EnterBusinessDetailsController.onPageLoad(NormalMode))
    }
  }
}
