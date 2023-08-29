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

import base.RegistrationSubscriptionHelper
import connectors.SoftDrinksIndustryLevyConnector
import errors.{MissingRequiredUserAnswers, SessionDatabaseInsertError}
import models._
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{ContactDetailsPage, OrganisationTypePage, StartDatePage}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import services.SessionService

class RegistrationOrchestratorSpec extends RegistrationSubscriptionHelper with MockitoSugar {

  val mockSDILConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockSessionService = mock[SessionService]

  val orchestrator = new RegistrationOrchestrator(mockSDILConnector, mockSessionService, logger)

  "createSubscriptionAndUpdateUserAnswers" - {
    OrganisationType.valuesWithST.filterNot(_ == OrganisationType.Partnership).foreach { orgType =>
      HowManyLitresGlobally.values.foreach { litresGlobally =>
        "should create and send the subscription, update the useranswers to contain submitted time and return unit" - {
          "when the user answers contains the required pages" - {
            s"for a ${orgType.toString} that is a ${litresGlobally.toString} producer" - {
              "that has all litres pages populated, warehouses and packagaing site" in {
                val userAnswers = getCompletedUserAnswers(orgType, litresGlobally, true)
                val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), identifier, true, Some(utr), userAnswers, rosmRegistration)
                val expectedSubscription = generateSubscription(orgType, litresGlobally, true)
                when(mockSDILConnector.createSubscription(expectedSubscription, "safeid")(hc)).thenReturn(createSuccessRegistrationResult((): Unit))
                when(mockSessionService.set(any())).thenReturn(createSuccessRegistrationResult(true))

                val res = orchestrator.createSubscriptionAndUpdateUserAnswers(dataRequest, hc, ec)

                whenReady(res.value) {result =>
                  result mustBe Right((): Unit)
                }
              }
              "that has no litres pages populated" in {
                val userAnswers = getCompletedUserAnswers(orgType, litresGlobally, false)
                val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), identifier, true, Some(utr), userAnswers, rosmRegistration)
                val expectedSubscription = generateSubscription(orgType, litresGlobally, false)

                when(mockSDILConnector.createSubscription(expectedSubscription, "safeid")(hc)).thenReturn(createSuccessRegistrationResult((): Unit))
                when(mockSessionService.set(any())).thenReturn(createSuccessRegistrationResult(true))
                val res = orchestrator.createSubscriptionAndUpdateUserAnswers(dataRequest, hc, ec)

                whenReady(res.value) { result =>
                  result mustBe Right((): Unit)
                }
              }
            }
          }
        }

        "should return a SessionDatabaseInsertError" - {
          "when the update of user answers fails" - {
            s"for a ${orgType.toString} that is a ${litresGlobally.toString} producer" - {
              "that has all litres pages populated, warehouses and packagaing site" in {
                val userAnswers = getCompletedUserAnswers(orgType, litresGlobally, true)
                val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), identifier, true, Some(utr), userAnswers, rosmRegistration)
                val expectedSubscription = generateSubscription(orgType, litresGlobally, true)
                when(mockSDILConnector.createSubscription(expectedSubscription, "safeid")(hc)).thenReturn(createSuccessRegistrationResult((): Unit))
                when(mockSessionService.set(any())).thenReturn(createFailureRegistrationResult(SessionDatabaseInsertError))

                val res = orchestrator.createSubscriptionAndUpdateUserAnswers(dataRequest, hc, ec)

                whenReady(res.value) { result =>
                  result mustBe Left(SessionDatabaseInsertError)
                }
              }
              "that has no litres pages populated" in {
                val userAnswers = getCompletedUserAnswers(orgType, litresGlobally, false)
                val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), identifier, true, Some(utr), userAnswers, rosmRegistration)
                val expectedSubscription = generateSubscription(orgType, litresGlobally, false)

                when(mockSDILConnector.createSubscription(expectedSubscription, "safeid")(hc)).thenReturn(createSuccessRegistrationResult((): Unit))
                when(mockSessionService.set(any())).thenReturn(createFailureRegistrationResult(SessionDatabaseInsertError))
                val res = orchestrator.createSubscriptionAndUpdateUserAnswers(dataRequest, hc, ec)

                whenReady(res.value) { result =>
                  result mustBe Left(SessionDatabaseInsertError)
                }
              }
            }
          }
        }
      }
    }

    "should return MissingRequiredUserAnswers error" - {
      "when the user answers doesn't include organisation type" in {
        val userAnswers = getCompletedUserAnswers(OrganisationType.LimitedCompany, HowManyLitresGlobally.Large, false)
          .remove(OrganisationTypePage).success.value
        val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), identifier, true, Some(utr), userAnswers, rosmRegistration)
        val res = orchestrator.createSubscriptionAndUpdateUserAnswers(dataRequest, hc, ec)

        whenReady(res.value) { result =>
          result mustBe Left(MissingRequiredUserAnswers)
        }
      }

      "when the user answers doesn't include contact details" in {
        val userAnswers = getCompletedUserAnswers(OrganisationType.LimitedCompany, HowManyLitresGlobally.Large, false)
          .remove(ContactDetailsPage).success.value
        val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), identifier, true, Some(utr), userAnswers, rosmRegistration)
        val res = orchestrator.createSubscriptionAndUpdateUserAnswers(dataRequest, hc, ec)

        whenReady(res.value) { result =>
          result mustBe Left(MissingRequiredUserAnswers)
        }
      }
    }
  }


}
