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
import forms.WarehousesTradingNameFormProvider
import helpers.LoggerHelper
import models.alf.AddressResponseForLookupState
import models.backend.UkAddress
import models.{NormalMode, UserAnswers, Warehouse, WarehousesTradingName}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AddressLookupState.WarehouseDetails
import services.SessionService
import utilities.GenericLogger
import views.html.WarehousesTradingNameView

class WarehousesTradingNameControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new WarehousesTradingNameFormProvider()
  val form: Form[WarehousesTradingName] = formProvider()
  val sdilId = "123456"
  val tradingName = "Sugary Lemonade"

  lazy val warehouseSiteNameRoute: String = routes.WarehousesTradingNameController.onPageLoad(NormalMode, sdilId).url
  val ukAddress = UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some("bar"))

  val alfResponseForLookupState = AddressResponseForLookupState(ukAddress, WarehouseDetails, sdilId)


  val userAnswersWithAlfResponseForSdilId: UserAnswers = emptyUserAnswers.copy(
    alfResponseForLookupState = Some(alfResponseForLookupState)
  )

  val userAnswersWithNoAlfResponseButWarehouseWithSdilRef = emptyUserAnswers.copy(
    warehouseList = Map(sdilId -> Warehouse(tradingName, ukAddress))
  )

  val userAnswersWithWarehouseButNotForSdilRef = emptyUserAnswers.copy(
    warehouseList = Map("5432456" -> Warehouse(tradingName, ukAddress))
  )

  "WarehousesTradingName Controller" - {
    "when the user has entered a warehouse address in alf" - {
      "must render the warehouseTradingName page for a GET" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithAlfResponseForSdilId)).build()

        running(application) {
          val request = FakeRequest(GET, warehouseSiteNameRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[WarehousesTradingNameView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, sdilId)(request, messages(application)).toString
        }
      }

      "must redirect to warehouseDetails when valid data is submitted" in {

        val mockSessionService = mock[SessionService]

        when(mockSessionService.set(any())) thenReturn createSuccessRegistrationResult(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithAlfResponseForSdilId))
            .overrides(
              bind[SessionService].toInstance(mockSessionService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, warehouseSiteNameRoute)
              .withFormUrlEncodedBody(("warehouseTradingName", "value 1"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.WarehouseDetailsController.onPageLoad(NormalMode).url
        }
      }
    }

    "when the user has a warehouse for the sdilRef but no alfAddress" - {
      "must render the warehouseTradingName page for a GET with trading name populated" in {
        val application = applicationBuilder(userAnswers = Some(userAnswersWithNoAlfResponseButWarehouseWithSdilRef)).build()

        running(application) {
          val request = FakeRequest(GET, warehouseSiteNameRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[WarehousesTradingNameView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(WarehousesTradingName(tradingName)), NormalMode, sdilId)(request, messages(application)).toString
        }
      }

      "must redirect to warehouseDetails when valid data is submitted" in {

        val mockSessionService = mock[SessionService]

        when(mockSessionService.set(any())) thenReturn createSuccessRegistrationResult(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithNoAlfResponseButWarehouseWithSdilRef))
            .overrides(
              bind[SessionService].toInstance(mockSessionService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, warehouseSiteNameRoute)
              .withFormUrlEncodedBody(("warehouseTradingName", "value 1"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.WarehouseDetailsController.onPageLoad(NormalMode).url
        }
      }
    }

    "when the user answers contains no alfAddress and has warehouse with none for the sdilId" - {
      "must not render the page and redirect to packagingSiteDetails for GET" in {
        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithWarehouseButNotForSdilRef))
            .overrides(
            )
            .build()

        running(application) {
          val request =
            FakeRequest(GET, warehouseSiteNameRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.WarehouseDetailsController.onPageLoad(NormalMode).url
        }
      }
    }


    "when the user answers contains no alfAddress and has no warehouse sites" - {
      "must not render the page and redirect to askSecondaryWarehouse page for GET" in {
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
            )
            .build()

        running(application) {
          val request =
            FakeRequest(GET, warehouseSiteNameRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithAlfResponseForSdilId)).build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseSiteNameRoute
          )
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[WarehousesTradingNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, sdilId)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, warehouseSiteNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseSiteNameRoute
          )
            .withFormUrlEncodedBody(("warehouseTradingName", "value 1"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn createFailureRegistrationResult(errors.SessionDatabaseInsertError)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithAlfResponseForSdilId))
          .overrides(
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, warehouseSiteNameRoute
            )
              .withFormUrlEncodedBody(("warehouseTradingName", "value 1"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on warehousesTradingName"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
