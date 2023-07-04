package controllers

import models.{CheckMode, LitresInBands}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.{ContractPackingPage, HowManyContractPackingPage, HowManyImportsPage, HowManyOperatePackagingSitesPage, ImportsPage, OperatePackagingSitesPage}
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class CheckYourAnswersControllerISpec extends ControllerITTestHelper {

  val route = "/check-your-answers"
  "GET " + routes.CheckYourAnswersController.onPageLoad().url - {
    "when the userAnswers contains no data" - {
      "should render the page" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("checkYourAnswers.title"))
            page.getElementsByClass("govuk-summary-list").size() mustBe 0
          }
        }
      }
    }
    "when the userAnswers contains data" - {
      "should render the page with answers" in {
        given
          .commonPrecondition

        val userAnswers = emptyUserAnswers
          .set(OperatePackagingSitesPage, true).success.value
          .set(HowManyOperatePackagingSitesPage, LitresInBands(1000, 2000)).success.value
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(3000, 4000)).success.value
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, LitresInBands(5000, 6000)).success.value

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("checkYourAnswers.title"))
            page.getElementsByClass("govuk-summary-list").size() mustBe 3
            val operatePackagingSites = page.getElementsByClass("govuk-summary-list").first().getElementsByClass("govuk-summary-list__row")

            page.getElementsByTag("h2").first().text() mustBe "Own brands packaged at your own site"
            operatePackagingSites.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"
            operatePackagingSites.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change do you operate any packaging sites in the UK to package liable drinks for the brands you own?"
            operatePackagingSites.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "do you operate any packaging sites in the UK to package liable drinks for the brands you own?"
            operatePackagingSites.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.OperatePackagingSitesController.onPageLoad(CheckMode).url

            operatePackagingSites.get(1).getElementsByClass("govuk-summary-list__value").first().text() mustBe "1,000"
            operatePackagingSites.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change amount of litres in low band for own brands packaged at your own site"
            operatePackagingSites.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "amount of litres in low band for own brands packaged at your own site"
            operatePackagingSites.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyOperatePackagingSitesController.onPageLoad(CheckMode).url

            operatePackagingSites.get(2).getElementsByClass("govuk-summary-list__value").first().text() mustBe "2,000"
            operatePackagingSites.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change amount of litres in high band for own brands packaged at your own site"
            operatePackagingSites.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "amount of litres in high band for own brands packaged at your own site"
            operatePackagingSites.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyOperatePackagingSitesController.onPageLoad(CheckMode).url

            val contractPacking = page.getElementsByClass("govuk-summary-list").get(1).getElementsByClass("govuk-summary-list__row")

            page.getElementsByTag("h2").get(1).text() mustBe "Contract packed at your own site"
            contractPacking.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"
            contractPacking.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change do you operate any packaging sites in the UK to package liable drinks as a third party or contract packer?"
            contractPacking.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "do you operate any packaging sites in the UK to package liable drinks as a third party or contract packer?"
            contractPacking.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.ContractPackingController.onPageLoad(CheckMode).url

            contractPacking.get(1).getElementsByClass("govuk-summary-list__value").first().text() mustBe "3,000"
            contractPacking.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change amount of litres in low band for contract packed at your own site"
            contractPacking.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "amount of litres in low band for contract packed at your own site"
            contractPacking.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyContractPackingController.onPageLoad(CheckMode).url

            contractPacking.get(2).getElementsByClass("govuk-summary-list__value").first().text() mustBe "4,000"
            contractPacking.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change amount of litres in high band for contract packed at your own site"
            contractPacking.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "amount of litres in high band for contract packed at your own site"
            contractPacking.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyContractPackingController.onPageLoad(CheckMode).url

            val imports = page.getElementsByClass("govuk-summary-list").get(2).getElementsByClass("govuk-summary-list__row")

            page.getElementsByTag("h2").get(2).text() mustBe "Brought into the UK"
            imports.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"
            imports.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change do you bring liable drinks into the UK from anywhere outside of the UK?"
            imports.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "do you bring liable drinks into the UK from anywhere outside of the UK?"
            imports.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.ImportsController.onPageLoad(CheckMode).url

            imports.get(1).getElementsByClass("govuk-summary-list__value").first().text() mustBe "5,000"
            imports.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change amount of litres in low band for brought into the UK"
            imports.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "amount of litres in low band for brought into the UK"
            imports.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyImportsController.onPageLoad(CheckMode).url

            imports.get(2).getElementsByClass("govuk-summary-list__value").first().text() mustBe "6,000"
            imports.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change amount of litres in high band for brought into the UK"
            imports.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "amount of litres in high band for brought into the UK"
            imports.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyImportsController.onPageLoad(CheckMode).url

            page.getElementsByTag("form").first().attr("action") mustBe routes.CheckYourAnswersController.onSubmit().url
            page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and apply"
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + route, Messages("checkYourAnswers.title"))
    testUnauthorisedUser(baseUrl + route)
    testAuthenticatedUserButNoUserAnswers(baseUrl + route)
  }

  "POST " + routes.CheckYourAnswersController.onPageLoad().url - {
    "should submit successfully and redirect to the next page" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswers)

      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(client, baseUrl + route, Json.obj())

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad().url)
        }
      }
    }
    testUnauthorisedUser(baseUrl + route, optJson = Some(Json.obj()))
    testAuthenticatedUserButNoUserAnswers(baseUrl + route, optJson = Some(Json.obj()))

  }
}
