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
import forms.VerifyFormProvider
import helpers.LoggerHelper
import models.Verify.{ No, YesNewAddress, YesRegister }
import models.{ NormalMode, RegisterState, UserAnswers, Verify }
import navigation.{ FakeNavigator, Navigator }
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.VerifyPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AddressLookupState.BusinessAddress
import services.{ AddressLookupService, SessionService }
import utilities.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.VerifyView

import scala.concurrent.Future

class VerifyControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new VerifyFormProvider()
  val form = formProvider()

  lazy val verifyRoute = routes.VerifyController.onPageLoad(NormalMode).url

  "Verify Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, verifyRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[VerifyView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, rosmRegistration.utr, AddressFormattingHelper.formatBusinessAddress(rosmRegistration.rosmRegistration.address, Some(rosmRegistration.rosmRegistration.organisationName)))(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(identifier, RegisterState.RegisterWithAuthUTR).set(VerifyPage, Verify.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, verifyRoute)

        val view = application.injector.instanceOf[VerifyView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(Verify.values.head), NormalMode, rosmRegistration.utr, AddressFormattingHelper.formatBusinessAddress(rosmRegistration.rosmRegistration.address, Some(rosmRegistration.rosmRegistration.organisationName)))(request, messages(application)).toString
      }
    }

    s"must redirect to the next page when valid data is submitted $YesRegister" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn createSuccessRegistrationResult(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, verifyRoute)
            .withFormUrlEncodedBody(("value", YesRegister.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
    s"must onboard to ALF when valid data is submitted $YesNewAddress" in {

      val mockSessionService = mock[SessionService]
      val mockAlfService = mock[AddressLookupService]

      when(mockSessionService.set(any())) thenReturn createSuccessRegistrationResult(true)
      when(mockAlfService
        .initJourneyAndReturnOnRampUrl(ArgumentMatchers.eq(BusinessAddress), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful("alfOnRamp"))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService),
            bind[AddressLookupService].toInstance(mockAlfService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, verifyRoute)
            .withFormUrlEncodedBody(("value", YesNewAddress.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "alfOnRamp"
      }
    }

    s"must redirect Sign out when valid data is submitted $No" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn createSuccessRegistrationResult(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, verifyRoute)
            .withFormUrlEncodedBody(("value", No.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual auth.routes.AuthController.signOutNoSurvey.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, verifyRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[VerifyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, rosmRegistration.utr, AddressFormattingHelper.formatBusinessAddress(rosmRegistration.rosmRegistration.address, Some(rosmRegistration.rosmRegistration.organisationName)))(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, verifyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, verifyRoute)
            .withFormUrlEncodedBody(("value", Verify.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    s"must fail if the setting of userAnswers fails for $YesRegister" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure)).build()

      running(application) {
        val request =
          FakeRequest(POST, verifyRoute)
            .withFormUrlEncodedBody(("value", YesRegister.toString))

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustBe "Sorry, there is a problem with the service - 500 - Soft Drinks Industry Levy - GOV.UK"
      }
    }
    s"must fail if the setting of userAnswers fails for $YesNewAddress" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure)).build()

      running(application) {
        val request =
          FakeRequest(POST, verifyRoute)
            .withFormUrlEncodedBody(("value", YesNewAddress.toString))

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
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService))
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, verifyRoute)
              .withFormUrlEncodedBody(("value", Verify.values.head.toString))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on verify"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
