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
import connectors.{DoesNotExist, Pending, Registered, SoftDrinksIndustryLevyConnector}
import errors.{AuthenticationError, EnteredBusinessDetailsDoNotMatch, MissingRequiredUserAnswers, NoROSMRegistration, SessionDatabaseInsertError, UnexpectedResponseFromSDIL}
import models.RegisterState.{AlreadyRegistered, RegisterApplicationAccepted, RegisterWithAuthUTR, RegisterWithOtherUTR, RegistrationPending, RequiresBusinessDetails}
import models._
import models.requests.{DataRequest, IdentifierRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{ContactDetailsPage, OrganisationTypePage}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import services.SessionService

class RegistrationOrchestratorSpec extends RegistrationSubscriptionHelper with MockitoSugar {

  val mockSDILConnector = mock[SoftDrinksIndustryLevyConnector]
  val mockSessionService = mock[SessionService]

  val orchestrator = new RegistrationOrchestrator(mockSDILConnector, mockSessionService, logger)

  val identifyRequestWithAuthUtrAndRegistered: IdentifierRequest[AnyContent] =
    IdentifierRequest(FakeRequest(), identifier, true, Some(utr), true)

  val identifyRequestWithAuthAndNotRegistered: IdentifierRequest[AnyContent] =
    identifyRequestWithAuthUtrAndRegistered.copy(isRegistered = false)

  val identifyRequestWithNoAuthAndNotRegistered: IdentifierRequest[AnyContent] =
    identifyRequestWithAuthUtrAndRegistered.copy(optUTR = None, hasCTEnrolment = false, isRegistered = false)

  def expectedRegStateForSubscriptionStatus(isEnteredUtr: Boolean) = {
    val registerWithUTR = if (isEnteredUtr) {
      RegisterWithOtherUTR
    } else {
      RegisterWithAuthUTR
    }
    Map(
      Pending -> RegistrationPending,
      Registered -> RegisterApplicationAccepted,
      DoesNotExist -> registerWithUTR
    )
  }

  "handleRegistrationRequest" - {
    "when the user has an authUtr " - {
      "that has rosmData associated" - {
        "for a user that is registered" - {
          "should return AlreadyRegistered" in {

            when(mockSDILConnector.retreiveRosmSubscription(utr, identifier)(hc)).thenReturn(createSuccessRegistrationResult(rosmRegistration))
            when(mockSessionService.set(any())).thenReturn(createSuccessRegistrationResult(true))

            val res = orchestrator.handleRegistrationRequest(identifyRequestWithAuthUtrAndRegistered, hc, ec)

            whenReady(res.value) {result =>
              result mustBe Right(AlreadyRegistered)
            }
          }
        }
        expectedRegStateForSubscriptionStatus(false).foreach{case (subscriptionStatus, expectedState) =>
          s"and has a subscription status of $subscriptionStatus" - {
            "for a user that is not registered" - {
              s"should return $expectedState" in {
                when(mockSDILConnector.retreiveRosmSubscription(utr, identifier)(hc)).thenReturn(createSuccessRegistrationResult(rosmRegistration))
                when(mockSDILConnector.checkPendingQueue(utr)(hc)).thenReturn(createSuccessRegistrationResult(subscriptionStatus))
                when(mockSessionService.set(any())).thenReturn(createSuccessRegistrationResult(true))

                val res = orchestrator.handleRegistrationRequest(identifyRequestWithAuthAndNotRegistered, hc, ec)

                whenReady(res.value) { result =>
                  result mustBe Right(expectedState)
                }
              }
            }
          }
        }

        "for a user that is not register" - {
          "but the call to get subscriptionStatus fails" - {
            "should return UnexpectedResponseFromSdil" in {
              when(mockSDILConnector.retreiveRosmSubscription(utr, identifier)(hc)).thenReturn(createSuccessRegistrationResult(rosmRegistration))
              when(mockSDILConnector.checkPendingQueue(utr)(hc)).thenReturn(createFailureRegistrationResult(UnexpectedResponseFromSDIL))

              val res = orchestrator.handleRegistrationRequest(identifyRequestWithAuthAndNotRegistered, hc, ec)

              whenReady(res.value) { result =>
                result mustBe Left(UnexpectedResponseFromSDIL)
              }
            }
          }
        }
      }

      "that has no rosmData associated" - {
        "for a user who is registered" - {
          "should return an AuthenticationError" in {
            when(mockSDILConnector.retreiveRosmSubscription(utr, identifier)(hc)).thenReturn(createFailureRegistrationResult(NoROSMRegistration))

            val res = orchestrator.handleRegistrationRequest(identifyRequestWithAuthUtrAndRegistered, hc, ec)

            whenReady(res.value) { result =>
              result mustBe Left(AuthenticationError)
            }
          }
        }

        "for a user who is not registered" - {
          "should return RequiresBusinessDetails" in {
            when(mockSDILConnector.retreiveRosmSubscription(utr, identifier)(hc)).thenReturn(createFailureRegistrationResult(NoROSMRegistration))
            when(mockSessionService.set(any())).thenReturn(createSuccessRegistrationResult(true))

            val res = orchestrator.handleRegistrationRequest(identifyRequestWithAuthAndNotRegistered, hc, ec)

            whenReady(res.value) { result =>
              result mustBe Right(RequiresBusinessDetails)
            }
          }
        }
      }
      "but the call to get rosmRegistration fails" - {
        "should return a UnexpectedResponseFromSdil error" in {
          when(mockSDILConnector.retreiveRosmSubscription(utr, identifier)(hc)).thenReturn(createFailureRegistrationResult(UnexpectedResponseFromSDIL))

          val res = orchestrator.handleRegistrationRequest(identifyRequestWithAuthAndNotRegistered, hc, ec)

          whenReady(res.value) { result =>
            result mustBe Left(UnexpectedResponseFromSDIL)
          }
        }
      }
    }

    "when a user has no authUtr" - {
      "should return RequiresBusinessDetails" in {
        val res = orchestrator.handleRegistrationRequest(identifyRequestWithNoAuthAndNotRegistered, hc, ec)

        whenReady(res.value) { result =>
          result mustBe Right(RequiresBusinessDetails)
        }
      }
    }
  }

  "checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers" - {
    "when the utr entered has rosmRegistration" - {
      expectedRegStateForSubscriptionStatus(true).foreach {
        case (subscriptionStatus, expectedRegisterState) =>
          "that has a postcode that exactly matches that typed in" - {
            val identify = Identify(utr = utr, postcode = "GU14 8NL")
            s"and has a subscription status of $subscriptionStatus" - {
              s"should return $expectedRegisterState" in {
                when(mockSDILConnector.retreiveRosmSubscription(utr, identifier)(hc)).thenReturn(createSuccessRegistrationResult(rosmRegistration))
                when(mockSDILConnector.checkPendingQueue(utr)(hc)).thenReturn(createSuccessRegistrationResult(subscriptionStatus))

                val res = orchestrator.checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers(identify, identifier)

                whenReady(res.value) { result =>
                  result mustBe Right(expectedRegisterState)
                }
              }
            }
          }

          "that has a postcode that matches that typed in expect for case and spacing" - {
            val identify = Identify(utr = utr, postcode = "gu148nl")
            s"and has a subscription status of $subscriptionStatus" - {
              s"should return $expectedRegisterState" in {
                when(mockSDILConnector.retreiveRosmSubscription(utr, identifier)(hc)).thenReturn(createSuccessRegistrationResult(rosmRegistration))
                when(mockSDILConnector.checkPendingQueue(utr)(hc)).thenReturn(createSuccessRegistrationResult(subscriptionStatus))

                val res = orchestrator.checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers(identify, identifier)

                whenReady(res.value) { result =>
                  result mustBe Right(expectedRegisterState)
                }
              }
            }
          }
        }

      "the postcode entered does not match the rosmReg postcode" - {
        "should return EnteredBusinessDetailsDoNotMatch error" in {
          val identify = Identify(utr, "DD1 3WE")
          when(mockSDILConnector.retreiveRosmSubscription(utr, identifier)(hc)).thenReturn(createSuccessRegistrationResult(rosmRegistration))
          val res = orchestrator.checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers(identify, identifier)

          whenReady(res.value) { result =>
            result mustBe Left(EnteredBusinessDetailsDoNotMatch)
          }
        }
      }

      "the call to get subscription status fails" - {
        "should return UnexpectedResponseFromSdil" in {
          val identify = Identify(utr = utr, postcode = "GU14 8NL")
          when(mockSDILConnector.retreiveRosmSubscription(utr, identifier)(hc)).thenReturn(createSuccessRegistrationResult(rosmRegistration))
          when(mockSDILConnector.checkPendingQueue(utr)(hc)).thenReturn(createFailureRegistrationResult(UnexpectedResponseFromSDIL))

          val res = orchestrator.checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers(identify, identifier)

          whenReady(res.value) { result =>
            result mustBe Left(UnexpectedResponseFromSDIL)
          }
        }
      }
    }

    "when the utr entered does not have rosm data" - {
      "should return NoROSMRegistration" in {
        val identify = Identify(utr = utr, postcode = "GU14 8NL")
        when(mockSDILConnector.retreiveRosmSubscription(utr, identifier)(hc)).thenReturn(createFailureRegistrationResult(NoROSMRegistration))

        val res = orchestrator.checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers(identify, identifier)

        whenReady(res.value) { result =>
          result mustBe Left(NoROSMRegistration)
        }
      }
    }

    "when the call to get rosm data fails" - {
      "should return UnexpectedResponseFromSDIL" in {
        val identify = Identify(utr = utr, postcode = "GU14 8NL")
        when(mockSDILConnector.retreiveRosmSubscription(utr, identifier)(hc)).thenReturn(createFailureRegistrationResult(UnexpectedResponseFromSDIL))

        val res = orchestrator.checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers(identify, identifier)

        whenReady(res.value) { result =>
          result mustBe Left(UnexpectedResponseFromSDIL)
        }
      }
    }
  }

  "createSubscriptionAndUpdateUserAnswers" - {
    OrganisationType.valuesWithST.filterNot(_ == OrganisationType.Partnership).foreach { orgType =>
      HowManyLitresGlobally.values.foreach { litresGlobally =>
        "should create and send the subscription, update the useranswers to contain submitted time and return unit" - {
          "when the user answers contains the required pages" - {
            s"for a ${orgType.toString} that is a ${litresGlobally.toString} producer" - {
              "that has all litres pages populated, warehouses and packagaing site" in {
                val userAnswers = getCompletedUserAnswers(orgType, litresGlobally, true)
                val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), identifier, true, Some(utr), userAnswers, rosmRegistration)
                val expectedSubscription = getExpectedSubscription(orgType, litresGlobally, true)
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
                val expectedSubscription = getExpectedSubscription(orgType, litresGlobally, false)

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
                val expectedSubscription = getExpectedSubscription(orgType, litresGlobally, true)
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
                val expectedSubscription = getExpectedSubscription(orgType, litresGlobally, false)

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
