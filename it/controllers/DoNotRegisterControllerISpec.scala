package controllers

import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.i18n.Messages
import play.api.test.WsTestClient

class DoNotRegisterControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/do-not-register"

  "GET " + normalRoutePath - {
    "should return OK and render the DoNotRegister page" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswers)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(Messages("doNotRegister" + ".title"))
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, Messages("doNotRegister" + ".title"
    ) )
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testWhoIsUnableToRegisterWithGivenUtr(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }
}
