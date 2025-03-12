package controllers

import models.{CheckMode, LitresInBands, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.HowManyImportsPage
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.WsTestClient


class HowManyImportsControllerISpec extends LitresISpecHelper {

  val normalRoutePath = "/how-many-imports-next-12-months"
  val checkRoutePath = "/change-how-many-imports-next-12-months"

  val userAnswers = emptyUserAnswers.set(HowManyImportsPage, litresInBands).success.value

  List(NormalMode, CheckMode).foreach { mode =>
    val (path, redirectLocation) = if(mode == NormalMode) {
      (normalRoutePath, routes.StartDateController.onPageLoad(NormalMode).url)
    } else {
      (checkRoutePath, routes.CheckYourAnswersController.onPageLoad.url)
    }

    "GET " + path - {
      "when the userAnswers contains no data" - {
        "should return OK and render the litres page for Imports with no data populated" in {
          `given`
            .commonPrecondition

          setAnswers(largeProducerImportsTrueUserAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "How many litres will you bring into the UK in the next 12 months? - Soft Drinks Industry Levy - GOV.UK"
              testLitresInBandsNoPrepopulatedData(page)
            }
          }
        }
      }

      s"when the userAnswers contains data for the page" - {
        s"should return OK and render the page with fields populated" in {
          `given`
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "How many litres will you bring into the UK in the next 12 months? - Soft Drinks Industry Levy - GOV.UK"
              testLitresInBandsWithPrepopulatedData(page)
            }
          }
        }
      }
      testOtherSuccessUserTypes(baseUrl + path, "How many litres will you bring into the UK in the next 12 months?")
      testUnauthorisedUser(baseUrl + path)
      testUserWhoIsUnableToRegister(baseUrl + path)
      testAuthenticatedUserButNoUserAnswers(baseUrl + path)
    }

    s"POST " + path - {
      "when the user populates all litres fields" - {
        "should update the session with the new values and redirect to " + redirectLocation - {
          "when the session contains no data for page" in {
            `given`
              .commonPrecondition

            setAnswers(largeProducerImportsTrueUserAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + path, Json.toJson(litresInBands)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          "when the session already contains data for page" in {
            `given`
              .commonPrecondition

            setAnswers(smallProducerNoPackagingRouteUserAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + path, Json.toJson(litresInBandsDiff)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBandsDiff
              }
            }
          }
        }
      }

      "should return 400 with required error" - {
        val errorTitle = "Error: How many litres will you bring into the UK in the next 12 months? - Soft Drinks Industry Levy - GOV.UK"

        "when no questions are answered" in {
          `given`
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + path, emptyJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testEmptyFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with no numeric answers" in {
          `given`
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + path, jsonWithNoNumeric
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testNoNumericFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with negative numbers" in {
          `given`
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + path, jsonWithNegativeNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testNegativeFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with decimal numbers" in {
          `given`
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + path, jsonWithDecimalNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testDecimalFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with out of max range numbers" in {
          `given`
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + path, jsonWithOutOfRangeNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testOutOfMaxValFormErrors(page, errorTitle)
            }
          }
        }
      }

      testUnauthorisedUser(baseUrl + path, Some(Json.toJson(litresInBandsDiff)))
      testUserWhoIsUnableToRegister(baseUrl + path, Some(Json.toJson(litresInBandsDiff)))
      testAuthenticatedUserButNoUserAnswers(baseUrl + path, Some(Json.toJson(litresInBandsDiff)))
    }

  }
}
