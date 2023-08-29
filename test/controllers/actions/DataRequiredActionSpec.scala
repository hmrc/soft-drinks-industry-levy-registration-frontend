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
import controllers.routes
import errors.NoROSMRegistration
import handlers.ErrorHandler
import models.requests.{DataRequest, OptionalDataRequest}
import models.{Identify, RegisterState}
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

  class Harness(connector: SoftDrinksIndustryLevyConnector) extends DataRequiredActionImpl(connector, application.injector.instanceOf[GenericLogger], application.injector.instanceOf[ErrorHandler]) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }
  val connector = mock[SoftDrinksIndustryLevyConnector]
  val request = FakeRequest()
  val postcode = "GU14 8NL"

  "Data Required Action" - {

    "when there is no user answers, user is redirected away" in {

      val action = new Harness(connector)
      val result = action.callRefine(OptionalDataRequest(request, "internalId", userAnswers = None)).futureValue
      result.left.toOption.get mustBe Redirect(controllers.routes.RegistrationController.start)
    }

    RegisterState.values.foreach{ registerState =>
      s"when the user answers are present and has a register state of $registerState" - {
        if(RegisterState.canRegister(registerState)) {
          "when the user entered a utr" - {
            if(registerState == RegisterState.RegisterWithOtherUTR) {
              val userAnswers = emptyUserAnswers.copy(registerState = registerState)
                .set(EnterBusinessDetailsPage, Identify(utr, postcode)).success.value
              "should return success" - {
                "when the utr has rosmData" in {
                  when(connector.retreiveRosmSubscription(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(identifier))(ArgumentMatchers.any()))
                    .thenReturn(createSuccessRegistrationResult(rosmRegistration))
                  val action = new Harness(connector)
                  val result = action.callRefine(OptionalDataRequest(request, identifier, authUtr = None, userAnswers = Some(userAnswers))).futureValue

                  val rightResult = result.toOption.get
                  rightResult.rosmWithUtr mustBe rosmRegistration
                  rightResult.userAnswers mustBe userAnswers
                  rightResult.authUtr mustBe None
                }
              }
              "should render the error page" - {
                "when the utr does not contain rosmData" in {
                  when(connector.retreiveRosmSubscription(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(identifier))(ArgumentMatchers.any()))
                    .thenReturn(createFailureRegistrationResult(NoROSMRegistration))
                  val action = new Harness(connector)
                  val result = action.callRefine(OptionalDataRequest(request, identifier, authUtr = None, userAnswers = Some(userAnswers))).futureValue
                  result.left.toOption.get.header.status mustBe 500
                }
                "when the useranswers does not contain the utr" in {
                  val userAnswerNoUtr = emptyUserAnswers.copy(registerState = registerState)
                  val action = new Harness(connector)
                  val result = action.callRefine(OptionalDataRequest(request, identifier, authUtr = None, userAnswers = Some(userAnswerNoUtr))).futureValue
                  result.left.toOption.get.header.status mustBe 500
                }
              }
            } else {
              val userAnswers = emptyUserAnswers.copy(registerState = registerState)
              "when the user has a utr in auth" - {
                "should return success" - {
                  "when the utr has rosmData" in {
                    when(connector.retreiveRosmSubscription(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(identifier))(ArgumentMatchers.any()))
                      .thenReturn(createSuccessRegistrationResult(rosmRegistration))
                    val action = new Harness(connector)
                    val result = action.callRefine(OptionalDataRequest(request, identifier, authUtr = Some(utr), userAnswers = Some(userAnswers))).futureValue

                    val rightResult = result.toOption.get
                    rightResult.rosmWithUtr mustBe rosmRegistration
                    rightResult.userAnswers mustBe userAnswers
                    rightResult.authUtr mustBe Some(utr)
                  }
                }
              }

              "should render the error page" - {
                "when the utr from auth does not contain rosmData" in {
                  when(connector.retreiveRosmSubscription(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(identifier))(ArgumentMatchers.any()))
                    .thenReturn(createFailureRegistrationResult(NoROSMRegistration))
                  val action = new Harness(connector)
                  val result = action.callRefine(OptionalDataRequest(request, identifier, authUtr = Some(utr), userAnswers = Some(userAnswers))).futureValue
                  result.left.toOption.get.header.status mustBe 500
                }
                "when there is no auth utr" in {
                  val action = new Harness(connector)
                  val result = action.callRefine(OptionalDataRequest(request, identifier, authUtr = None, userAnswers = Some(userAnswers))).futureValue
                  result.left.toOption.get.header.status mustBe 500
                }
              }
            }
          }
        } else {
          "should redirect away" in {
            val expectedRedirectLocation = registerState match {
              case RegisterState.RequiresBusinessDetails => routes.EnterBusinessDetailsController.onPageLoad
              case RegisterState.AlreadyRegistered => routes.AlreadyRegisteredController.onPageLoad
              case RegisterState.RegisterApplicationAccepted => routes.ApplicationAlreadySubmittedController.onPageLoad
              case _ => routes.RegistrationPendingController.onPageLoad
            }
            val userAnswers = emptyUserAnswers.copy(registerState = registerState)
            val action = new Harness(connector)
            val result = action.callRefine(OptionalDataRequest(request, identifier, authUtr = Some(utr), userAnswers = Some(userAnswers))).futureValue

            result.left.toOption.get mustBe Redirect(expectedRedirectLocation)
          }
        }
      }
    }
  }
}
