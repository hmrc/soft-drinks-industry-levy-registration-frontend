package controllers

import base.SpecBase
import errors.SessionDatabaseInsertError
import helpers.LoggerHelper
import utilities.GenericLogger
import forms.$className$FormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.$className$Page
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import views.html.$className$View

import scala.concurrent.Future
import org.jsoup.Jsoup
import java.time.{LocalDate, ZoneOffset}

class $className$ControllerSpec extends SpecBase with MockitoSugar with LoggerHelper {

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = LocalDate.now(ZoneOffset.UTC)

  val formProvider = new $className$FormProvider()
  val form = formProvider()

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, $className;
  format = "decap" $Route
  )

  def postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, $className;
  format = "decap" $Route
  )
  .withFormUrlEncodedBody(
    "value.day" -> validAnswer.getDayOfMonth.toString,
    "value.month" -> validAnswer.getMonthValue.toString,
    "value.year" -> validAnswer.getYear.toString
  )

  lazy val $className;format="decap"$Route = routes.$className$Controller.onPageLoad(NormalMode).url

  "$className$ Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(getRequest, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(identifier).set(DatePageNamePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val view = application.injector.instanceOf[$className$View]

        val result = route(application, getRequest).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode)(getRequest, messages(application)).toString
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

        val result = route(application, postRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, $className;
        format = "decap" $Route
        )
        .withFormUrlEncodedBody(("value", "invalid value"))

        running(application) {
          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[$className$View]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must fail if the setting of userAnswers fails" in {
      val mockSessionService = mock[SessionService]

      when(mockSessionService.set(any())) thenReturn Future.successful(Right(true))

      val application =
        applicationBuilder(userAnswers = Some(userDetailsWithSetMethodsReturningFailure))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionService].toInstance(mockSessionService)
          )
          .build()

      running(application) {
        val result = route(application, postRequest).value

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

          await(route(application, postRequest).value)
          events.collectFirst {
            case event =>
              event.getLevel.levelStr mustBe "ERROR"
              event.getMessage mustEqual "Failed to set value in session repository while attempting set on $className;format="decap"$"
          }.getOrElse(fail("No logging captured"))
        }
      }
    }
  }
}
