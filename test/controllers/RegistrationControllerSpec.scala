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
import controllers.actions.{DataRequiredAction, DataRequiredActionImpl, DataRetrievalAction, FakeDataRetrievalAction, FakeIdentifierAction, FakeIdentifierActionWithCTEnrolment, IdentifierAction}
import errors.SessionDatabaseInsertError
import handlers.ErrorHandler
import models.UserAnswers
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SessionService
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar {

  val mockSessionService = mock[SessionService]
  protected def applicationBuilderForHomePage(hasCTEnrolment: Boolean = false): GuiceApplicationBuilder = {
    if(hasCTEnrolment) {
      new GuiceApplicationBuilder()
        .overrides(
          bind[SessionService].toInstance(mockSessionService),
          bind[IdentifierAction].to[FakeIdentifierAction]
      )
    } else {
      new GuiceApplicationBuilder()
        .overrides(
          bind[SessionService].toInstance(mockSessionService),
          bind[IdentifierAction].to[FakeIdentifierActionWithCTEnrolment]
        )
    }
  }

  "RegistrationController.start" - {
    "when a valid user" - {
      "should generate a user answers record" - {
        "and redirect to ??? when the user has a IR-CT enrolment" in {
          val application = applicationBuilderForHomePage(true).build()
          when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

          running(application) {
            val request = FakeRequest(GET, routes.RegistrationController.start.url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some(routes.IndexController.onPageLoad.url)
          }
        }
        "and redirect to ??? when the user has no IR-CT enrolment" in {
          val application = applicationBuilderForHomePage(false).build()
          when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

          running(application) {
            val request = FakeRequest(GET, routes.RegistrationController.start.url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some(routes.IndexController.onPageLoad.url)
          }
        }
      }
    }

    "should render the error page when the database call fails" in {
      val application = applicationBuilderForHomePage(false).build()
      when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationController.start.url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - soft-drinks-industry-levy - GOV.UK"
      }
    }
  }
}
