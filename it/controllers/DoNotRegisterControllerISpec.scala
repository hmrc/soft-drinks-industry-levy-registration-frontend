package controllers

import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.{FakeRequest, WsTestClient}
import testSupport.preConditions.PreconditionHelpers

class DoNotRegisterControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/do-not-register"

  override val preconditionHelpers: PreconditionHelpers = mock[PreconditionHelpers]
  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())
  
  "GET " + normalRoutePath - {
    "should return OK and render the DoNotRegister page" in {
      preconditionHelpers.commonPrecondition

      setAnswers(emptyUserAnswers)(using timeout)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(messages("doNotRegister" + ".title"))
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, messages("doNotRegister" + ".title"
    ) )
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }
}
