package controllers

import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.i18n.Messages
import play.api.test.WsTestClient

class AlreadyRegisteredControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/application-already-registered"

  "GET " + normalRoutePath - {
    "should return OK and render the AlreadyRegistered page" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswers)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(Messages("alreadyRegistered.heading.title"))
          page.getElementsByClass("govuk-heading-m").text() mustEqual Messages("alreadyRegistered.heading.title")
          page.getElementById("subheader").text() mustEqual s"These are the details we hold for Unique Taxpayer Reference (UTR) 0000001611:"
          page.getElementById("utrField").text() mustEqual "0000001611:"
          page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove Guildford GU14 8NL"
          page.getElementById("account-redirect").text() mustBe "To view your registration details, go to your Soft Drinks Industry Levy account."
          page.getElementById("account-link").attr("href") mustBe routes.IndexController.onPageLoad().url
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, Messages("alreadyRegistered.heading.title"
    ) )
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }
}
