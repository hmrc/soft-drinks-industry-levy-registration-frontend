package controllers

import models.HowManyLitresGlobally.{Large, Small}
import models.OrganisationType.LimitedCompany
import models.{NormalMode, Verify}
import models.Verify.YesRegister
import models.backend.UkAddress
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages._
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient


class CheckYourAnswersControllerISpec extends RegSummaryISpecHelper {

  val route = "/check-your-answers"


  "GET " + routes.CheckYourAnswersController.onPageLoad.url - {
    "when the userAnswers contains no data" - {
      "should redirect to verify controller" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.VerifyController.onPageLoad(NormalMode).url)
          }
        }
      }
    }
    "when the userAnswers contains data" - {
      "should render the page with answers of yes for every liters page, start date and contact details" in {
        given
          .commonPrecondition

        setAnswers(userAnswersWithLitres)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("checkYourAnswers.title"))
            page.getElementsByClass("govuk-summary-list").size() mustBe 6

            val businessDetails = page.getElementsByClass("govuk-summary-list").first()
            page.getElementsByTag("h2").first().text() mustBe "Business details"
            validateBusinessDetailsSummaryList(businessDetails, "0000001611", rosmAddress, "1 million litres or more", true)

            val operatePackagingSites = page.getElementsByClass("govuk-summary-list").get(1)
            page.getElementsByTag("h2").get(1).text() mustBe "Own brands packaged at your own site"
            validateOperatePackagingSitesWithLitresSummaryList(operatePackagingSites, operatePackagingSiteLitres, true)

            val contractPacking = page.getElementsByClass("govuk-summary-list").get(2)

            page.getElementsByTag("h2").get(2).text() mustBe "Contract packed at your own site"
            validateContractPackingWithLitresSummaryList(contractPacking, contractPackingLitres, true)

            val imports = page.getElementsByClass("govuk-summary-list").get(3)
            page.getElementsByTag("h2").get(3).text() mustBe "Brought into the UK"
            validateImportsWithLitresSummaryList(imports, importsLitres, true)

            val startDateSummaryListItem = page.getElementsByClass("govuk-summary-list").get(4)
            page.getElementsByTag("h2").get(4).text() mustBe "Soft Drinks Industry Levy liability date"
            validateStartDateSummaryList(startDateSummaryListItem, startDate, true)

            val contactDetailsSummaryListItem = page.getElementsByClass("govuk-summary-list").get(5)
            page.getElementsByTag("h2").get(5).text() mustBe "Contact person details"
            validateContactDetailsSummaryList(contactDetailsSummaryListItem, contactDetails, true)

            page.getElementsByTag("form").first().attr("action") mustBe routes.CheckYourAnswersController.onSubmit.url
            page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and apply"
          }
        }
      }
      "should render the page with answers of no for every liters page, the start date and contact details" in {
        given
          .commonPrecondition

        setAnswers(userAnswersWithAllNo)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("checkYourAnswers.title"))
            page.getElementsByClass("govuk-summary-list").size() mustBe 6

            val businessDetails = page.getElementsByClass("govuk-summary-list").first()
            page.getElementsByTag("h2").first().text() mustBe "Business details"
            validateBusinessDetailsSummaryList(businessDetails, "0000001611", newAddress, "Less than 1 million litres", true)

            val operatePackagingSites = page.getElementsByClass("govuk-summary-list").get(1)
            page.getElementsByTag("h2").get(1).text() mustBe "Own brands packaged at your own site"
            validateOperatePackagingSitesWithNoLitresSummaryList(operatePackagingSites, true)

            val contractPacking = page.getElementsByClass("govuk-summary-list").get(2)

            page.getElementsByTag("h2").get(2).text() mustBe "Contract packed at your own site"
            validateContractPackingWithNoLitresSummaryList(contractPacking, true)

            val imports = page.getElementsByClass("govuk-summary-list").get(3)
            page.getElementsByTag("h2").get(3).text() mustBe "Brought into the UK"
            validateImportsWithNoLitresSummaryList(imports, true)

            val startDateSummaryListItem = page.getElementsByClass("govuk-summary-list").get(4)
            page.getElementsByTag("h2").get(4).text() mustBe "Soft Drinks Industry Levy liability date"
            validateStartDateSummaryList(startDateSummaryListItem, startDate, true)

            val contactDetailsSummaryListItem = page.getElementsByClass("govuk-summary-list").get(5)
            page.getElementsByTag("h2").get(5).text() mustBe "Contact person details"
            validateContactDetailsSummaryList(contactDetailsSummaryListItem, contactDetails, true)

            page.getElementsByTag("form").first().attr("action") mustBe routes.CheckYourAnswersController.onSubmit.url
            page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and apply"
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + route, Messages("checkYourAnswers.title"), fullExampleUserAnswers)
    testUnauthorisedUser(baseUrl + route)
    testAuthenticatedUserButNoUserAnswers(baseUrl + route)
  }

  "POST " + routes.CheckYourAnswersController.onPageLoad.url - {
    "should redirect to verify controller when user answers empty" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswers)

      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(client, baseUrl + route, Json.obj())

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.VerifyController.onPageLoad(NormalMode).url)
        }
      }
    }
    "should submit successfully and redirect to the next page when user answers full" in {
      given
        .commonPrecondition

      val userAnswers = {
        emptyUserAnswers
          .set(VerifyPage, YesRegister).success.value
          .set(OrganisationTypePage, LimitedCompany).success.value
          .set(HowManyLitresGloballyPage, Large).success.value
          .set(OperatePackagingSitesPage, true).success.value
          .set(HowManyOperatePackagingSitesPage, operatePackagingSiteLitres).success.value
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, contractPackingLitres).success.value
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, importsLitres).success.value
          .set(StartDatePage, startDate).success.value
          .set(PackAtBusinessAddressPage, true).success.value
          .set(PackagingSiteDetailsPage, true).success.value
          .set(AskSecondaryWarehousesPage, true).success.value
          .set(WarehouseDetailsPage, true).success.value
          .set(ContactDetailsPage, contactDetails).success.value
      }
      setAnswers(userAnswers)

      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(client, baseUrl + route, Json.obj())

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.RegistrationConfirmationController.onPageLoad.url)
        }
      }
    }
    testUnauthorisedUser(baseUrl + route, optJson = Some(Json.obj()))
    testAuthenticatedUserButNoUserAnswers(baseUrl + route, optJson = Some(Json.obj()))
  }
}
