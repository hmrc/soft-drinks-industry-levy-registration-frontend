package controllers

import models.HowManyLitresGlobally.{Large, Small}
import models.backend.Subscription
import models.{CreatedSubscriptionAndAmountProducedGlobally, HowManyLitresGlobally, RosmWithUtr}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import play.api.http.HeaderNames
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.{FakeRequest, WsTestClient}
import repositories.SDILSessionKeys

class RegistrationConfirmationControllerISpec extends RegSummaryISpecHelper {

  val path = "/registration-confirmation"

  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())

  "GET " + path - {
    "when the user has submitted a registration request" - {
      "should return OK and render the RegistrationConfirmation page" - {
        "with the expected content" - {
          s"when the user has selected they are a Large producer type" - {
            "and they have populated all pages including litres" in {
              val userAnswers = userAnswerWithLitresForAllPagesIncludingOnesNotRequired(HowManyLitresGlobally.Large)
                .copy(submittedOn = Some(submittedDate))
              val createdSubscriptionAndAmountProducedGlobally = CreatedSubscriptionAndAmountProducedGlobally(
                Subscription.generate(userAnswers, RosmWithUtr("0000001611", rosmRegistration)), HowManyLitresGlobally.Large)
              build
                .commonPrecondition

              setAnswers(userAnswers)
              addToCache(SDILSessionKeys.CREATED_SUBSCRIPTION_AND_AMOUNT_PRODUCED_GLOBALLY, createdSubscriptionAndAmountProducedGlobally)

              WsTestClient.withClient { client =>
                val result = createClientRequestGet(client, baseUrl + path)

                whenReady(result) { res =>
                  res.status mustBe OK
                  val page = Jsoup.parse(res.body)
                  page.title must include(messages("Application complete"))
                  validateSummaryContent(page)
                  val detailsSection = page.getElementsByClass("govuk-details").get(0)
                  detailsSection.getElementsByClass("govuk-summary-list").size() mustBe 7

                  val businessDetails = detailsSection.getElementsByClass("govuk-summary-list").first()
                  detailsSection.getElementsByTag("h2").first().text() mustBe "Business details"
                  validateBusinessDetailsSummaryList(businessDetails, "0000001611", rosmAddress, Large, false)

                  val operatePackagingSites = detailsSection.getElementsByClass("govuk-summary-list").get(1)
                  detailsSection.getElementsByTag("h2").get(1).text() mustBe "Own brands packaged at your own site"
                  validateOperatePackagingSitesWithLitresSummaryList(operatePackagingSites, operatePackagingSiteLitres, false)

                  val contractPacking = detailsSection.getElementsByClass("govuk-summary-list").get(2)

                  detailsSection.getElementsByTag("h2").get(2).text() mustBe "Contract packed at your own site"
                  validateContractPackingWithLitresSummaryList(contractPacking, contractPackingLitres, false)

                  val imports = detailsSection.getElementsByClass("govuk-summary-list").get(3)
                  detailsSection.getElementsByTag("h2").get(3).text() mustBe "Brought into the UK"
                  validateImportsWithLitresSummaryList(imports, importsLitres, false)

                  val startDateSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(4)
                  detailsSection.getElementsByTag("h2").get(4).text() mustBe "Soft Drinks Industry Levy liability date"
                  validateStartDateSummaryList(startDateSummaryListItem, startDate, false)

                  val contactDetailsSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(5)
                  detailsSection.getElementsByTag("h2").get(5).text() mustBe "Contact person details"
                  validateContactDetailsSummaryList(contactDetailsSummaryListItem, contactDetails, false)

                  val siteDetailsSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(6)
                  detailsSection.getElementsByTag("h2").get(6).text() mustBe "UK site details"
                  validateSiteDetailsSummary(siteDetailsSummaryListItem, 3, 1, false)

                }
              }
            }

            "and they have only populated the required pages and have no litres" in {
              val userAnswers = userAnswerWithAllNoAndNoPagesToFilterOut(HowManyLitresGlobally.Large)
                .copy(submittedOn = Some(submittedDate))
              val createdSubscriptionAndAmountProducedGlobally = CreatedSubscriptionAndAmountProducedGlobally(
                Subscription.generate(userAnswers, RosmWithUtr("0000001611", rosmRegistration)), HowManyLitresGlobally.Large)
              build
                .commonPrecondition

              setAnswers(userAnswers)
              addToCache(SDILSessionKeys.CREATED_SUBSCRIPTION_AND_AMOUNT_PRODUCED_GLOBALLY, createdSubscriptionAndAmountProducedGlobally)

              WsTestClient.withClient { client =>
                val result = createClientRequestGet(client, baseUrl + path)

                whenReady(result) { res =>
                  res.status mustBe OK
                  val page = Jsoup.parse(res.body)
                  page.title must include(messages("Application complete"))
                  validateSummaryContent(page)
                  val detailsSection = page.getElementsByClass("govuk-details").get(0)
                  detailsSection.getElementsByClass("govuk-summary-list").size() mustBe 7

                  val businessDetails = detailsSection.getElementsByClass("govuk-summary-list").first()
                  detailsSection.getElementsByTag("h2").first().text() mustBe "Business details"
                  validateBusinessDetailsSummaryList(businessDetails, "0000001611", newAddress, Large, false)

                  val operatePackagingSites = detailsSection.getElementsByClass("govuk-summary-list").get(1)
                  detailsSection.getElementsByTag("h2").get(1).text() mustBe "Own brands packaged at your own site"
                  validateOperatePackagingSitesWithNoLitresSummaryList(operatePackagingSites, false)

                  val contractPacking = detailsSection.getElementsByClass("govuk-summary-list").get(2)

                  detailsSection.getElementsByTag("h2").get(2).text() mustBe "Contract packed at your own site"
                  validateContractPackingWithNoLitresSummaryList(contractPacking, false)

                  val imports = detailsSection.getElementsByClass("govuk-summary-list").get(3)
                  detailsSection.getElementsByTag("h2").get(3).text() mustBe "Brought into the UK"
                  validateImportsWithNoLitresSummaryList(imports, false)

                  val startDateSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(4)
                  detailsSection.getElementsByTag("h2").get(4).text() mustBe "Soft Drinks Industry Levy liability date"
                  validateStartDateSummaryList(startDateSummaryListItem, startDate, false)

                  val contactDetailsSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(5)
                  detailsSection.getElementsByTag("h2").get(5).text() mustBe "Contact person details"
                  validateContactDetailsSummaryList(contactDetailsSummaryListItem, contactDetails, false)

                  val siteDetailsSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(6)
                  detailsSection.getElementsByTag("h2").get(6).text() mustBe "UK site details"
                  validateSiteDetailsSummary(siteDetailsSummaryListItem, 0, 0, false)                }
              }
            }
          }

          s"when the user has selected they are a Small producer type" - {
            "and they have populated all pages including litres" in {
              val userAnswers = userAnswerWithLitresForAllPagesIncludingOnesNotRequired(HowManyLitresGlobally.Small)
                .copy(submittedOn = Some(submittedDate))
              val createdSubscriptionAndAmountProducedGlobally = CreatedSubscriptionAndAmountProducedGlobally(
                Subscription.generate(userAnswers, RosmWithUtr("0000001611", rosmRegistration)), HowManyLitresGlobally.Small)
              build
                .commonPrecondition

              setAnswers(userAnswers)
              addToCache(SDILSessionKeys.CREATED_SUBSCRIPTION_AND_AMOUNT_PRODUCED_GLOBALLY, createdSubscriptionAndAmountProducedGlobally)

              WsTestClient.withClient { client =>
                val result = createClientRequestGet(client, baseUrl + path)

                whenReady(result) { res =>
                  res.status mustBe OK
                  val page = Jsoup.parse(res.body)
                  page.title must include(messages("Application complete"))
                  validateSummaryContent(page)
                  val detailsSection = page.getElementsByClass("govuk-details").get(0)
                  detailsSection.getElementsByClass("govuk-summary-list").size() mustBe 8

                  val businessDetails = detailsSection.getElementsByClass("govuk-summary-list").first()
                  detailsSection.getElementsByTag("h2").first().text() mustBe "Business details"
                  validateBusinessDetailsSummaryList(businessDetails, "0000001611", rosmAddress, Small, false)

                  val copackee = detailsSection.getElementsByClass("govuk-summary-list").get(1)
                  detailsSection.getElementsByTag("h2").get(1).text() mustBe "Use third party packagers"
                  validateThirdPartyPackersSummaryList(copackee, false)

                  val operatePackagingSites = detailsSection.getElementsByClass("govuk-summary-list").get(2)
                  detailsSection.getElementsByTag("h2").get(2).text() mustBe "Own brands packaged at your own site"
                  validateOperatePackagingSitesWithLitresSummaryList(operatePackagingSites, operatePackagingSiteLitres, false)

                  val contractPacking = detailsSection.getElementsByClass("govuk-summary-list").get(3)
                  detailsSection.getElementsByTag("h2").get(3).text() mustBe "Contract packed at your own site"
                  validateContractPackingWithLitresSummaryList(contractPacking, contractPackingLitres, false)

                  val imports = detailsSection.getElementsByClass("govuk-summary-list").get(4)
                  detailsSection.getElementsByTag("h2").get(4).text() mustBe "Brought into the UK"
                  validateImportsWithLitresSummaryList(imports, importsLitres, false)

                  val startDateSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(5)
                  detailsSection.getElementsByTag("h2").get(5).text() mustBe "Soft Drinks Industry Levy liability date"
                  validateStartDateSummaryList(startDateSummaryListItem, startDate, false)

                  val contactDetailsSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(6)
                  detailsSection.getElementsByTag("h2").get(6).text() mustBe "Contact person details"
                  validateContactDetailsSummaryList(contactDetailsSummaryListItem, contactDetails, false)

                  val siteDetailsSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(7)
                  detailsSection.getElementsByTag("h2").get(7).text() mustBe "UK site details"
                  validateSiteDetailsSummary(siteDetailsSummaryListItem, 3, 1, false)
                }
              }
            }

            "and they have only populated the required pages and have no litres" in {
              val userAnswers = userAnswerWithAllNoAndNoPagesToFilterOut(HowManyLitresGlobally.Small)
                .copy(submittedOn = Some(submittedDate))
              val createdSubscriptionAndAmountProducedGlobally = CreatedSubscriptionAndAmountProducedGlobally(
                Subscription.generate(userAnswers, RosmWithUtr("0000001611", rosmRegistration)), HowManyLitresGlobally.Small)
              build
                .commonPrecondition

              setAnswers(userAnswers)
              addToCache(SDILSessionKeys.CREATED_SUBSCRIPTION_AND_AMOUNT_PRODUCED_GLOBALLY, createdSubscriptionAndAmountProducedGlobally)

              WsTestClient.withClient { client =>
                val result = createClientRequestGet(client, baseUrl + path)

                whenReady(result) { res =>
                  res.status mustBe OK
                  val page = Jsoup.parse(res.body)
                  page.title must include(messages("Application complete"))
                  validateSummaryContent(page)
                  val detailsSection = page.getElementsByClass("govuk-details").get(0)
                  detailsSection.getElementsByClass("govuk-summary-list").size() mustBe 6

                  val businessDetails = detailsSection.getElementsByClass("govuk-summary-list").first()
                  detailsSection.getElementsByTag("h2").first().text() mustBe "Business details"
                  validateBusinessDetailsSummaryList(businessDetails, "0000001611", newAddress, HowManyLitresGlobally.Small, false)

                  val copackee = detailsSection.getElementsByClass("govuk-summary-list").get(1)
                  detailsSection.getElementsByTag("h2").get(1).text() mustBe "Use third party packagers"
                  validateThirdPartyPackersSummaryList(copackee, false)

                  val operatePackagingSites = detailsSection.getElementsByClass("govuk-summary-list").get(2)
                  detailsSection.getElementsByTag("h2").get(2).text() mustBe "Own brands packaged at your own site"
                  validateOperatePackagingSitesWithNoLitresSummaryList(operatePackagingSites, false)

                  val contractPacking = detailsSection.getElementsByClass("govuk-summary-list").get(3)
                  detailsSection.getElementsByTag("h2").get(3).text() mustBe "Contract packed at your own site"
                  validateContractPackingWithNoLitresSummaryList(contractPacking, false)

                  val imports = detailsSection.getElementsByClass("govuk-summary-list").get(4)
                  detailsSection.getElementsByTag("h2").get(4).text() mustBe "Brought into the UK"
                  validateImportsWithNoLitresSummaryList(imports, false)

                  val contactDetailsSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(5)
                  detailsSection.getElementsByTag("h2").get(5).text() mustBe "Contact person details"
                  validateContactDetailsSummaryList(contactDetailsSummaryListItem, contactDetails, false)

                  detailsSection.getElementsByTag("h2").eachText() mustNot contain("UK site details")
                }
              }
            }
          }

          s"when the user has selected they are a None producer type" - {
            "and they have populated all pages including litres" in {
              val userAnswers = userAnswerWithLitresForAllPagesIncludingOnesNotRequired(HowManyLitresGlobally.None)
                .copy(submittedOn = Some(submittedDate))
              val createdSubscriptionAndAmountProducedGlobally = CreatedSubscriptionAndAmountProducedGlobally(
                Subscription.generate(userAnswers, RosmWithUtr("0000001611", rosmRegistration)), HowManyLitresGlobally.None)
              build
                .commonPrecondition

              setAnswers(userAnswers)
              addToCache(SDILSessionKeys.CREATED_SUBSCRIPTION_AND_AMOUNT_PRODUCED_GLOBALLY, createdSubscriptionAndAmountProducedGlobally)

              WsTestClient.withClient { client =>
                val result = createClientRequestGet(client, baseUrl + path)

                whenReady(result) { res =>
                  res.status mustBe OK
                  val page = Jsoup.parse(res.body)
                  page.title must include(messages("Application complete"))
                  validateSummaryContent(page)
                  val detailsSection = page.getElementsByClass("govuk-details").get(0)
                  detailsSection.getElementsByClass("govuk-summary-list").size() mustBe 6

                  val businessDetails = detailsSection.getElementsByClass("govuk-summary-list").first()
                  detailsSection.getElementsByTag("h2").first().text() mustBe "Business details"
                  validateBusinessDetailsSummaryList(businessDetails, "0000001611", rosmAddress, HowManyLitresGlobally.None, false)

                  val contractPacking = detailsSection.getElementsByClass("govuk-summary-list").get(1)
                  detailsSection.getElementsByTag("h2").get(1).text() mustBe "Contract packed at your own site"
                  validateContractPackingWithLitresSummaryList(contractPacking, contractPackingLitres, false)

                  val imports = detailsSection.getElementsByClass("govuk-summary-list").get(2)
                  detailsSection.getElementsByTag("h2").get(2).text() mustBe "Brought into the UK"
                  validateImportsWithLitresSummaryList(imports, importsLitres, false)

                  val startDateSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(3)
                  detailsSection.getElementsByTag("h2").get(3).text() mustBe "Soft Drinks Industry Levy liability date"
                  validateStartDateSummaryList(startDateSummaryListItem, startDate, false)

                  val contactDetailsSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(4)
                  detailsSection.getElementsByTag("h2").get(4).text() mustBe "Contact person details"
                  validateContactDetailsSummaryList(contactDetailsSummaryListItem, contactDetails, false)

                  val siteDetailsSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(5)
                  detailsSection.getElementsByTag("h2").get(5).text() mustBe "UK site details"
                  validateSiteDetailsSummary(siteDetailsSummaryListItem, 3, 1, false)
                }
              }
            }
            "and they have only populated the required pages and have no litres" in {
              val userAnswers = userAnswerWithAllNoAndNoPagesToFilterOut(HowManyLitresGlobally.None)
                .copy(submittedOn = Some(submittedDate))
              val createdSubscriptionAndAmountProducedGlobally = CreatedSubscriptionAndAmountProducedGlobally(
                Subscription.generate(userAnswers, RosmWithUtr("0000001611", rosmRegistration)), HowManyLitresGlobally.None)
              build
                .commonPrecondition

              setAnswers(userAnswers)
              addToCache(SDILSessionKeys.CREATED_SUBSCRIPTION_AND_AMOUNT_PRODUCED_GLOBALLY, createdSubscriptionAndAmountProducedGlobally)

              WsTestClient.withClient { client =>
                val result = createClientRequestGet(client, baseUrl + path)

                whenReady(result) { res =>
                  res.status mustBe OK
                  val page = Jsoup.parse(res.body)
                  page.title must include(messages("Application complete"))
                  validateSummaryContent(page)
                  val detailsSection = page.getElementsByClass("govuk-details").get(0)
                  detailsSection.getElementsByClass("govuk-summary-list").size() mustBe 6

                  val businessDetails = detailsSection.getElementsByClass("govuk-summary-list").first()
                  detailsSection.getElementsByTag("h2").first().text() mustBe "Business details"
                  validateBusinessDetailsSummaryList(businessDetails, "0000001611", newAddress, HowManyLitresGlobally.None, false)

                  val contractPacking = detailsSection.getElementsByClass("govuk-summary-list").get(1)
                  detailsSection.getElementsByTag("h2").get(1).text() mustBe "Contract packed at your own site"
                  validateContractPackingWithNoLitresSummaryList(contractPacking, false)

                  val imports = detailsSection.getElementsByClass("govuk-summary-list").get(2)
                  detailsSection.getElementsByTag("h2").get(2).text() mustBe "Brought into the UK"
                  validateImportsWithNoLitresSummaryList(imports, false)

                  val startDateSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(3)
                  detailsSection.getElementsByTag("h2").get(3).text() mustBe "Soft Drinks Industry Levy liability date"
                  validateStartDateSummaryList(startDateSummaryListItem, startDate1, false)

                  val contactDetailsSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(4)
                  detailsSection.getElementsByTag("h2").get(4).text() mustBe "Contact person details"
                  validateContactDetailsSummaryList(contactDetailsSummaryListItem, contactDetails, false)

                  val siteDetailsSummaryListItem = detailsSection.getElementsByClass("govuk-summary-list").get(5)
                  detailsSection.getElementsByTag("h2").get(5).text() mustBe "UK site details"
                  validateSiteDetailsSummary(siteDetailsSummaryListItem, 0, 0, false)
                }
              }
            }
          }
        }
      }
    }

    "the user has not submitted a registration request" - {
      "should redirect to registration start" in {
        build
          .commonPrecondition

        val userAnswers = userAnswersWithLitres.copy(submittedOn = None)

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + path)

          whenReady(result) { res =>
            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(routes.RegistrationController.start.url)
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + path)
  }
}
