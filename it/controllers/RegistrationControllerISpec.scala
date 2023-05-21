package controllers

import play.api.http.HeaderNames
import play.api.test.WsTestClient
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, defined, include}

class RegistrationControllerISpec extends ControllerITTestHelper {

  val path = "/start"

  s"GET $path" - {
    "when the user meets the requirements for registration" - {
      "and has a IR-CT enrolment" - {
        "should create a database record and redirect to the ???" in {
          given
            .commonPrecondition

          remove(identifier)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad.url)
              getAnswers(identifier) mustBe defined
            }
          }
        }
      }

      "and does not have a IR-CT enrolment" - {
        "should create a database record and redirect to the ???" in {
          given
            .authorisedButNoEnrolmentsPrecondition

          remove(identifier)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad.url)
              getAnswers(identifier) mustBe defined
            }
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + path)
  }

}
