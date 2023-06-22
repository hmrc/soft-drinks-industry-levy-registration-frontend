package controllers

import models.{Identification, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.EnterBusinessDetailsPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.test.WsTestClient

import scala.util.Random

class EnterBusinessDetailsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/enter-business-details"
  val checkRoutePath = "/change-enter-business-details"

  val enterBusinessDetails = Identification(utr = "0000000437", postcode = "GU14 8NL")

  val randomStringExceedingMaxLength = Random.nextString(10 + 1)

  val userAnswers = emptyUserAnswers.set(EnterBusinessDetailsPage, Identification(utr = "0000000437", postcode = "GU14 8NL")).success.value

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the EnterBusinessDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("enterBusinessDetails" + ".title"))
            val inputFields = page.getElementsByClass("govuk-input")
            inputFields.text() mustEqual ""
          }
        }
      }
    }

    testOtherSuccessUserTypes(baseUrl + normalRoutePath, Messages("enterBusinessDetails" + ".title"))
    testUnauthorisedUser(baseUrl + normalRoutePath)
  }

  "GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the EnterBusinessDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("enterBusinessDetails" + ".title"))
            val inputFields = page.getElementsByClass("govuk-input")
            inputFields.size() mustBe 2
            inputFields.text() mustBe ""
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + checkRoutePath, Messages("enterBusinessDetails" + ".title"))
    testUnauthorisedUser(baseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    "when the user answers the question" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition
          given.sdilBackend.retrieveRosm("0000000437")

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("utr" -> "0000000437", "postcode" -> "GU14 8NL")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Identification]](None)(_.get(EnterBusinessDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe enterBusinessDetails
            }
          }
        }


        "when the session already contains data for page" in {
          given
            .commonPrecondition
          given.sdilBackend.retrieveRosm("0000000437")

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("utr" -> "0000000437", "postcode" -> "GU14 8NL")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Identification]](None)(_.get(EnterBusinessDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe enterBusinessDetails
            }
          }
        }
      }
    }

    "should return 400 with utr max length error" - {
      "when the question is answered with incorrect data" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("utr" -> randomStringExceedingMaxLength, "postcode" -> "GU14 8NL")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("enterBusinessDetails" + ".title"
            ) )
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe 1
            val errorSummary = errorSummaryList.get(0)
            errorSummary
              .select("a")
              .attr("href") mustBe "#utr"
            errorSummary.text() mustBe Messages("enterBusinessDetails.invalid.utr.length"
            )
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("utr" -> enterBusinessDetails.utr, "postcode" -> enterBusinessDetails.postcode)))
  }

  s"POST " + checkRoutePath - {
    "when the user answers the question" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition
          given.sdilBackend.retrieveRosm("0000000437")

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.obj("utr" -> "0000000437", "postcode" -> "GU14 8NL")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Identification]](None)(_.get(EnterBusinessDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe enterBusinessDetails
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      "when the question is not answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, Json.obj("utr" -> randomStringExceedingMaxLength, "postcode" -> "GU14 8NL")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("enterBusinessDetails" + ".title"))
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe 1
            val errorSummary = errorSummaryList.get(0)
              errorSummary
              .select("a")
              .attr("href") mustBe "#utr"
            errorSummary.text() mustBe Messages("enterBusinessDetails.invalid.utr.length")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("utr" -> enterBusinessDetails.utr, "postcode" -> enterBusinessDetails.postcode)))
  }
}
