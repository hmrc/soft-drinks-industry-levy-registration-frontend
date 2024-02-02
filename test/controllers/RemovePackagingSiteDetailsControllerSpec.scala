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
import base.SpecBase.aTradingName
import errors.SessionDatabaseInsertError
import forms.RemovePackagingSiteDetailsFormProvider
import helpers.LoggerHelper
import models.NormalMode
import models.backend.{Site, UkAddress}
import navigation.{FakeNavigator, Navigator}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.Assertion
import org.scalatestplus.mockito.MockitoSugar
import pages.RemovePackagingSiteDetailsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.SessionService
import utilities.GenericLogger
import views.html.RemovePackagingSiteDetailsView

class RemovePackagingSiteDetailsControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new RemovePackagingSiteDetailsFormProvider()
  val form: Form[Boolean] = formProvider()

  "RemovePackagingSiteDetails Controller" - {
    def commonAssertionsForPageLoad(addressToBeDisplayed: Html, page: String, ref: String): Assertion = {
      val doc: Document = Jsoup.parse(page)
      doc.getElementById("packagingSiteDetails").text() mustBe addressToBeDisplayed.toString()
      doc.getElementsByTag("form").attr("action") mustBe routes.RemovePackagingSiteDetailsController.onSubmit(NormalMode, ref).url
    }

    "must redirect to site details page when 0 sites exists in list on load" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request = FakeRequest(GET, routes.RemovePackagingSiteDetailsController.onPageLoad(NormalMode, "foo").url)
          val result = await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "INFO"
              event.getMessage mustEqual s"User at ${RemovePackagingSiteDetailsPage.toString} with 1 or less sites in packaging site list. Redirected to PackagingSiteDetails"
          }.getOrElse(fail("No logging captured"))

          result.header.status mustEqual SEE_OTHER
          result.header.headers.get(LOCATION) mustEqual Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
        }
      }
    }

    "must redirect to site details page when 1 site exists in list on load" in {
      val ref: String = "12345678"
      val packagingSite: Map[String, Site] = Map(ref -> Site(
        UkAddress(List("a", "b"), "c"),
        None,
        aTradingName,
        None))
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(packagingSiteList = packagingSite))).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request = FakeRequest(GET, routes.RemovePackagingSiteDetailsController.onPageLoad(NormalMode, ref).url)
          val result = await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "INFO"
              event.getMessage mustEqual s"User at ${RemovePackagingSiteDetailsPage.toString} with 1 or less sites in packaging site list. Redirected to PackagingSiteDetails"
          }.getOrElse(fail("No logging captured"))

          result.header.status mustEqual SEE_OTHER
          result.header.headers.get(LOCATION) mustEqual Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
        }
      }
    }

    "must redirect to site details page when loaded but no site ref exists in list on load" in {
      val wrongRef: String = "wrongref"
      val packagingSite: Map[String, Site] = Map(
        "12345678" -> Site(UkAddress(List("a", "b"), "c"), None, aTradingName, None),
        "87654321" -> Site(UkAddress(List("a", "b"), "c"), None, aTradingName, None)
      )
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(packagingSiteList = packagingSite))).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request = FakeRequest(GET, routes.RemovePackagingSiteDetailsController.onPageLoad(NormalMode, wrongRef).url)
          val result = await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "WARN"
              event.getMessage mustEqual s"user has potentially hit page and ref does not exist for packaging site" +
                s"$wrongRef ${emptyUserAnswers.id} amount currently: 2"
          }.getOrElse(fail("No logging captured"))

          result.header.status mustEqual SEE_OTHER
          result.header.headers.get(LOCATION) mustEqual Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
        }
      }
    }

    "must redirect to the site details page when valid data is submitted and item exists in site details" in {
      val ref: String = "12345678"
      val mockSessionRepository = mock[SessionService]

      when(mockSessionRepository.set(any())) thenReturn createSuccessRegistrationResult(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(packagingSiteList = packagingSiteListWith3)))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RemovePackagingSiteDetailsController.onSubmit(NormalMode, ref).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to site details page when loaded but no site ref exists in list on submit" in {
      val wrongRef: String = "wrongref"
      val packagingSite: Map[String, Site] = Map(
        "12345678" -> Site(UkAddress(List("a", "b"), "c"), None, aTradingName, None),
        "87654321" -> Site(UkAddress(List("a", "b"), "c"), None, aTradingName, None)
      )
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(packagingSiteList = packagingSite))).build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request = FakeRequest(POST, routes.RemovePackagingSiteDetailsController.onSubmit(NormalMode, wrongRef).url)
          val result = await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "WARN"
              event.getMessage mustEqual s"user has potentially submit page and ref does not exist for packaging site" +
                s"$wrongRef ${emptyUserAnswers.id} amount currently: 2"
          }.getOrElse(fail("No logging captured"))

          result.header.status mustEqual SEE_OTHER
          result.header.headers.get(LOCATION) mustEqual Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val ref: String = "foo"
      val packagingSite: Map[String, Site] = Map(ref -> Site(
        UkAddress(List("a", "b"), "c"),
        None,
        aTradingName,
        None))
      val htmlExpectedInView = Html(s"$aTradingName<br>a, b, <span class=\"nowrap\" style=\"white-space: nowrap;\">c</span>")
      val htmlExpectedAfterRender = Html(s"$aTradingName a, b, c")
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(packagingSiteList = packagingSite))).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RemovePackagingSiteDetailsController.onSubmit(NormalMode, ref).url)
        .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemovePackagingSiteDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        val contentOfResult: String = contentAsString(result)
        contentOfResult mustEqual view(boundForm, NormalMode, ref, htmlExpectedInView)(request, messages(application)).toString
        commonAssertionsForPageLoad(htmlExpectedAfterRender, contentOfResult, ref)
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.RemovePackagingSiteDetailsController.onSubmit(NormalMode, "foo").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RemovePackagingSiteDetailsController.onSubmit(NormalMode, "foo").url)
        .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {
      val ref: String = "foo"
      val packagingSite: Map[String, Site] = Map(ref -> Site(
        UkAddress(List("a", "b"), "c"),
        None,
        aTradingName,
        None))
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn createFailureRegistrationResult(SessionDatabaseInsertError)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(packagingSiteList = packagingSite)))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, routes.RemovePackagingSiteDetailsController.onSubmit(NormalMode, ref).url)
          .withFormUrlEncodedBody(("value", "true"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on removePackagingSiteDetails"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
