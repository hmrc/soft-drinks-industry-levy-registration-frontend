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
import forms.PackagingSiteNameFormProvider
import helpers.LoggerHelper
import models.RegisterState.RegisterWithAuthUTR
import models.alf.AddressResponseForLookupState
import models.backend.UkAddress
import models.{NormalMode, PackagingSiteName, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.PackagingSiteNamePage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AddressLookupState.PackingDetails
import services.SessionService
import utilities.GenericLogger
import views.html.PackagingSiteNameView

class PackagingSiteNameControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new PackagingSiteNameFormProvider()
  val form: Form[PackagingSiteName] = formProvider()

  lazy val packagingSiteNameRoute: String = routes.PackagingSiteNameController.onPageLoad(NormalMode).url
  val ukAddress = UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some("bar"))
  val sdilId = "123456"

  val alfResponseForLookupState = AddressResponseForLookupState(ukAddress, PackingDetails, sdilId)


  val userAnswers: UserAnswers = UserAnswers(
    identifier,
    RegisterWithAuthUTR,
    alfResponseForLookupState = Some(alfResponseForLookupState)
  )

  val userAnswersWithFormData = userAnswers.copy(data = Json.obj(
    PackagingSiteNamePage.toString -> Json.obj(
      "packagingSiteName" -> "value 1"
    )
  ))

  "PackagingSiteName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, packagingSiteNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PackagingSiteNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must not prepopulate the view on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithFormData)).build()

      running(application) {
        val request = FakeRequest(GET, packagingSiteNameRoute)

        val view = application.injector.instanceOf[PackagingSiteNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(PackagingSiteName("value 1")), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn createSuccessRegistrationResult(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteNameRoute)
        .withFormUrlEncodedBody(("packagingSiteName", "value 1"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteNameRoute
        )
        .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PackagingSiteNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, packagingSiteNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, packagingSiteNameRoute
        )
        .withFormUrlEncodedBody(("packagingSiteName", "value 1"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn createFailureRegistrationResult(errors.SessionDatabaseInsertError)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, packagingSiteNameRoute
          )
          .withFormUrlEncodedBody(("packagingSiteName", "value 1"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on packagingSiteName"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
