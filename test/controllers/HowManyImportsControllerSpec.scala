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
import errors.SessionDatabaseInsertError
import forms.HowManyLitresFormProvider
import helpers.LoggerHelper
import models.{ LitresInBands, NormalMode, RegisterState, UserAnswers }
import navigation.{ FakeNavigator, Navigator }
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.HowManyImportsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import utilities.GenericLogger
import views.html.HowManyImportsView

class HowManyImportsControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new HowManyLitresFormProvider
  val form = formProvider()

  lazy val howManyImportsRoute = routes.HowManyImportsController.onPageLoad(NormalMode).url

  "HowManyImports Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), rosmRegistration = rosmRegistration).build()

      running(application) {
        val request = FakeRequest(GET, howManyImportsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowManyImportsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(identifier, RegisterState.RegisterWithAuthUTR).set(HowManyImportsPage, LitresInBands(100, 200)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), rosmRegistration = rosmRegistration).build()

      running(application) {
        val request = FakeRequest(GET, howManyImportsRoute)

        val view = application.injector.instanceOf[HowManyImportsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(LitresInBands(100, 200)), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn createSuccessRegistrationResult(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), rosmRegistration = rosmRegistration)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, howManyImportsRoute)
            .withFormUrlEncodedBody(("lowBand", "1000"), ("highBand", "2000"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), rosmRegistration = rosmRegistration).build()

      running(application) {
        val request =
          FakeRequest(POST, howManyImportsRoute)
            .withFormUrlEncodedBody(("lowBand", ""), ("highBand", ""))

        val boundForm = form.bind(Map("lowBand" -> "", "highBand" -> ""))

        val view = application.injector.instanceOf[HowManyImportsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, rosmRegistration = rosmRegistration).build()

      running(application) {
        val request = FakeRequest(GET, howManyImportsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, rosmRegistration = rosmRegistration).build()

      running(application) {
        val request =
          FakeRequest(POST, howManyImportsRoute)
            .withFormUrlEncodedBody(("lowBand", "1000"), ("highBand", "2000"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure), rosmRegistration = rosmRegistration).build()

      running(application) {
        val request =
          FakeRequest(POST, howManyImportsRoute)
            .withFormUrlEncodedBody(("lowBand", "1000"), ("highBand", "2000"))

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustBe "Sorry, there is a problem with the service - 500 - Soft Drinks Industry Levy - GOV.UK"
      }
    }

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn createFailureRegistrationResult(SessionDatabaseInsertError)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), rosmRegistration = rosmRegistration)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService))
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, howManyImportsRoute)
              .withFormUrlEncodedBody(("lowBand", "1000"), ("highBand", "2000"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on howManyImports"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
