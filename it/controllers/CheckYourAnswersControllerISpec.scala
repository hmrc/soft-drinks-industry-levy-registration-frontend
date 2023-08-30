package controllers

import models.HowManyLitresGlobally.{Large, Small}
import models.OrganisationType.LimitedCompany
import models.Verify.YesRegister
import models.{CheckMode, HowManyLitresGlobally}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.include
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
            res.header(HeaderNames.LOCATION) mustBe Some(routes.VerifyController.onPageLoad(CheckMode).url)
          }
        }
      }
    }


    s"when the user has selected they are a Large producer type" - {
      "and they have populated all pages including litres" - {
        "should render the check your answers page with only the required details" in {
          val userAnswers = userAnswerWithLitresForAllPagesIncludingOnesNotRequired(Large)
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("checkYourAnswers.title"))
              page.getElementsByClass("govuk-summary-list").size() mustBe 7

              val businessDetails = page.getElementsByClass("govuk-summary-list").first()
              page.getElementsByTag("h2").first().text() mustBe "Business details"
              validateBusinessDetailsSummaryList(businessDetails, "0000001611", rosmAddress, Large, true)

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

              val packingSiteDetailsSummaryListItem = page.getElementsByClass("govuk-summary-list").get(6)
              page.getElementsByTag("h2").get(6).text() mustBe "UK site details"
              validatePackingSiteDetailsSummary(packingSiteDetailsSummaryListItem, true)

              page.getElementsByTag("form").first().attr("action") mustBe routes.CheckYourAnswersController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and apply"
            }
          }
        }
      }

      "and they have only populated the required pages and have no litres" - {
        "should render the check your answers page with expected summary items" in {
          val userAnswers = userAnswerWithAllNoAndNoPagesToFilterOut(Large)
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("checkYourAnswers.title"))
              page.getElementsByClass("govuk-summary-list").size() mustBe 6

              val businessDetails = page.getElementsByClass("govuk-summary-list").first()
              page.getElementsByTag("h2").first().text() mustBe "Business details"
              validateBusinessDetailsSummaryList(businessDetails, "0000001611", newAddress, Large, true)

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
    }

    s"when the user has selected they are a Small producer type" - {
      "and they have populated all pages including litres" - {
        "should render the check your answers page with only the required details" in {
          val userAnswers = userAnswerWithLitresForAllPagesIncludingOnesNotRequired(Small)
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("checkYourAnswers.title"))
              page.getElementsByClass("govuk-summary-list").size() mustBe 8

              val businessDetails = page.getElementsByClass("govuk-summary-list").first()
              page.getElementsByTag("h2").first().text() mustBe "Business details"
              validateBusinessDetailsSummaryList(businessDetails, "0000001611", rosmAddress, Small, true)

              val copackee = page.getElementsByClass("govuk-summary-list").get(1)
              page.getElementsByTag("h2").get(1).text() mustBe "Use third party packagers"
              validateThirdPartyPackersSummaryList(copackee, true)

              val operatePackagingSites = page.getElementsByClass("govuk-summary-list").get(2)
              page.getElementsByTag("h2").get(2).text() mustBe "Own brands packaged at your own site"
              validateOperatePackagingSitesWithLitresSummaryList(operatePackagingSites, operatePackagingSiteLitres, true)

              val contractPacking = page.getElementsByClass("govuk-summary-list").get(3)
              page.getElementsByTag("h2").get(3).text() mustBe "Contract packed at your own site"
              validateContractPackingWithLitresSummaryList(contractPacking, contractPackingLitres, true)

              val imports = page.getElementsByClass("govuk-summary-list").get(4)
              page.getElementsByTag("h2").get(4).text() mustBe "Brought into the UK"
              validateImportsWithLitresSummaryList(imports, importsLitres, true)

              val startDateSummaryListItem = page.getElementsByClass("govuk-summary-list").get(5)
              page.getElementsByTag("h2").get(5).text() mustBe "Soft Drinks Industry Levy liability date"
              validateStartDateSummaryList(startDateSummaryListItem, startDate, true)

              val contactDetailsSummaryListItem = page.getElementsByClass("govuk-summary-list").get(6)
              page.getElementsByTag("h2").get(6).text() mustBe "Contact person details"
              validateContactDetailsSummaryList(contactDetailsSummaryListItem, contactDetails, true)

              val packingSiteDetailsSummaryListItem = page.getElementsByClass("govuk-summary-list").get(7)
              page.getElementsByTag("h2").get(7).text() mustBe "UK site details"
              validatePackingSiteDetailsSummary(packingSiteDetailsSummaryListItem, true)

              page.getElementsByTag("form").first().attr("action") mustBe routes.CheckYourAnswersController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and apply"
            }
          }
        }
      }

      "and they have only populated the required pages and have no litres" - {
        "should render the check your answers page with expected summary items" in {
          val userAnswers = userAnswerWithAllNoAndNoPagesToFilterOut(Small)
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("checkYourAnswers.title"))
              page.getElementsByClass("govuk-summary-list").size() mustBe 6

              val businessDetails = page.getElementsByClass("govuk-summary-list").first()
              page.getElementsByTag("h2").first().text() mustBe "Business details"
              validateBusinessDetailsSummaryList(businessDetails, "0000001611", newAddress, HowManyLitresGlobally.Small, true)

              val copackee = page.getElementsByClass("govuk-summary-list").get(1)
              page.getElementsByTag("h2").get(1).text() mustBe "Use third party packagers"
              validateThirdPartyPackersSummaryList(copackee, true)

              val operatePackagingSites = page.getElementsByClass("govuk-summary-list").get(2)
              page.getElementsByTag("h2").get(2).text() mustBe "Own brands packaged at your own site"
              validateOperatePackagingSitesWithNoLitresSummaryList(operatePackagingSites, true)

              val contractPacking = page.getElementsByClass("govuk-summary-list").get(3)
              page.getElementsByTag("h2").get(3).text() mustBe "Contract packed at your own site"
              validateContractPackingWithNoLitresSummaryList(contractPacking, true)

              val imports = page.getElementsByClass("govuk-summary-list").get(4)
              page.getElementsByTag("h2").get(4).text() mustBe "Brought into the UK"
              validateImportsWithNoLitresSummaryList(imports, true)


              val contactDetailsSummaryListItem = page.getElementsByClass("govuk-summary-list").get(5)
              page.getElementsByTag("h2").get(5).text() mustBe "Contact person details"
              validateContactDetailsSummaryList(contactDetailsSummaryListItem, contactDetails, true)

              page.getElementsByTag("form").first().attr("action") mustBe routes.CheckYourAnswersController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and apply"
            }
          }
        }
      }
    }

    s"when the user has selected they are a None producer type" - {
      "and they have populated all pages including litres" - {
        "should render the check your answers page with only the required details" in {
          val userAnswers = userAnswerWithLitresForAllPagesIncludingOnesNotRequired(HowManyLitresGlobally.None)
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("checkYourAnswers.title"))
              page.getElementsByClass("govuk-summary-list").size() mustBe 6

              val businessDetails = page.getElementsByClass("govuk-summary-list").first()
              page.getElementsByTag("h2").first().text() mustBe "Business details"
              validateBusinessDetailsSummaryList(businessDetails, "0000001611", rosmAddress, HowManyLitresGlobally.None, true)

              val contractPacking = page.getElementsByClass("govuk-summary-list").get(1)
              page.getElementsByTag("h2").get(1).text() mustBe "Contract packed at your own site"
              validateContractPackingWithLitresSummaryList(contractPacking, contractPackingLitres, true)

              val imports = page.getElementsByClass("govuk-summary-list").get(2)
              page.getElementsByTag("h2").get(2).text() mustBe "Brought into the UK"
              validateImportsWithLitresSummaryList(imports, importsLitres, true)

              val startDateSummaryListItem = page.getElementsByClass("govuk-summary-list").get(3)
              page.getElementsByTag("h2").get(3).text() mustBe "Soft Drinks Industry Levy liability date"
              validateStartDateSummaryList(startDateSummaryListItem, startDate, true)

              val contactDetailsSummaryListItem = page.getElementsByClass("govuk-summary-list").get(4)
              page.getElementsByTag("h2").get(4).text() mustBe "Contact person details"
              validateContactDetailsSummaryList(contactDetailsSummaryListItem, contactDetails, true)

              val packingSiteDetailsSummaryListItem = page.getElementsByClass("govuk-summary-list").get(5)
              page.getElementsByTag("h2").get(5).text() mustBe "UK site details"
              validatePackingSiteDetailsSummary(packingSiteDetailsSummaryListItem, true)

              page.getElementsByTag("form").first().attr("action") mustBe routes.CheckYourAnswersController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and apply"
            }
          }
        }
      }

      "and they have only populated the required pages and have no litres" - {
        "should render the check your answers page with expected summary items" in {
          val userAnswers = userAnswerWithAllNoAndNoPagesToFilterOut(HowManyLitresGlobally.None)
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + route)

            whenReady(result) { res =>
              res.status mustBe OK
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("checkYourAnswers.title"))
              page.getElementsByClass("govuk-summary-list").size() mustBe 5

              val businessDetails = page.getElementsByClass("govuk-summary-list").first()
              page.getElementsByTag("h2").first().text() mustBe "Business details"
              validateBusinessDetailsSummaryList(businessDetails, "0000001611", newAddress, HowManyLitresGlobally.None, true)

              val contractPacking = page.getElementsByClass("govuk-summary-list").get(1)
              page.getElementsByTag("h2").get(1).text() mustBe "Contract packed at your own site"
              validateContractPackingWithNoLitresSummaryList(contractPacking, true)

              val imports = page.getElementsByClass("govuk-summary-list").get(2)
              page.getElementsByTag("h2").get(2).text() mustBe "Brought into the UK"
              validateImportsWithNoLitresSummaryList(imports, true)

              val startDateSummaryListItem = page.getElementsByClass("govuk-summary-list").get(3)
              page.getElementsByTag("h2").get(3).text() mustBe "Soft Drinks Industry Levy liability date"
              validateStartDateSummaryList(startDateSummaryListItem, startDate, true)

              val contactDetailsSummaryListItem = page.getElementsByClass("govuk-summary-list").get(4)
              page.getElementsByTag("h2").get(4).text() mustBe "Contact person details"
              validateContactDetailsSummaryList(contactDetailsSummaryListItem, contactDetails, true)

              page.getElementsByTag("form").first().attr("action") mustBe routes.CheckYourAnswersController.onSubmit.url
              page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm details and apply"
            }
          }
        }
      }
    }

    testOtherSuccessUserTypes(baseUrl + route, Messages("checkYourAnswers.title"), fullExampleUserAnswers)
    testUnauthorisedUser(baseUrl + route)
    testWhoIsUnableToRegisterWithGivenUtr(baseUrl + route)
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
          res.header(HeaderNames.LOCATION) mustBe Some(routes.VerifyController.onPageLoad(CheckMode).url)
        }
      }
    }

    "should create a subscription and send to back end then redirect to the next page" - {
      "when all required user answers are present" in {
        given
          .commonPrecondition
          .sdilBackend.createSubscription("0000001611")

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
    }

    "should render the error page" - {
      "when the sending of the created subscription fails" in {
        given
          .commonPrecondition
          .sdilBackend.createSubscriptionError("0000001611")

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
            res.status mustBe 500
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + route, optJson = Some(Json.obj()))
    testWhoIsUnableToRegisterWithGivenUtr(baseUrl + route, optJson = Some(Json.obj()))
    testAuthenticatedUserButNoUserAnswers(baseUrl + route, optJson = Some(Json.obj()))
  }
}
