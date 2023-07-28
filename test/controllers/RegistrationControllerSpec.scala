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
import errors.SessionDatabaseInsertError
import models.NormalMode
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import viewmodels.govuk.SummaryListFluency

class RegistrationControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar{

  val mockSessionService = mock[SessionService]

  def applicationBuilderForHome(): GuiceApplicationBuilder = {
    val bodyParsers = stubControllerComponents().parsers.defaultBodyParser
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].toInstance(new FakeIdentifierAction(bodyParsers)),
        bind[SessionService].toInstance(mockSessionService)
      )
  }


  "RegistrationController.start" - {
    "when a valid user" - {
      "should generate a user answers record" - {
        "and redirect to Verify Controller when the user has a IR-CT enrolment" in {
          val application = applicationBuilderForHome().build()
          when(mockSessionService.set(any())) thenReturn createSuccessRegistrationResult(true)

          running(application) {
            val request = FakeRequest(GET, routes.RegistrationController.start.url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some(routes.VerifyController.onPageLoad(NormalMode).url)
          }
        }
      }
    }

    "should render the error page when the database call fails" in {
      val application = applicationBuilderForHome().build()
      when(mockSessionService.set(any())) thenReturn createFailureRegistrationResult(SessionDatabaseInsertError)

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
