package controllers

import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.http.HeaderNames
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.Messages
import play.api.test.WsTestClient

class RegistrationConfirmationControllerISpec extends RegSummaryISpecHelper {

  val normalRoutePath = "/registration-confirmation"

  "GET " + normalRoutePath - {
    "when the user has submitted a registration request" - {
      "should return OK and render the RegistrationConfirmation page" - {
        "with the expected content" - {
          "when the user answered yes to all litres questions" in {
            given
              .commonPrecondition

            val userAnswers = userAnswersWithLitres.copy(submittedOn = Some(submittedDate))

            setAnswers(userAnswers)

            WsTestClient.withClient { client =>
              val result = createClientRequestGet(client, baseUrl + normalRoutePath)

              whenReady(result) { res =>
                res.status mustBe OK
                val page = Jsoup.parse(res.body)
                page.title must include(Messages("Application complete"))

                page.getElementsByClass("govuk-summary-list").size() mustBe 7

                validateSummaryContent(page)
                val detailsSection = page.getElementsByClass("govuk-details").get(0)

                val businessDetails = detailsSection.getElementsByClass("govuk-summary-list").first()
                detailsSection.getElementsByTag("h2").first().text() mustBe "Business details"
                validateBusinessDetailsSummaryList(businessDetails, "0000001611", rosmAddress, "1 million litres or more", false)

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
              }
            }
          }

          "when the user answered no to all litres questions" in {
            given
              .commonPrecondition

            val userAnswers = userAnswersWithAllNo.copy(submittedOn = Some(submittedDate))

            setAnswers(userAnswers)

            WsTestClient.withClient { client =>
              val result = createClientRequestGet(client, baseUrl + normalRoutePath)

              whenReady(result) { res =>
                res.status mustBe OK
                val page = Jsoup.parse(res.body)
                page.title must include(Messages("Application complete"))

                page.getElementsByClass("govuk-summary-list").size() mustBe 7

                validateSummaryContent(page)
                val detailsSection = page.getElementsByClass("govuk-details").get(0)

                val businessDetails = detailsSection.getElementsByClass("govuk-summary-list").first()
                detailsSection.getElementsByTag("h2").first().text() mustBe "Business details"
                validateBusinessDetailsSummaryList(businessDetails, "0000001611", newAddress, "Less than 1 million litres", false)

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
              }
            }
          }
        }
      }
    }

    "the user has not submitted a registration request" - {
      "should redirect to the start" in {
        given
          .commonPrecondition

        val userAnswers = userAnswersWithLitres.copy(submittedOn = None)

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result) { res =>
            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(routes.VerifyController.onPageLoad(NormalMode).url)
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, Messages("registrationConfirmation" + ".title"), fullExampleUserAnswers.copy(submittedOn = Some(submittedDate)))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
    testWhoIsUnableToRegisterWithGivenUtr(baseUrl + normalRoutePath)
    testUnauthorisedUser(baseUrl + normalRoutePath)
  }
}
