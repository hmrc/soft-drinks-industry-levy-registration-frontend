package controllers

import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.test.WsTestClient

class TestStringControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/testString"
  val checkRoutePath = "/changeTestString"

  val testString = "testing123"
  val testStringDiff = "testing456"

  val userAnswers = emptyUserAnswers.set(TestStringPage, testString).success.value

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the TestString page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("testString" + ".title"))
            val inputFields = page.getElementsByClass("govuk-input  govuk-!-width-full")
            inputFields.size() mustBe 1
            inputFields.get(0).getElementById("value").hasAttr("value") mustBe false
          }
        }
      }
    }

    s"when the userAnswers contains data for the page" - {
      s"should return OK and render the page with fields populated" in {
        given
          .commonPrecondition

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("testString" + ".title"))
            val inputFields = page.getElementsByClass("govuk-input  govuk-!-width-full")
            inputFields.size() mustBe 1
            inputFields.get(0).getElementById("value").hasAttr("value") mustBe true
            inputFields.get(0).getElementById("value").attr("value") mustBe testString
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, Messages("testString" + ".title"))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  "GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the TestString page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("testString" + ".title"))
            val inputFields = page.getElementsByClass("govuk-input  govuk-!-width-full")
            inputFields.size() mustBe 1
            inputFields.get(0).getElementById("value").hasAttr("value") mustBe false
          }
        }
      }
    }

    s"when the userAnswers contains data for the page" - {
      s"should return OK and render the page with fields populated" in {
        given
          .commonPrecondition

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("testString" + ".title"))
            val inputFields = page.getElementsByClass("govuk-input  govuk-!-width-full")
            inputFields.size() mustBe 1
            inputFields.get(0).getElementById("value").hasAttr("value") mustBe true
            inputFields.get(0).getElementById("value").attr("value") mustBe testString
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + checkRoutePath, Messages("testString" + ".title"))
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    "when the user ansers the question" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> testStringDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.TestRadioController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[String]](None)(_.get(TestStringPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe testStringDiff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> testStringDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.TestRadioController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[String]](None)(_.get(TestStringPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe testStringDiff
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
            client, baseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("testString" + ".title"
            ) )
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe 1
            val errorSummary = errorSummaryList.get(0)
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("testString.error.required"
            )
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> testStringDiff)))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> testStringDiff)))
  }

  s"POST " + checkRoutePath - {
    "when the user ansers the question" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.obj("value" -> testStringDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[String]](None)(_.get(TestStringPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe testStringDiff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.obj("value" -> testStringDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[String]](None)(_.get(TestStringPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe testStringDiff
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
            client, baseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("testString" + ".title"))
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe 1
            val errorSummary = errorSummaryList.get(0)
              errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("testString.error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> testStringDiff)))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> testStringDiff)))
  }
}
