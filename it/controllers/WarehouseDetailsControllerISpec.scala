package controllers

import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.{AskSecondaryWarehousesPage, WarehouseDetailsPage}
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class WarehouseDetailsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/warehouses"
  val checkRoutePath = "/change-warehouses"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return SEE_OTHER and redirect to Ask Secondary Warehouse page" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url)
          }
        }
      }
    }

    userAnswersForWarehouseDetailsPage.foreach { case (key, userAnswers) =>
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
              page.title must include(Messages("warehouseDetails.title.heading","1",""))
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

    testUnauthorisedUser(baseUrl + normalRoutePath)
    testWhoIsUnableToRegisterWithGivenUtr(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the WarehouseDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url)
          }
        }
      }
    }

    userAnswersForWarehouseDetailsPage.foreach { case (key, userAnswers) =>
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
              page.title must include(Messages("warehouseDetails.title.heading","1",""))
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


    testUnauthorisedUser(baseUrl + checkRoutePath)
    testWhoIsUnableToRegisterWithGivenUtr(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
  }

  "Get should return redirect to AskSecondaryWarehouses page when 0 warehouses listed with No answer on AskSecondaryWarehouses page" in {
    given
      .commonPrecondition

    setAnswers(emptyUserAnswers
      .set(AskSecondaryWarehousesPage, true).success.value
    )

    WsTestClient.withClient { client =>
      val result = createClientRequestGet(client, baseUrl + checkRoutePath)
      whenReady(result) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url)
        val dataStoredForPage = getAnswers(identifier).fold[Option[Boolean]](None)(_.get(AskSecondaryWarehousesPage))
        dataStoredForPage.nonEmpty mustBe false
      }
    }
  }

  "Get should return the correct title and header when 1 warehouse listed on page" in {
    given
      .commonPrecondition
    setAnswers(userAnswersWith1Warehouse)
    WsTestClient.withClient { client =>
      val result = createClientRequestGet(client, baseUrl + checkRoutePath)
      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.title must include(Messages("warehouseDetails.title.heading", "1", ""))
        page.getElementsByClass("remove-link").size() mustEqual 1
      }
    }
  }

  "Get should return the correct title and header when 2 warehouses listed on page" in {
    given
      .commonPrecondition
    setAnswers(userAnswersWith2Warehouses)
    WsTestClient.withClient { client =>
      val result = createClientRequestGet(client, baseUrl + checkRoutePath)
      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.title must include(Messages("warehouseDetails.title.heading", "2", "s"))
        page.getElementsByClass("remove-link").size() mustEqual 2
      }
    }
  }

  s"POST " + normalRoutePath - {
    userAnswersForWarehouseDetailsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            val alfOnRampURL: String = "http://onramp.com"
            setAnswers(emptyUserAnswers
              .set(WarehouseDetailsPage, true).success.value
              )
            given
              .commonPrecondition
              .alf.getSuccessResponseFromALFInit(alfOnRampURL)

            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            val alfOnRampURL: String = "http://onramp.com"
            setAnswers(emptyUserAnswers
              .set(WarehouseDetailsPage, true).success.value
            )
            given
              .commonPrecondition
              .alf.getSuccessResponseFromALFInit(alfOnRampURL)

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
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
            page.title must include("Error: " + Messages("warehouseDetails.title.heading","0","s"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("warehouseDetails" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testWhoIsUnableToRegisterWithGivenUtr(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {

    "when user selects yes" in {
      val alfOnRampURL: String = "http://onramp.com"
      setAnswers(emptyUserAnswers.set(WarehouseDetailsPage, true).success.value
        .copy(warehouseList = warehouseListWith1))
      given
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)
      WsTestClient.withClient { client =>
        emptyUserAnswers
          .set(WarehouseDetailsPage, true).success.value
          .copy(warehouseList = warehouseListWith1)
        val result = createClientRequestPOST(
          client, baseUrl + checkRoutePath, Json.obj("value" -> true)
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
        }
      }
    }

    "when user selects no" in {
      val alfOnRampURL: String = "http://onramp.com"
      setAnswers(emptyUserAnswers.set(WarehouseDetailsPage, true).success.value
        .copy(warehouseList = warehouseListWith1))
      given
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)
      WsTestClient.withClient { client =>
        emptyUserAnswers
          .set(WarehouseDetailsPage, true).success.value
          .copy(warehouseList = warehouseListWith1)
        val result = createClientRequestPOST(
          client, baseUrl + checkRoutePath, Json.obj("value" -> false)
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
        }
      }
    }

    userAnswersForWarehouseDetailsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            val alfOnRampURL: String = "http://onramp.com"
            setAnswers(emptyUserAnswers.set(WarehouseDetailsPage, true).success.value
              .copy(warehouseList = warehouseListWith1))
            given
              .commonPrecondition
              .alf.getSuccessResponseFromALFInit(alfOnRampURL)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            val alfOnRampURL: String = "http://onramp.com"
            given
              .commonPrecondition
              .alf.getSuccessResponseFromALFInit(alfOnRampURL)

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
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
            page.title must include("Error: " + Messages("warehouseDetails.title.heading","0","s"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("warehouseDetails" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testWhoIsUnableToRegisterWithGivenUtr(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
