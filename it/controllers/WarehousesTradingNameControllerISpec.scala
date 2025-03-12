package controllers

import models.alf.AddressResponseForLookupState
import models.{CheckMode, NormalMode, UserAnswers, Warehouse, WarehousesTradingName}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.WsTestClient
import services.AddressLookupState.WarehouseDetails

class WarehousesTradingNameControllerISpec extends ControllerITTestHelper {

  val ref = "1234567890"
  val normalRoutePath = s"/warehouses-trading-name/$ref"
  val checkRoutePath = s"/change-warehouses-trading-name/$ref"

  val warehousesTradingNameJsObject: collection.Map[String, JsValue] = Json.toJson(warehousesTradingName).as[JsObject].value
  val warehousesTradingNameMap: collection.Map[String, String] = {
    warehousesTradingNameJsObject.map { case (fName, fValue) => fName -> fValue.as[String] }
  }
  val alfResponseForLookupState = AddressResponseForLookupState(ukAddress, WarehouseDetails, ref)
  val userAnswersWithAlfResponseForSdilId: UserAnswers = emptyUserAnswers.copy(
    alfResponseForLookupState = Some(alfResponseForLookupState))
  val userAnswersWithNoAlfResponseButWarehouseWithSdilRef = emptyUserAnswers.copy(
    warehouseList = Map(ref -> Warehouse(warehousesTradingName.warehouseTradingName, ukAddress)))
  val userAnswersWithWarehousesButNotForSdilRef = emptyUserAnswers.copy(
    warehouseList = Map("5432456" -> Warehouse(warehousesTradingName.warehouseTradingName, ukAddress)))
  val modeWithPaths = Map(NormalMode -> normalRoutePath,
    CheckMode -> checkRoutePath)

  modeWithPaths.foreach { case (mode, path) =>
    "GET " + path - {
      "when the userAnswers contains alfResponseWithLookupState for the reference number" - {
        "should return OK and render the WarehouseTradingName page with no data populated" in {
          `given`
            .commonPrecondition

          setAnswers(userAnswersWithAlfResponseForSdilId)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "What is your UK warehouse trading name? - Soft Drinks Industry Levy - GOV.UK"
              val inputFields = page.getElementsByClass("govuk-form-group")
              inputFields.size() mustBe 1
              inputFields.get(0).text() mustBe "What is your UK warehouse trading name?"
              inputFields.get(0).getElementById("warehouseTradingName").hasAttr("value") mustBe false
            }
          }
        }
      }

      "when the userAnswers contains no alfResponseWithLookupState but contains a packaging site for the reference number" - {
        "should return OK and render the WarehouseTradingName page with data populated" in {
          `given`
            .commonPrecondition

          setAnswers(userAnswersWithNoAlfResponseButWarehouseWithSdilRef)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "What is your UK warehouse trading name? - Soft Drinks Industry Levy - GOV.UK"
              val inputFields = page.getElementsByClass("govuk-form-group")
              inputFields.size() mustBe 1
              inputFields.get(0).text() mustBe "What is your UK warehouse trading name?"
              inputFields.get(0).getElementById("warehouseTradingName").hasAttr("value") mustBe true
              inputFields.get(0).getElementById("warehouseTradingName").attr("value") mustBe warehousesTradingName.warehouseTradingName
            }
          }
        }
      }

      "when the useranswers contains warehouses but none with sdilId and has no alfAddres" - {
        "must redirect to warehouseDetails" in {
          `given`
            .commonPrecondition

          setAnswers(userAnswersWithWarehousesButNotForSdilRef)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseDetailsController.onPageLoad(mode).url)
            }
          }
        }
      }

      "when the useranswers contains no warehouses or alfAddres" - {
        "must redirect to AskSecondaryWarehouse" in {
          `given`
            .commonPrecondition

          setAnswers(emptyUserAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(mode).url)
            }
          }
        }
      }
      testUnauthorisedUser(baseUrl + path)
      testAuthenticatedUserButNoUserAnswers(baseUrl + path)
    }

    "POST " + path - {
      "should add the warehouse and remove alfResponse from user answers if present and redirect to warehouse details" - {
        "when the user populates the trading name field with a valid value" - {
          "and the userAnswers contains alfResponseWithLookupState for the reference number" in {
            `given`
              .commonPrecondition

            setAnswers(userAnswersWithAlfResponseForSdilId)

            WsTestClient.withClient { client =>
              val result1 = createClientRequestPOST(client, baseUrl + path, Json.toJson(warehousesTradingNameDiff))

              whenReady(result1) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseDetailsController.onPageLoad(mode).url)
                val updatedUserAnswer = getAnswers(identifier).get
                updatedUserAnswer.alfResponseForLookupState mustBe None
                updatedUserAnswer.warehouseList mustBe Map(ref -> Warehouse(warehousesTradingNameDiff.warehouseTradingName, ukAddress))
              }
            }
          }

          "and the userAnswers contains no alfResponseWithLookupState but has a warehouse for the reference number" in {
            `given`
              .commonPrecondition

            setAnswers(userAnswersWithNoAlfResponseButWarehouseWithSdilRef)

            WsTestClient.withClient { client =>
              val result1 = createClientRequestPOST(client, baseUrl + path, Json.toJson(warehousesTradingNameDiff))

              whenReady(result1) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseDetailsController.onPageLoad(mode).url)
                val updatedUserAnswer = getAnswers(identifier).get
                updatedUserAnswer.alfResponseForLookupState mustBe None
                updatedUserAnswer.warehouseList mustBe Map(ref -> Warehouse(warehousesTradingNameDiff.warehouseTradingName, ukAddress))
              }
            }
          }
        }
      }

      "should not update the database and redirect to warehouse details" - {
        "when the useranswers contains waehouses but none with sdilId and has no alfAddress" in {
          `given`
            .commonPrecondition

          setAnswers(userAnswersWithWarehousesButNotForSdilRef)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestPOST(client, baseUrl + path, Json.toJson(warehousesTradingNameDiff))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseDetailsController.onPageLoad(mode).url)
            }
          }
        }
      }

      "should not update the database and redirect to ask secondary warehouse" - {
        "when the useranswers contains no warehouses or alfAddress" in {
          `given`
            .commonPrecondition

          setAnswers(emptyUserAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestPOST(client, baseUrl + path, Json.toJson(warehousesTradingNameDiff))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(mode).url)
            }
          }
        }
      }

      "should return 400 with required error" - {
        "when no questions are answered" in {
          `given`
            .commonPrecondition

          setAnswers(userAnswersWithAlfResponseForSdilId)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + path, Json.toJson(WarehousesTradingName(""))
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title mustBe "Error: What is your UK warehouse trading name? - Soft Drinks Industry Levy - GOV.UK"
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first().getElementsByTag("li")
              errorSummaryList.size() mustBe warehousesTradingNameMap.size
              warehousesTradingNameMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
                val errorSummary = errorSummaryList.get(index)
                errorSummary
                  .select("a")
                  .attr("href") mustBe "#" + fieldName
                errorSummary.text() mustBe "Enter a warehouse trading name"
              }
            }
          }
        }
        warehousesTradingNameMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
          "when no answer is given for field" + fieldName in {
            `given`
              .commonPrecondition

            setAnswers(userAnswersWithAlfResponseForSdilId)
            val invalidJson = warehousesTradingNameMap.foldLeft(Json.obj()) { case (current, (fn, fv)) =>
              val fieldValue = if (fn == fieldName) {
                ""
              } else {
                fv
              }
              current ++ Json.obj(fn -> fieldValue)
            }
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + path, invalidJson
              )

              whenReady(result) { res =>
                res.status mustBe 400
                val page = Jsoup.parse(res.body)
                page.title mustBe "Error: What is your UK warehouse trading name? - Soft Drinks Industry Levy - GOV.UK"
                val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                  .first()
                errorSummaryList
                  .select("a")
                  .attr("href") mustBe "#" + fieldName
                errorSummaryList.text() mustBe "Enter a warehouse trading name"
              }
            }
          }
        }
      }

      testUnauthorisedUser(baseUrl + path, Some(Json.obj("value" -> "true")))
      testAuthenticatedUserButNoUserAnswers(baseUrl + path, Some(Json.obj("value" -> "true")))
    }
  }
}
