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
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.SessionService
import utilities.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.RemovePackagingSiteDetailsView

import scala.concurrent.Future

class RemovePackagingSiteDetailsControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RemovePackagingSiteDetailsFormProvider()
  val form = formProvider()

  "RemovePackagingSiteDetails Controller" - {
    def commonAssertionsForPageLoad(addressToBeDisplayed: Html, page: String, ref: String): Assertion = {
      val doc: Document = Jsoup.parse(page)
      doc.getElementById("packagingSiteDetails").text() mustBe addressToBeDisplayed.toString()
      doc.getElementsByTag("h1").text() mustEqual "Are you sure you want to remove this packaging site?"
      doc.getElementsByTag("form").attr("action") mustBe routes.RemovePackagingSiteDetailsController.onSubmit(ref).url
    }

    Map(
      "No Trading name" -> Site(
        UkAddress(List("a", "b"), "c"),
        None,
        None,
        None),
      "Trading Name" -> Site(
        UkAddress(List("a", "b"), "c"),
        None,
        Some("trading"),
        None),
      "Trading Name AND Long Address AND Long Postcode" -> Site(
        UkAddress(List("abcdefg abcdefg abcdefg abcdefg", "abcdefg abcdefg abcdefg abcdefg", "abcdefg abcdefg abcdefg"), "abcdefg abcdefg abcdefg abcdefg"),
        None,
        Some("trading"),
        None),
      "No Trading Name AND Long Address AND Long Postcode" -> Site(
        UkAddress(List("abcdefg abcdefg abcdefg abcdefg", "abcdefg abcdefg abcdefg abcdefg", "abcdefg abcdefg abcdefg"), "abcdefg abcdefg abcdefg abcdefg"),
        None,
        None,
        None),
      "No Trading Name AND no Lines AND Postcode" -> Site(
        UkAddress(List.empty, "abcdefg abcdefg abcdefg abcdefg"),
        None,
        None,
        None)
    ).foreach { test =>
      s"must return OK and the correct view for a GET when packaging site exists for ${test._1}" in {
        val ref: String = "foo"
        val htmlExpectedInView: Html = AddressFormattingHelper.addressFormatting(test._2.address, test._2.tradingName)

        val htmlExpectedAfterRender: Html = Html(htmlExpectedInView.body.replace("<br>", " "))
        val userAnswers = emptyUserAnswers.copy(packagingSiteList = Map(ref -> test._2))
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.RemovePackagingSiteDetailsController.onPageLoad(ref).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RemovePackagingSiteDetailsView]

          status(result) mustEqual OK
          val contentOfResult: String = contentAsString(result)

          contentOfResult mustEqual view(form, ref, htmlExpectedInView)(request, messages(application)).toString
          commonAssertionsForPageLoad(htmlExpectedAfterRender, contentOfResult, ref)
        }
      }
    }
    "must redirect to site details page when loaded but no site ref exists in list on load" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, routes.RemovePackagingSiteDetailsController.onPageLoad("foo").url)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
      }
    }
    "must redirect to the site details page when valid data is submitted and no item exists in the list for ref" in {
      val ref: String = "foo"

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RemovePackagingSiteDetailsController.onSubmit(ref).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url
      }
    }
    "must redirect to the site details page when valid data is submitted and item exists in site details" in {
      val ref: String = "foo"
      val packagingSite: Map[String, Site] = Map(ref -> Site(
        UkAddress(List("a", "b"), "c"),
        None,
        Some("trading"),
        None))
      val mockSessionRepository = mock[SessionService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(packagingSiteList = packagingSite)))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RemovePackagingSiteDetailsController.onSubmit(ref).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val ref: String = "foo"
      val packagingSite: Map[String, Site] = Map(ref -> Site(
        UkAddress(List("a", "b"), "c"),
        None,
        Some("trading"),
        None))
      val htmlExpectedInView = Html("trading<br>a, b, c")
      val htmlExpectedAfterRender = Html("trading a, b, c")
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(packagingSiteList = packagingSite))).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RemovePackagingSiteDetailsController.onSubmit(ref).url)
        .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemovePackagingSiteDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        val contentOfResult: String = contentAsString(result)
        contentOfResult mustEqual view(boundForm,ref,htmlExpectedInView)(request, messages(application)).toString
        commonAssertionsForPageLoad(htmlExpectedAfterRender, contentOfResult, ref)
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.RemovePackagingSiteDetailsController.onSubmit("foo").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RemovePackagingSiteDetailsController.onSubmit("foo").url)
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
        Some("trading"),
        None))
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Left(SessionDatabaseInsertError))

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
            FakeRequest(POST, routes.RemovePackagingSiteDetailsController.onSubmit(ref).url)
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
