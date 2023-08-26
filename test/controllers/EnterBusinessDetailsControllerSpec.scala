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
import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions.DataRequiredActionImpl
import errors.{EnteredBusinessDetailsDoNotMatch, NoROSMRegistration, SessionDatabaseInsertError}
import forms.EnterBusinessDetailsFormProvider
import handlers.ErrorHandler
import helpers.LoggerHelper
import models.requests.{DataRequest, OptionalDataRequest}
import models.{Identify, RegisterState}
import navigation.{FakeNavigator, Navigator}
import orchestrators.RegistrationOrchestrator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.EnterBusinessDetailsPage
import play.api.inject.bind
import play.api.mvc.{Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.http.HttpClient
import utilities.GenericLogger
import views.html.EnterBusinessDetailsView

import scala.concurrent.Future

class EnterBusinessDetailsControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute = Call("GET", "/foo")

  class Harness(connector: SoftDrinksIndustryLevyConnector) extends DataRequiredActionImpl(connector, application.injector.instanceOf[GenericLogger], application.injector.instanceOf[ErrorHandler]) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  val mockHttp = mock[HttpClient]
  val mockOrchestrator = mock[RegistrationOrchestrator]
  val mockSessionService = mock[SessionService]

  val formProvider = new EnterBusinessDetailsFormProvider()
  val form = formProvider()

  lazy val enterBusinessDetailsRoute = routes.EnterBusinessDetailsController.onPageLoad.url
  val emptyUserAnswersForEnterBusinessDetails = emptyUserAnswers.copy(registerState = RegisterState.RequiresBusinessDetails)


  "EnterBusinessDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForEnterBusinessDetails)).build()

      running(application) {
        val request = FakeRequest(GET, enterBusinessDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EnterBusinessDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application), frontendAppConfig).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockOrchestrator.checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers(any(), any())(any(), any()))
        .thenReturn(createSuccessRegistrationResult(RegisterState.RegisterWithOtherUTR))
      when(mockSessionService.set(any())) thenReturn createSuccessRegistrationResult(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForEnterBusinessDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService),
            bind[RegistrationOrchestrator].toInstance(mockOrchestrator)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, enterBusinessDetailsRoute)
            .withFormUrlEncodedBody(("utr", "0000000437"), ("postcode", "GU14 8NL"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }


    "must redirect to the next page with no updates when answers are the same as previously entered" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForEnterBusinessDetails
        .set(EnterBusinessDetailsPage, Identify("0000000437", "GU14 8NL")).success.value))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService),
            bind[RegistrationOrchestrator].toInstance(mockOrchestrator)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, enterBusinessDetailsRoute)
            .withFormUrlEncodedBody(("utr", "0000000437"), ("postcode", "GU14 8NL"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when utr or postcode is found from rosm data when submitted" in {

      when(mockOrchestrator.checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers(any(), any())(any(), any()))
        .thenReturn(createFailureRegistrationResult(EnteredBusinessDetailsDoNotMatch))
      when(mockSessionService.set(any())) thenReturn createSuccessRegistrationResult(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForEnterBusinessDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService),
            bind[RegistrationOrchestrator].toInstance(mockOrchestrator)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, enterBusinessDetailsRoute)
            .withFormUrlEncodedBody(("utr", "0000000436"), ("postcode", "GU14 8NL"))

        val view = application.injector.instanceOf[EnterBusinessDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual  view(form.fill(Identify(utr = "0000000436", postcode = "GU14 8NL"))
          .withError("utr", "enterBusinessDetails.no-record.utr"))(request, messages(application), frontendAppConfig).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockOrchestrator.checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers(any(), any())(any(), any()))
        .thenReturn(createFailureRegistrationResult(NoROSMRegistration))
      when(mockSessionService.set(any())) thenReturn createSuccessRegistrationResult(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForEnterBusinessDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService),
            bind[RegistrationOrchestrator].toInstance(mockOrchestrator)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, enterBusinessDetailsRoute)
            .withFormUrlEncodedBody(("utr", ""), ("postcode", ""))

        val boundForm = form.bind(Map("utr" -> "", "postcode" -> ""))

        val view = application.injector.instanceOf[EnterBusinessDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application), frontendAppConfig).toString
      }
    }

    "should log an error message when internal server error is returned when user answers are not set in session repository" in {

      when(mockOrchestrator.checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers(any(), any())(any(), any()))
        .thenReturn(createSuccessRegistrationResult(RegisterState.RegisterWithOtherUTR))
      when(mockSessionService.set(any())) thenReturn createFailureRegistrationResult(SessionDatabaseInsertError)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersForEnterBusinessDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService),
            bind[RegistrationOrchestrator].toInstance(mockOrchestrator)
          )
          .build()

      running(application) {
        withCaptureOfLoggingFrom(application.injector.instanceOf[GenericLogger].logger) { events =>
          val request =
            FakeRequest(POST, enterBusinessDetailsRoute)
            .withFormUrlEncodedBody(("utr", "0000000437"), ("postcode", "GU14 8NL"))

          await(route(application, request).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on enterBusinessDetails"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
