package controllers

import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import pages.{AskSecondaryWarehousesPage, WarehouseDetailsPage}
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.{FakeRequest, WsTestClient}
import org.scalatestplus.mockito.MockitoSugar.mock
import testSupport.preConditions.PreconditionHelpers

class WarehouseDetailsControllerISpec extends ControllerITTestHelper {

  override val preconditionHelpers: PreconditionHelpers = mock[PreconditionHelpers]
  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())
  
  val normalRoutePath = "/warehouses"
  val checkRoutePath = "/change-warehouses"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return SEE_OTHER and redirect to Ask Secondary Warehouse page" in {
        preconditionHelpers
          .commonPrecondition

        setAnswers(emptyUserAnswers)(using timeout)

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
        s"should return OK and render the page with " + key + " radio unchecked" in {
          preconditionHelpers
            .commonPrecondition

          setAnswers(userAnswers)(using timeout)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("warehouseDetails.title.heading","1",""))
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
    }

    testUnauthorisedUser(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Ask Secondary Warehouse page with no data populated" in {
        preconditionHelpers
          .commonPrecondition

        setAnswers(emptyUserAnswers)(using timeout)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(CheckMode).url)
          }
        }
      }
    }

    userAnswersForWarehouseDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio unchecked" in {
          preconditionHelpers
            .commonPrecondition

          setAnswers(userAnswers)(using timeout)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("warehouseDetails.title.heading","1",""))
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
    }


    testUnauthorisedUser(baseUrl + checkRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
  }

  "Get should return redirect to AskSecondaryWarehouses page when 0 warehouses listed" in {
    preconditionHelpers
      .commonPrecondition

    setAnswers(emptyUserAnswers
      .set(AskSecondaryWarehousesPage, true).success.value
    )(using timeout)

    WsTestClient.withClient { client =>
      val result = createClientRequestGet(client, baseUrl + checkRoutePath)

      whenReady(result) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(CheckMode).url)
      }
    }
  }

  "Get should return the correct title and header when 1 warehouse listed on page" in {
    preconditionHelpers
      .commonPrecondition
    setAnswers(userAnswersWith1Warehouse)(using timeout)
    WsTestClient.withClient { client =>
      val result = createClientRequestGet(client, baseUrl + checkRoutePath)
      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.title must include(messages("warehouseDetails.title.heading", "1", ""))
        page.getElementsByClass("remove-link").size() mustEqual 1
      }
    }
  }

  "Get should return the correct title and header when 2 warehouses listed on page" in {
    preconditionHelpers
      .commonPrecondition
    setAnswers(userAnswersWith2Warehouses)(using timeout)
    WsTestClient.withClient { client =>
      val result = createClientRequestGet(client, baseUrl + checkRoutePath)
      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.title must include(messages("warehouseDetails.title.heading", "2", "s"))
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
              )(using timeout)
            preconditionHelpers
              .commonPrecondition
              .alf.getSuccessResponseFromALFInit(alfOnRampURL)

            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val dataStoredForPage = getAnswers(userAnswers.id)(using timeout).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            val alfOnRampURL: String = "http://onramp.com"
            setAnswers(emptyUserAnswers
              .set(WarehouseDetailsPage, true).success.value
            )(using timeout)
            preconditionHelpers
              .commonPrecondition
              .alf.getSuccessResponseFromALFInit(alfOnRampURL)

            setAnswers(userAnswers)(using timeout)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val dataStoredForPage = getAnswers(userAnswers.id)(using timeout).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
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
        preconditionHelpers
          .commonPrecondition

        setAnswers(emptyUserAnswers)(using timeout)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("warehouseDetails.title.heading","0","s"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("warehouseDetails" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {

    "when user selects yes" in {
      val alfOnRampURL: String = "http://onramp.com"
      setAnswers(emptyUserAnswers.set(WarehouseDetailsPage, true).success.value
        .copy(warehouseList = warehouseListWith1))(using timeout)
      preconditionHelpers
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
        .copy(warehouseList = warehouseListWith1))(using timeout)
      preconditionHelpers
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
              .copy(warehouseList = warehouseListWith1))(using timeout)
            preconditionHelpers
              .commonPrecondition
              .alf.getSuccessResponseFromALFInit(alfOnRampURL)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val dataStoredForPage = getAnswers(userAnswers.id)(using timeout).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            val alfOnRampURL: String = "http://onramp.com"
            preconditionHelpers
              .commonPrecondition
              .alf.getSuccessResponseFromALFInit(alfOnRampURL)

            setAnswers(userAnswers)(using timeout)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val dataStoredForPage = getAnswers(userAnswers.id)(using timeout).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
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
        preconditionHelpers
          .commonPrecondition

        setAnswers(emptyUserAnswers)(using timeout)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("warehouseDetails.title.heading","0","s"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("warehouseDetails" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
