package controllers

import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.PackAtBusinessAddressPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class PackAtBusinessAddressControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/pack-at-business-address"
  val checkRoutePath = "/change-pack-at-business-address"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackAtBusinessAddress page with no data populated" in {
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include ("packAtBusinessAddress" + ".title")
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

    userAnswersForPackAtBusinessAddressPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          `given`
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include ("packAtBusinessAddress" + ".title")
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
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, "packAtBusinessAddress" + ".title")
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackAtBusinessAddress page with no data populated" in {
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include ("packAtBusinessAddress" + ".title")
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

    userAnswersForPackAtBusinessAddressPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          `given`
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include ("packAtBusinessAddress" + ".title")
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

    testOtherSuccessUserTypes(baseUrl + checkRoutePath, "packAtBusinessAddress" + ".title")
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {

    "when user selects yes with user answers, set trading name to organisation name" in {
      setAnswers(emptyUserAnswers)
      `given`
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(
          client, baseUrl + normalRoutePath, Json.obj("value" -> true)
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
          val dataStoredForPage = getAnswers(emptyUserAnswers.id).get
          dataStoredForPage.packagingSiteList.head._2.tradingName mustEqual "Super Lemonade Plc"
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + "packAtBusinessAddress" + ".title")
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe ("packAtBusinessAddress" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {

    "when user selects no with user answers" in {
      val alfOnRampURL: String = "http://onramp.com"
      setAnswers(emptyUserAnswers.set(PackAtBusinessAddressPage, false).success.value
        .copy(warehouseList = warehouseListWith1))
      `given`
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)
      WsTestClient.withClient { client =>
        emptyUserAnswers
          .set(PackAtBusinessAddressPage, true).success.value
          .copy(warehouseList = warehouseListWith1)
        val result = createClientRequestPOST(
          client, baseUrl + checkRoutePath, Json.obj("value" -> false)
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
        }
      }
    }

    "when user selects yes with user answers" in {
      setAnswers(emptyUserAnswers.set(PackAtBusinessAddressPage, true).success.value
        .copy(warehouseList = warehouseListWith1))
      `given`
        .commonPrecondition
      WsTestClient.withClient { client =>
        emptyUserAnswers
          .set(PackAtBusinessAddressPage, true).success.value
          .copy(warehouseList = warehouseListWith1)
        val result = createClientRequestPOST(
          client, baseUrl + checkRoutePath, Json.obj("value" -> true)
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url)
        }
      }
    }

    "when user selects no without user answers" in {
      val alfOnRampURL: String = "http://onramp.com"
      setAnswers(emptyUserAnswers)
      `given`
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)
      WsTestClient.withClient { client =>
        emptyUserAnswers
          .set(PackAtBusinessAddressPage, true).success.value
          .copy(warehouseList = warehouseListWith1)
        val result = createClientRequestPOST(
          client, baseUrl + checkRoutePath, Json.obj("value" -> false)
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
        }
      }
    }

    "when user selects yes without user answers" in {
      setAnswers(emptyUserAnswers)
      `given`
        .commonPrecondition
      WsTestClient.withClient { client =>
        emptyUserAnswers
          .set(PackAtBusinessAddressPage, true).success.value
          .copy(warehouseList = warehouseListWith1)
        val result = createClientRequestPOST(
          client, baseUrl + checkRoutePath, Json.obj("value" -> true)
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url)
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + "packAtBusinessAddress" + ".title")
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe ("packAtBusinessAddress" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
