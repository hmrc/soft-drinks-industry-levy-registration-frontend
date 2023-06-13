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
import helpers.LoggerHelper
import utilities.GenericLogger
import forms.WarehouseDetailsFormProvider
import models.backend.UkAddress
import models.{NormalMode, UserAnswers, Warehouse}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.WarehouseDetailsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import views.html.WarehouseDetailsView

import scala.concurrent.Future
import org.jsoup.Jsoup
import viewmodels.govuk.SummaryListFluency
import viewmodels.summary.WarehouseDetailsSummary

class WarehouseDetailsControllerSpec extends SpecBase with MockitoSugar with LoggerHelper with SummaryListFluency {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new WarehouseDetailsFormProvider()
  val form = formProvider()

  lazy val warehouseDetailsRoute = routes.WarehouseDetailsController.onPageLoad(NormalMode).url

  "WarehouseDetails Controller" - {

    "must redirect when no warehouses are added" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request = FakeRequest(GET, warehouseDetailsRoute)
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some(routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustEqual ("WARN")
              event.getMessage mustEqual ("Failed to load the requested page due to no warehouse being present")
          }.getOrElse(fail("No logging captured"))
        }
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val address = UkAddress(List("29 Station Place", "The Railyard", "Cambridge"), "CB1 2FP")
      val warehouse = Map("78941132" -> Warehouse(Some("Warehouse One"), address))
      val userAnswers = UserAnswers(identifier).set(WarehouseDetailsPage, true).success.value
      val uaWithWarehouses = userAnswers.copy(warehouseList = warehouse)
      val warehouseSummary = Some(SummaryListViewModel(rows = WarehouseDetailsSummary.warehouseDetailsRow(warehouse)))

      val application = applicationBuilder(userAnswers = Some(uaWithWarehouses)).build()

      running(application) {
        val request = FakeRequest(GET, warehouseDetailsRoute)

        val view = application.injector.instanceOf[WarehouseDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, warehouseSummary, 1)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute)
        .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val address = UkAddress(List("29 Station Place", "The Railyard", "Cambridge"), "CB1 2FP")
      val warehouse = Map("78941132" -> Warehouse(Some("Warehouse One"), address))
      val userAnswers = UserAnswers(identifier).set(WarehouseDetailsPage, true).success.value
      val uaWithWarehouses = userAnswers.copy(warehouseList = warehouse)
      val warehouseSummary = Some(SummaryListViewModel(rows = WarehouseDetailsSummary.warehouseDetailsRow(warehouse)))
      val application = applicationBuilder(userAnswers = Some(uaWithWarehouses)).build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute)
        .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[WarehouseDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, warehouseSummary, 1)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, warehouseDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute)
        .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must fail if the setting of userAnswers fails" in {

      val application = applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure)).build()

      running(application) {
        val request =
          FakeRequest(POST, warehouseDetailsRoute
        )
        .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
      }
    }

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, warehouseDetailsRoute)
          .withFormUrlEncodedBody(("value", "true"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on warehouseDetails"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
