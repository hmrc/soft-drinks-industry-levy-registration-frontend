package controllers

import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.AskSecondaryWarehousesPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class AskSecondaryWarehousesControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/ask-secondary-warehouses"
  val checkRoutePath = "/change-ask-secondary-warehouses"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the AskSecondaryWarehouses page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("askSecondaryWarehouses" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersForAskSecondaryWarehousesPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("askSecondaryWarehouses" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, Messages("askSecondaryWarehouses" + ".title"))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the AskSecondaryWarehouses page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("askSecondaryWarehouses" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersForAskSecondaryWarehousesPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("askSecondaryWarehouses" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }

    testOtherSuccessUserTypes(baseUrl + checkRoutePath, Messages("askSecondaryWarehouses" + ".title"))
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)

  }

  s"POST " + normalRoutePath - {
    userAnswersForAskSecondaryWarehousesPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad().url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(AskSecondaryWarehousesPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad().url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(AskSecondaryWarehousesPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
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
            page.title must include("Error: " + Messages("askSecondaryWarehouses" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("askSecondaryWarehouses" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))

  }

  s"POST " + checkRoutePath - {
    userAnswersForAskSecondaryWarehousesPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(AskSecondaryWarehousesPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(AskSecondaryWarehousesPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
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
            page.title must include("Error: " + Messages("askSecondaryWarehouses" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("askSecondaryWarehouses" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
