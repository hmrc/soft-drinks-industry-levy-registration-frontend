package controllers

import connectors.{DoesNotExist, Pending, Registered}
import models.RegisterState.{RegisterApplicationAccepted, RegisterWithOtherUTR, RegistrationPending}
import models.backend.{Site, UkAddress}
import models.{Identify, NormalMode, RegisterState, Warehouse}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.EnterBusinessDetailsPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

import scala.util.Random

class EnterBusinessDetailsControllerISpec extends ControllerITTestHelper {

  val path = "/enter-business-details"

  val enterBusinessDetails = Identify(utr = "0000000437", postcode = "GU14 8NL")

  val randomStringExceedingMaxLength = Random.nextString(10 + 1)

  val userAnswersPageSame = emptyUserAnswers.copy(registerState = RegisterState.RegisterWithOtherUTR)
    .set(EnterBusinessDetailsPage, Identify(utr = "0000000437", postcode = "GU14 8NL")).success.value

  val expectedRedirectUrlForSubscriptionStatus = Map(
    Pending -> routes.RegistrationPendingController.onPageLoad.url,
    Registered -> routes.ApplicationAlreadySubmittedController.onPageLoad.url,
    DoesNotExist -> routes.VerifyController.onPageLoad(NormalMode).url
  )

  val expectedRegisteredStateForSubscriptionStatus = Map(
    Pending -> RegistrationPending,
    Registered -> RegisterApplicationAccepted,
    DoesNotExist -> RegisterWithOtherUTR
  )

  "GET " + path - {
    "when the userAnswers contains no data" - {
      "should return OK and render the EnterBusinessDetails page with no data populated" in {
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers.copy(registerState = RegisterState.RequiresBusinessDetails))

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + path)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include ("enterBusinessDetails" + ".title")
            val inputFields = page.getElementsByClass("govuk-input")
            inputFields.text() mustEqual ""
          }
        }
      }
    }
  }

  s"POST " + path - {
    "when the user answers the question," - {
      "the utr entered has rosmData" - {
        expectedRedirectUrlForSubscriptionStatus.foreach { case (subscriptionState, expectedUrl) =>
          s"and subscription status of $subscriptionState" - {
            s"should redirect to $expectedUrl" - {
              "when the session contains no data for the page" in {
                `given`
                  .user.isAuthorisedButNotEnrolled()
                `given`.sdilBackend.retrieveRosm("0000000437")
                `given`.sdilBackend.checkPendingQueue("0000000437", subscriptionState)

                setAnswers(emptyUserAnswers.copy(registerState = RegisterState.RequiresBusinessDetails))
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, baseUrl + path, Json.obj("utr" -> "0000000437", "postcode" -> "GU14 8NL")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                    val updatedUA = getAnswers(userAnswersPageSame.id)
                    updatedUA.get.registerState mustBe expectedRegisteredStateForSubscriptionStatus(subscriptionState)
                    val dataStoredForPage = updatedUA.fold[Option[Identify]](None)(_.get(EnterBusinessDetailsPage))

                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe enterBusinessDetails
                  }
                }
              }

              "when the session already contains data for the page which is different" in {
                `given`
                  .user.isAuthorisedButNotEnrolled()
                `given`.sdilBackend.retrieveRosm("0000000437")
                `given`.sdilBackend.checkPendingQueue("0000000437", subscriptionState)

                val userAnswersWithNonIdenticalData = {
                  emptyUserAnswers
                    .set(EnterBusinessDetailsPage, Identify("0000001611", "AA1 1AA")).success.get
                    .copy(
                      registerState = RegisterState.RequiresBusinessDetails,
                      address = Some(UkAddress(List.empty, "")),
                      packagingSiteList = Map("" -> Site(UkAddress(List.empty, ""), None, aTradingName, None)),
                      warehouseList = Map("" -> Warehouse(aTradingName, UkAddress(List.empty, ""))),
                      submittedOn = None
                    )
                }

                setAnswers(userAnswersWithNonIdenticalData)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, baseUrl + path, Json.obj("utr" -> "0000000437", "postcode" -> "GU14 8NL")
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                    val updatedAnswers = getAnswers(userAnswersWithNonIdenticalData.id)
                    val dataStoredForPage = updatedAnswers.fold[Option[Identify]](None)(_.get(EnterBusinessDetailsPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe Identify(utr = "0000000437", postcode = "GU14 8NL")
                    updatedAnswers.get.registerState mustBe expectedRegisteredStateForSubscriptionStatus(subscriptionState)
                    updatedAnswers.get.address mustBe None
                    updatedAnswers.get.warehouseList mustBe Map.empty
                    updatedAnswers.get.packagingSiteList mustBe Map.empty
                    updatedAnswers.get.submittedOn mustBe None
                    updatedAnswers.get.data mustBe Json.obj("enterBusinessDetails" -> Json.obj("utr" -> "0000000437", "postcode" -> "GU14 8NL"))
                  }
                }
              }
            }
          }
        }
      }
    }

    "when the session already contains data that does matches the data the user entered, should NOT reset user answers" - {
      "and redirect to verify" in {
        val userAnswersWithIdenticalData = {
          emptyUserAnswers
            .set(EnterBusinessDetailsPage, Identify("0000001611", "GU14 8NL")).success.get
            .copy(
              registerState = RegisterState.RegisterWithOtherUTR,
              address = Some(UkAddress(List.empty, "")),
              packagingSiteList = Map("" -> Site(UkAddress(List.empty, ""), None, aTradingName, None)),
              warehouseList = Map("" -> Warehouse(aTradingName, UkAddress(List.empty, ""))),
              submittedOn = None
            )
        }
        `given`
          .commonPrecondition

        setAnswers(userAnswersWithIdenticalData)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + path, Json.obj("utr" -> "0000001611", "postcode" -> "GU14 8NL")
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.VerifyController.onPageLoad(NormalMode).url)
            val updatedAnswers = getAnswers(userAnswersWithIdenticalData.id)
            updatedAnswers.get.address mustBe userAnswersWithIdenticalData.address
            updatedAnswers.get.warehouseList mustBe userAnswersWithIdenticalData.warehouseList
            updatedAnswers.get.packagingSiteList mustBe userAnswersWithIdenticalData.packagingSiteList
            updatedAnswers.get.submittedOn mustBe userAnswersWithIdenticalData.submittedOn
            updatedAnswers.get.data mustBe userAnswersWithIdenticalData.data
          }
        }
      }
    }

    "should return 400 with utr max length error" - {
      "when the question is answered with incorrect data" in {
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers.copy(registerState = RegisterState.RequiresBusinessDetails))
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + path, Json.obj("utr" -> randomStringExceedingMaxLength, "postcode" -> "GU14 8NL")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + "enterBusinessDetails" + ".title")
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe 1
            val errorSummary = errorSummaryList.get(0)
            errorSummary
              .select("a")
              .attr("href") mustBe "#utr"
            errorSummary.text() mustBe "enterBusinessDetails.invalid.utr.length"
          }
        }
      }
    }
  }
}
