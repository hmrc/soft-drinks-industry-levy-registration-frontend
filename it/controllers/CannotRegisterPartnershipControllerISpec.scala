package controllers

import models.UserAnswers
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.{ContactDetailsPage, HowManyLitresGloballyPage, StartDatePage}
import play.api.i18n.Messages
import play.api.test.WsTestClient

class CannotRegisterPartnershipControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/cannot-register-partnership"

  "GET " + normalRoutePath - {
    "should return OK and render the CannotRegisterPartnership page" in {
      given.commonPrecondition

      val userAnswers = emptyUserAnswers.set(StartDatePage, date).success.value
      setAnswers(userAnswers)



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
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, Messages("cannotRegisterPartnership" + ".title"))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }
}
