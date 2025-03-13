package controllers

import models.RegisterState
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.{FakeRequest, WsTestClient}

class ApplicationAlreadySubmittedControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/business-already-has-application-submitted"

  "GET " + normalRoutePath - {
    "should return OK and render the ApplicationAlreadySubmitted page" in {
      build
        .commonPrecondition

      setAnswers(emptyUserAnswers.copy(registerState = RegisterState.RegisterApplicationAccepted))

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          
          given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
          given messages: Messages = messagesApi.preferred(FakeRequest())

          page.title must include(messages("applicationAlreadySubmitted.heading.title"))
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }
}
