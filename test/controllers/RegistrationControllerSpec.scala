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

package controllers

import base.SpecBase
import controllers.actions.{FakeIdentifierAction, IdentifierAction}
import errors.{RegistrationAlreadySubmitted, SessionDatabaseInsertError, UnexpectedResponseFromSDIL}
import models.RegisterState._
import models.{NormalMode, RegisterState}
import orchestrators.RegistrationOrchestrator
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency

class RegistrationControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar{

  val mockOrchestrator = mock[RegistrationOrchestrator]

  def applicationBuilderForHome(): GuiceApplicationBuilder = {
    val bodyParsers = stubControllerComponents().parsers.defaultBodyParser
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].toInstance(new FakeIdentifierAction(bodyParsers)),
        bind[RegistrationOrchestrator].toInstance(mockOrchestrator)
      )
  }

  def expectedLocationForRegState(regState: RegisterState): String = {
    regState match {
      case AlreadyRegistered => routes.AlreadyRegisteredController.onPageLoad.url
      case RegistrationPending => routes.RegistrationPendingController.onPageLoad.url
      case RequiresBusinessDetails => routes.EnterBusinessDetailsController.onPageLoad.url
      case RegisterApplicationAccepted => routes.ApplicationAlreadySubmittedController.onPageLoad.url
      case _ => routes.VerifyController.onPageLoad(NormalMode).url
    }
  }


  "RegistrationController.start" - {
    "when a valid user" - {
      RegisterState.values.foreach{registerState =>
        s"that is found to have a register state of $registerState" - {
          "should redirect to the expected location" in {

            val application = applicationBuilderForHome().build()
            when(mockOrchestrator.handleRegistrationRequest(any(), any(), any())) thenReturn createSuccessRegistrationResult(registerState)

            running(application) {
              val request = FakeRequest(GET, routes.RegistrationController.start.url)

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result) mustEqual Some(expectedLocationForRegState(registerState))
            }
          }
        }
      }
    }

    "should redirect to register confirmation page" - {
      "when the application has been submitted" in {
        val application = applicationBuilderForHome().build()
        when(mockOrchestrator.handleRegistrationRequest(any(), any(), any())) thenReturn createFailureRegistrationResult(RegistrationAlreadySubmitted)

        running(application) {
          val request = FakeRequest(GET, routes.RegistrationController.start.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some(routes.RegistrationConfirmationController.onPageLoad.url)
        }
      }
    }

    "should render the error page" - {
      "when a backend call fails" in {
        val application = applicationBuilderForHome().build()
        when(mockOrchestrator.handleRegistrationRequest(any(), any(), any())) thenReturn createFailureRegistrationResult(UnexpectedResponseFromSDIL)

        running(application) {
          val request = FakeRequest(GET, routes.RegistrationController.start.url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
          val page = Jsoup.parse(contentAsString(result))
          page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
        }
      }

      "when the database call fails" in {
        val application = applicationBuilderForHome().build()
        when(mockOrchestrator.handleRegistrationRequest(any(), any(), any())) thenReturn createFailureRegistrationResult(SessionDatabaseInsertError)

        running(application) {
          val request = FakeRequest(GET, routes.RegistrationController.start.url)

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
          val page = Jsoup.parse(contentAsString(result))
          page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
        }
      }
    }
  }
}
