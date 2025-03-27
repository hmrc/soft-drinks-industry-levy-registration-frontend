package controllers

import models.alf.AddressResponseForLookupState
import models.backend.Site
import models.{CheckMode, NormalMode, PackagingSiteName, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.WsTestClient
import services.AddressLookupState.PackingDetails

class PackagingSiteNameControllerISpec extends ControllerITTestHelper {

  val ref = "1234567890"
  val normalRoutePath = s"/packaging-site-name/$ref"
  val checkRoutePath = s"/change-packaging-site-name/$ref"

  val packagingSiteNameJsObject: collection.Map[String, JsValue] = Json.toJson(packagingSiteName).as[JsObject].value
  val packagingSiteNameMap: collection.Map[String, String] = {
    packagingSiteNameJsObject.map { case (fName, fValue) => fName -> fValue.as[String] }
  }
  val alfResponseForLookupState = AddressResponseForLookupState(ukAddress, PackingDetails, ref)
  val userAnswersWithAlfResponseForSdilId: UserAnswers = emptyUserAnswers.copy(
    alfResponseForLookupState = Some(alfResponseForLookupState))
  val userAnswersWithNoAlfResponseButPackingSiteWithSdilRef = emptyUserAnswers.copy(
    packagingSiteList = Map(ref -> Site(ukAddress, None, packagingSiteName.packagingSiteName, None)))
  val userAnswersWithPackagingSitesButNotForSdilRef = emptyUserAnswers.copy(
    packagingSiteList = Map("5432456" -> Site(ukAddress, None, packagingSiteName.packagingSiteName, None)))
  val modeWithPaths = Map(NormalMode -> normalRoutePath,
    CheckMode -> checkRoutePath)

  modeWithPaths.foreach { case (mode, path) =>
    "GET " + path - {
      "when the userAnswers contains alfResponseWithLookupState for the reference number" - {
        "should return OK and render the PackagingSiteName page with no data populated" in {
          build
            .commonPrecondition

          setAnswers(userAnswersWithAlfResponseForSdilId)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "What is your UK packaging site name? - Soft Drinks Industry Levy - GOV.UK"
              val inputFields = page.getElementsByClass("govuk-form-group")
              inputFields.size() mustBe 1
              inputFields.get(0).text() mustBe "What is your UK packaging site name?"
              inputFields.get(0).getElementById("packagingSiteName").hasAttr("value") mustBe false
            }
          }
        }
      }

      "when the userAnswers contains no alfResponseWithLookupState but contains a packaging site for the reference number" - {
        "should return OK and render the PackagingSiteName page with data populated" in {
          build
            .commonPrecondition

          setAnswers(userAnswersWithNoAlfResponseButPackingSiteWithSdilRef)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "What is your UK packaging site name? - Soft Drinks Industry Levy - GOV.UK"
              val inputFields = page.getElementsByClass("govuk-form-group")
              inputFields.size() mustBe 1
              inputFields.get(0).text() mustBe "What is your UK packaging site name?"
              inputFields.get(0).getElementById("packagingSiteName").hasAttr("value") mustBe true
              inputFields.get(0).getElementById("packagingSiteName").attr("value") mustBe packagingSiteName.packagingSiteName
            }
          }
        }
      }

      "when the useranswers contains packaging sites but none with sdilId and has no alfAddres" - {
        "must redirect to packagingSiteDetails" in {
          build
            .commonPrecondition

          setAnswers(userAnswersWithPackagingSitesButNotForSdilRef)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(mode).url)
            }
          }
        }
      }

      "when the useranswers contains no packaging sites or alfAddres" - {
        "must redirect to packAtBusinessAddress" in {
          build
            .commonPrecondition

          setAnswers(emptyUserAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(mode).url)
            }
          }
        }
      }
      testUnauthorisedUser(baseUrl + path)
      testAuthenticatedUserButNoUserAnswers(baseUrl + path)
    }

    "POST " + path - {
      "should add the packaging site and remove alfResponse from user answers if present and redirect to packaging site details" - {
        "when the user populates the trading name field with a valid value" - {
          "and the userAnswers contains alfResponseWithLookupState for the reference number" in {
            build
              .commonPrecondition

            setAnswers(userAnswersWithAlfResponseForSdilId)

            WsTestClient.withClient { client =>
              val result1 = createClientRequestPOST(client, baseUrl + path, Json.toJson(packagingSiteNameDiff))

              whenReady(result1) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(mode).url)
                val updatedUserAnswer = getAnswers(identifier).get
                updatedUserAnswer.alfResponseForLookupState mustBe None
                updatedUserAnswer.packagingSiteList mustBe Map(ref -> Site(ukAddress, None, packagingSiteNameDiff.packagingSiteName, None))
              }
            }
          }

          "and the userAnswers contains no alfResponseWithLookupState but has a packaging site for the reference number" in {
            build
              .commonPrecondition

            setAnswers(userAnswersWithNoAlfResponseButPackingSiteWithSdilRef)

            WsTestClient.withClient { client =>
              val result1 = createClientRequestPOST(client, baseUrl + path, Json.toJson(packagingSiteNameDiff))

              whenReady(result1) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(mode).url)
                val updatedUserAnswer = getAnswers(identifier).get
                updatedUserAnswer.alfResponseForLookupState mustBe None
                updatedUserAnswer.packagingSiteList mustBe Map(ref -> Site(ukAddress, None, packagingSiteNameDiff.packagingSiteName, None))
              }
            }
          }
        }
      }

      "should not update the database and redirect to packaging site details" - {
        "when the useranswers contains packaging sites but none with sdilId and has no alfAddress" in {
          build
            .commonPrecondition

          setAnswers(userAnswersWithPackagingSitesButNotForSdilRef)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestPOST(client, baseUrl + path, Json.toJson(packagingSiteNameDiff))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(mode).url)
            }
          }
        }
      }

      "should not update the database and redirect to pack at business address" - {
        "when the useranswers contains no packaging sites or alfAddress" in {
          build
            .commonPrecondition

          setAnswers(emptyUserAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestPOST(client, baseUrl + path, Json.toJson(packagingSiteNameDiff))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(mode).url)
            }
          }
        }
      }

      "should return 400 with required error" - {
        "when no questions are answered" in {
          build
            .commonPrecondition

          setAnswers(userAnswersWithAlfResponseForSdilId)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + path, Json.toJson(PackagingSiteName(""))
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title mustBe "Error: What is your UK packaging site name? - Soft Drinks Industry Levy - GOV.UK"
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first().getElementsByTag("li")
              errorSummaryList.size() mustBe packagingSiteNameMap.size
              packagingSiteNameMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
                val errorSummary = errorSummaryList.get(index)
                errorSummary
                  .select("a")
                  .attr("href") mustBe "#" + fieldName
                errorSummary.text() mustBe "Enter a packaging site name"
              }
            }
          }
        }
        packagingSiteNameMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
          "when no answer is given for field" + fieldName in {
            build
              .commonPrecondition

            setAnswers(userAnswersWithAlfResponseForSdilId)
            val invalidJson = packagingSiteNameMap.foldLeft(Json.obj()) { case (current, (fn, fv)) =>
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
                page.title mustBe "Error: What is your UK packaging site name? - Soft Drinks Industry Levy - GOV.UK"
                val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                  .first()
                errorSummaryList
                  .select("a")
                  .attr("href") mustBe "#" + fieldName
                errorSummaryList.text() mustBe "Enter a packaging site name"
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
