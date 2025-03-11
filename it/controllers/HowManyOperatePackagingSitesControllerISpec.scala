package controllers

import models.{CheckMode, LitresInBands, NormalMode}
import org.jsoup.Jsoup
import pages.HowManyOperatePackagingSitesPage
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.{FakeRequest, WsTestClient}
import org.scalatest.matchers.must.Matchers._
import org.scalatestplus.mockito.MockitoSugar.mock
import testSupport.preConditions.PreconditionHelpers

class HowManyOperatePackagingSitesControllerISpec extends LitresISpecHelper {

  val normalRoutePath = "/how-many-own-brands-next-12-months"
  val checkRoutePath = "/change-how-many-own-brands-next-12-months"

  override val preconditionHelpers: PreconditionHelpers = mock[PreconditionHelpers]
  
  val userAnswers = emptyUserAnswers.set(HowManyOperatePackagingSitesPage, litresInBands).success.value

  List(NormalMode, CheckMode).foreach { mode =>
    val (path, redirectLocation) = if(mode == NormalMode) {
      (normalRoutePath, routes.ContractPackingController.onPageLoad(NormalMode).url)
    } else {
      (checkRoutePath, routes.CheckYourAnswersController.onPageLoad.url)
    }

    "GET " + path - {
      "when the userAnswers contains no data" - {
        "should return OK and render the litres page for OperatePackagingSites with no data populated" in {
          preconditionHelpers
            .commonPrecondition

          setAnswers(emptyUserAnswers)(using timeout)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("howManyOperatePackagingSites" + ".title"))
              testLitresInBandsNoPrepopulatedData(page)
            }
          }
        }
      }

      s"when the userAnswers contains data for the page" - {
        s"should return OK and render the page with fields populated" in {
          preconditionHelpers
            .commonPrecondition

          setAnswers(userAnswers)(using timeout)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("howManyOperatePackagingSites" + ".title"))
              testLitresInBandsWithPrepopulatedData(page)
            }
          }
        }
      }
      testOtherSuccessUserTypes(baseUrl + path, messages("howManyOperatePackagingSites" + ".title"))
      testUnauthorisedUser(baseUrl + path)
      testUserWhoIsUnableToRegister(baseUrl + path)
      testAuthenticatedUserButNoUserAnswers(baseUrl + path)
    }

    s"POST " + path - {
      "when the user populates all litres fields" - {
        "should update the session with the new values and redirect to " + redirectLocation - {
          "when the session contains no data for page" in {
            preconditionHelpers
              .commonPrecondition

            setAnswers(emptyUserAnswers)(using timeout)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + path, Json.toJson(litresInBands)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage = getAnswers(userAnswers.id)(using timeout).fold[Option[LitresInBands]](None)(_.get(HowManyOperatePackagingSitesPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          "when the session already contains data for page" in {
            preconditionHelpers
              .commonPrecondition

            setAnswers(userAnswers)(using timeout)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + path, Json.toJson(litresInBandsDiff)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage = getAnswers(userAnswers.id)(using timeout).fold[Option[LitresInBands]](None)(_.get(HowManyOperatePackagingSitesPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBandsDiff
              }
            }
          }
        }
      }

      "should return 400 with required error" - {
        val errorTitle = "Error: " + messages("howManyOperatePackagingSites.title")

        "when no questions are answered" in {
          preconditionHelpers
            .commonPrecondition

          setAnswers(emptyUserAnswers)(using timeout)
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
          preconditionHelpers
            .commonPrecondition

          setAnswers(emptyUserAnswers)(using timeout)
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
          preconditionHelpers
            .commonPrecondition

          setAnswers(emptyUserAnswers)(using timeout)
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
          preconditionHelpers
            .commonPrecondition

          setAnswers(emptyUserAnswers)(using timeout)
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
          preconditionHelpers
            .commonPrecondition

          setAnswers(emptyUserAnswers)(using timeout)
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
