package controllers

import models.RegisterState
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.WsTestClient
import testSupport.preConditions.PreconditionHelpers
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ApplicationAlreadySubmittedControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/business-already-has-application-submitted"
  override val preconditionHelpers: PreconditionHelpers = mock[PreconditionHelpers]

  "GET " + normalRoutePath - {
    "should return OK and render the ApplicationAlreadySubmitted page" in {
      preconditionHelpers.commonPrecondition

      setAnswers(emptyUserAnswers.copy(registerState = RegisterState.RegisterApplicationAccepted))

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          
          given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
          given Messages = messagesApi.preferred(FakeRequest())

          page.title must include(Messages("applicationAlreadySubmitted.heading.title"))
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }
}
