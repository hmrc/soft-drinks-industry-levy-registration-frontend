/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
  val checkRoutePath  = "/change-how-many-imports-next-12-months"

  val userAnswers = emptyUserAnswers.set(HowManyImportsPage, litresInBands).success.value

  List(NormalMode, CheckMode).foreach { mode =>
    val (path, redirectLocation) = if (mode == NormalMode) {
      (normalRoutePath, routes.StartDateController.onPageLoad(NormalMode).url)
    } else {
      (checkRoutePath, routes.CheckYourAnswersController.onPageLoad.url)
    }

    "GET " + path - {
      "when the userAnswers contains no data" - {
        "should return OK and render the litres page for Imports with no data populated" in {
          build.commonPrecondition

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
          build.commonPrecondition

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
            build.commonPrecondition

            setAnswers(largeProducerImportsTrueUserAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client,
                baseUrl + path,
                Json.toJson(litresInBands)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage =
                  getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          "when the session already contains data for page" in {
            build.commonPrecondition

            setAnswers(smallProducerNoPackagingRouteUserAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client,
                baseUrl + path,
                Json.toJson(litresInBandsDiff)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage =
                  getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBandsDiff
              }
            }
          }
        }
      }

      "should return 400 with required error" - {
        val errorTitle =
          "Error: How many litres will you bring into the UK in the next 12 months? - Soft Drinks Industry Levy - GOV.UK"

        "when no questions are answered" in {
          build.commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client,
              baseUrl + path,
              emptyJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testEmptyFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with no numeric answers" in {
          build.commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client,
              baseUrl + path,
              jsonWithNoNumeric
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testNoNumericFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with negative numbers" in {
          build.commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client,
              baseUrl + path,
              jsonWithNegativeNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testNegativeFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with decimal numbers" in {
          build.commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client,
              baseUrl + path,
              jsonWithDecimalNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testDecimalFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with out of max range numbers" in {
          build.commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client,
              baseUrl + path,
              jsonWithOutOfRangeNumber
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
