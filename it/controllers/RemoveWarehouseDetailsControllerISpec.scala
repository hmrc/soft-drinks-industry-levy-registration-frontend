package controllers

import models.{CheckMode, NormalMode, Warehouse}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.RemoveWarehouseDetailsPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class RemoveWarehouseDetailsControllerISpec extends ControllerITTestHelper {

  def normalRoutePath(index: String) = s"/warehouse-details/remove/$index"
  def checkRoutePath(index: String) = s"/change-warehouse-details/remove/$index"
  val indexOfWarehouseToBeRemoved: String = "warehouseUNO"
  "GET " + normalRoutePath("indexDoesntExist") - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + normalRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
          }
        }
      }
    }

    userAnswersForUpdateRegisteredDetailsRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page without the " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("removeWarehouseDetails" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe false
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe false
              page.getElementById("warehouseToRemove").text() mustBe "foo, bar, wizz"
            }
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))  }

  s"GET " + checkRoutePath(indexOfWarehouseToBeRemoved) - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + checkRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehouseDetailsController.onPageLoad(CheckMode).url)
          }
        }
      }
    }

    userAnswersForUpdateRegisteredDetailsRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page without the " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, baseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("removeWarehouseDetails" + ".title"))
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

    testUnauthorisedUser(baseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))
  }

  s"POST " + normalRoutePath(indexOfWarehouseToBeRemoved) - {

    userAnswersForUpdateRegisteredDetailsRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the Warehouse details controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            getAnswers(userAnswers.id).get.warehouseList.size mustBe 1
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
                val userAnswersAfterTest = getAnswers(userAnswers.id)
                val dataStoredForPage = userAnswersAfterTest.fold[Option[Boolean]](None)(_.get(RemoveWarehouseDetailsPage))
                if(yesSelected) {
                  userAnswersAfterTest.get.warehouseList.size mustBe 0
                } else {
                  userAnswersAfterTest.get.warehouseList.size mustBe 1
                }
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

        setAnswers(
          emptyUserAnswers
            .copy(warehouseList = Map(indexOfWarehouseToBeRemoved -> Warehouse(None, ukAddress))))
        getAnswers(emptyUserAnswers.id).get.warehouseList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("removeWarehouseDetails" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("removeWarehouseDetails" + ".error.required")
            page.getElementById("warehouseToRemove").text() mustBe "foo, bar, wizz"
            getAnswers(emptyUserAnswers.id).get.warehouseList.size mustBe 1
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath(indexOfWarehouseToBeRemoved) - {

    userAnswersForUpdateRegisteredDetailsRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the Warehouse details controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehouseDetailsController.onPageLoad(CheckMode).url)
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            getAnswers(userAnswers.id).get.warehouseList.size mustBe 1
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehouseDetailsController.onPageLoad(CheckMode).url)
                val userAnswersAfterTest = getAnswers(userAnswers.id)
                val dataStoredForPage = userAnswersAfterTest.fold[Option[Boolean]](None)(_.get(RemoveWarehouseDetailsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
                if(yesSelected) {
                  userAnswersAfterTest.get.warehouseList.size mustBe 0
                } else {
                  userAnswersAfterTest.get.warehouseList.size mustBe 1
                }
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

        setAnswers(
          emptyUserAnswers
            .copy(warehouseList = Map(indexOfWarehouseToBeRemoved -> Warehouse(None, ukAddress))))
        getAnswers(emptyUserAnswers.id).get.warehouseList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            getAnswers(emptyUserAnswers.id).get.warehouseList.size mustBe 1
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("removeWarehouseDetails" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("removeWarehouseDetails" + ".error.required")
            page.getElementById("warehouseToRemove").text() mustBe "foo, bar, wizz"
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
  }
}
