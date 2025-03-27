package controllers

import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import pages.StartDatePage
import play.api.i18n.{Messages, MessagesApi, MessagesProvider}
import play.api.test.{FakeRequest, WsTestClient}

class CannotRegisterPartnershipControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/cannot-register-partnership"

  "GET " + normalRoutePath - {
    "should return OK and render the CannotRegisterPartnership page" in {
      build.commonPrecondition

      val userAnswers = emptyUserAnswers.set(StartDatePage, date).success.value
      setAnswers(userAnswers)

      given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
      given request: FakeRequest[_] = FakeRequest()
      given messagesProvider: MessagesProvider = messagesApi.preferred(request)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(Messages("cannotRegisterPartnership" + ".title"))
          page.getElementsByClass("govuk-body").text() mustBe Messages("cannotRegisterPartnership.subText", "0300 200 1000")
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, Messages("cannotRegisterPartnership" + ".title")(using messagesProvider))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)

  }
}
