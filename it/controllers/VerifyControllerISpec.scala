package controllers

import models.{NormalMode, Verify}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.VerifyPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class VerifyControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/verify"
  val checkRoutePath = "/change-verify"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Verify page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("verify" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe Verify.values.size
            page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove GU14 8NL"
            page.getElementById("utrField").text() mustBe "0000001611"
            Verify.values.zipWithIndex.foreach { case (radio1, index1) =>
              radioInputs.get(index1).attr("value") mustBe radio1.toString
              radioInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    Verify.values.zipWithIndex.foreach { case (radio, index) =>
      s"when the userAnswers contains data for the page with " + radio.toString + " selected" - {
        s"should return OK and render the page with " + radio.toString + " radio checked" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(VerifyPage, radio).success.value

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("verify" + ".title"))
              page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove GU14 8NL"
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe Verify.values.size
              page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove GU14 8NL"
              page.getElementById("utrField").text() mustBe "0000001611"
              Verify.values.zipWithIndex.foreach { case (radio1, index1) =>
                radioInputs.get(index1).attr("value") mustBe radio1.toString
                radioInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, Messages("verify" + ".title"))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Verify page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("verify" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe Verify.values.size
            page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove GU14 8NL"
            page.getElementById("utrField").text() mustBe "0000001611"
            Verify.values.zipWithIndex.foreach { case (radio1, index1) =>
              radioInputs.get(index1).attr("value") mustBe radio1.toString
              radioInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    Verify.values.zipWithIndex.foreach { case (radio, index) =>
      s"when the userAnswers contains data for the page with " + radio.toString + " selected" - {
        s"should return OK and render the page with " + radio.toString + " radio checked" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(VerifyPage, radio).success.value


          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("verify" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe Verify.values.size
              page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove GU14 8NL"
              page.getElementById("utrField").text() mustBe "0000001611"
              Verify.values.zipWithIndex.foreach { case (radio1, index1) =>
                radioInputs.get(index1).attr("value") mustBe radio1.toString
                radioInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + checkRoutePath, Messages("verify" + ".title"))
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)

  }

  s"POST " + normalRoutePath - {
    Verify.values.foreach { case radio =>
      "when the user selects " + radio.toString - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> radio)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.OrganisationTypeController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(identifier).fold[Option[Verify]](None)(_.get(VerifyPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe radio
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswers.set(VerifyPage, radio).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> radio)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.OrganisationTypeController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Verify]](None)(_.get(VerifyPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe radio
              }
            }
          }
        }
      }
    }

    "when the user does not select an option" - {
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
            page.title must include("Error: " + Messages("verify" + ".title"))
            page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove GU14 8NL"
            page.getElementById("utrField").text() mustBe "0000001611"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe "Select if this is the business you want to register, if you want to register this business but want to add a new contact address for the Soft Drinks Industry Levy, or if you need to sign in using a different Government Gateway user ID"
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    Verify.values.foreach { case radio =>
      "when the user selects " + radio.toString - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(radio))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
                val dataStoredForPage = getAnswers(identifier).fold[Option[Verify]](None)(_.get(VerifyPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe radio
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswers.set(VerifyPage, radio).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(radio))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Verify]](None)(_.get(VerifyPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe radio
              }
            }
          }
        }
      }
    }

    "when the user does not select and option" - {
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
            page.title must include("Error: " + Messages("verify" + ".title"))
            page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove GU14 8NL"
            page.getElementById("utrField").text() mustBe "0000001611"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe "Select if this is the business you want to register, if you want to register this business but want to add a new contact address for the Soft Drinks Industry Levy, or if you need to sign in using a different Government Gateway user ID"
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
