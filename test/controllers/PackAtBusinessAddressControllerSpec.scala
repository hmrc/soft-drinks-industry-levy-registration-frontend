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
import forms.PackAtBusinessAddressFormProvider
import helpers.LoggerHelper
import models.{NormalMode, RegisterState, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{times, verify}
import org.scalatestplus.mockito.MockitoSugar
import pages.PackAtBusinessAddressPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{AddressLookupService, PackingDetails, SessionService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import utilities.GenericLogger
import views.html.PackAtBusinessAddressView

import scala.concurrent.Future

class PackAtBusinessAddressControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new PackAtBusinessAddressFormProvider()
  val form = formProvider()

  lazy val packAtBusinessAddressRoute = routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url

  "PackAtBusinessAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), rosmRegistration = rosmRegistration).build()

      running(application) {
        val request = FakeRequest(GET, packAtBusinessAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PackAtBusinessAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, HtmlContent("Super Lemonade Plc<br/>105B Godfrey Marchant Grove<br/>Guildford<br/>GU14 8NL"),  NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(identifier, RegisterState.RegisterWithAuthUTR).set(PackAtBusinessAddressPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), rosmRegistration = rosmRegistration).build()

      running(application) {
        val request = FakeRequest(GET, packAtBusinessAddressRoute)

        val view = application.injector.instanceOf[PackAtBusinessAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), HtmlContent("Super Lemonade Plc<br/>105B Godfrey Marchant Grove<br/>Guildford<br/>GU14 8NL"), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted (true)" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn createSuccessRegistrationResult(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), rosmRegistration = rosmRegistration)
          .overrides(
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packAtBusinessAddressRoute)
        .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to the next page when valid data is submitted (false)" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockAddressLookupService = mock[AddressLookupService]
      val onwardUrlForALF = "foobarwizz"


      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockAddressLookupService.initJourneyAndReturnOnRampUrl(
        ArgumentMatchers.eq(PackingDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(onwardUrlForALF))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AddressLookupService].toInstance(mockAddressLookupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packAtBusinessAddressRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardUrlForALF

        verify(mockAddressLookupService, times(1)).initJourneyAndReturnOnRampUrl(
          ArgumentMatchers.eq(PackingDetails), ArgumentMatchers.any(), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), rosmRegistration = rosmRegistration).build()

      running(application) {
        val request =
          FakeRequest(POST, packAtBusinessAddressRoute)
        .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PackAtBusinessAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, HtmlContent("Super Lemonade Plc<br/>105B Godfrey Marchant Grove<br/>Guildford<br/>GU14 8NL"), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, rosmRegistration = rosmRegistration).build()

      running(application) {
        val request = FakeRequest(GET, packAtBusinessAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, rosmRegistration = rosmRegistration).build()

      running(application) {
        val request =
          FakeRequest(POST, packAtBusinessAddressRoute)
        .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn createFailureRegistrationResult(SessionDatabaseInsertError)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), rosmRegistration = rosmRegistration)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, packAtBusinessAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on packAtBusinessAddress"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
