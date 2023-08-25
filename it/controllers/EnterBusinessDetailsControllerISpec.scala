package controllers

import models.backend.{Site, UkAddress}
import models.{Identify, NormalMode, RegisterState, SmallProducer, Warehouse}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.EnterBusinessDetailsPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

import scala.util.Random

class EnterBusinessDetailsControllerISpec extends ControllerITTestHelper {

  val path = "/enter-business-details"

  val enterBusinessDetails = Identify(utr = "0000000437", postcode = "GU14 8NL")

  val randomStringExceedingMaxLength = Random.nextString(10 + 1)

  val userAnswersPageSame = emptyUserAnswers.copy(registerState = RegisterState.RequiresBusinessDetails)
    .set(EnterBusinessDetailsPage, Identify(utr = "0000000437", postcode = "GU14 8NL")).success.value

  "GET " + path - {
    "when the userAnswers contains no data" - {
      "should return OK and render the EnterBusinessDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers.copy(registerState = RegisterState.RequiresBusinessDetails))

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + path)

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
  }

  s"POST " + path - {
    "when the user answers the question" - {
      "should update the session with the new values and redirect to the Verify controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition
          given.sdilBackend.retrieveRosm("0000000437")
          given.sdilBackend.checkPendingQueueDoesntExist("0000000437")

          setAnswers(emptyUserAnswers.copy(registerState = RegisterState.RequiresBusinessDetails))
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + path, Json.obj("utr" -> "0000000437", "postcode" -> "GU14 8NL")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.VerifyController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(userAnswersPageSame.id).fold[Option[Identify]](None)(_.get(EnterBusinessDetailsPage))

              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe enterBusinessDetails
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition
          given.sdilBackend.retrieveRosm("0000000437")
          given.sdilBackend.checkPendingQueueDoesntExist("0000000437")

          setAnswers(userAnswersPageSame)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + path, Json.obj("utr" -> "0000000437", "postcode" -> "GU14 8NL")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.VerifyController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(userAnswersPageSame.id).fold[Option[Identify]](None)(_.get(EnterBusinessDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe enterBusinessDetails
            }
          }
        }
        "when the session already contains data that does not matches the data the user entered, should reset user answers" in {
          val userAnswersWithNonIdenticalData = {
            emptyUserAnswers
              .set(EnterBusinessDetailsPage, Identify("0000001611", "AA1 1AA")).success.get
              .copy(
                registerState = RegisterState.RequiresBusinessDetails,
                address = Some(UkAddress(List.empty,"")),
                smallProducerList = List(SmallProducer("","", (0,0))),
                packagingSiteList = Map("" -> Site(UkAddress(List.empty,""),None,None,None)),
                warehouseList = Map("" -> Warehouse(None, UkAddress(List.empty,""))),
                submittedOn = None
              )
          }
          given
            .commonPrecondition

          setAnswers(userAnswersWithNonIdenticalData)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + path, Json.obj("utr" -> "0000001611", "postcode" -> "GU14 8NL")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.VerifyController.onPageLoad(NormalMode).url)
              val updatedAnswers = getAnswers(userAnswersWithNonIdenticalData.id)
              val dataStoredForPage = updatedAnswers.fold[Option[Identify]](None)(_.get(EnterBusinessDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe Identify(utr = "0000001611", postcode = "GU14 8NL")
              updatedAnswers.get.address mustBe None
              updatedAnswers.get.warehouseList mustBe Map.empty
              updatedAnswers.get.packagingSiteList mustBe Map.empty
              updatedAnswers.get.submittedOn mustBe None
              updatedAnswers.get.smallProducerList mustBe List.empty
              updatedAnswers.get.data mustBe Json.obj("enterBusinessDetails" -> Json.obj("utr" -> "0000001611", "postcode" -> "GU14 8NL"))
            }
          }
        }
        "when the session already contains data that does matches the data the user entered, should NOT reset user answers" in {
          val userAnswersWithIdenticalData = {
            emptyUserAnswers
              .set(EnterBusinessDetailsPage, Identify("0000001611", "GU14 8NL")).success.get
              .copy(
                registerState = RegisterState.RequiresBusinessDetails,
                address = Some(UkAddress(List.empty,"")),
                smallProducerList = List(SmallProducer("","", (0,0))),
                packagingSiteList = Map("" -> Site(UkAddress(List.empty,""),None,None,None)),
                warehouseList = Map("" -> Warehouse(None, UkAddress(List.empty,""))),
                submittedOn = None
              )
          }
          given
            .commonPrecondition

          setAnswers(userAnswersWithIdenticalData)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + path, Json.obj("utr" -> "0000001611", "postcode" -> "GU14 8NL")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.VerifyController.onPageLoad(NormalMode).url)
              val updatedAnswers = getAnswers(userAnswersWithIdenticalData.id)
              updatedAnswers.get.address mustBe userAnswersWithIdenticalData.address
              updatedAnswers.get.warehouseList mustBe userAnswersWithIdenticalData.warehouseList
              updatedAnswers.get.packagingSiteList mustBe userAnswersWithIdenticalData.packagingSiteList
              updatedAnswers.get.submittedOn mustBe userAnswersWithIdenticalData.submittedOn
              updatedAnswers.get.smallProducerList mustBe userAnswersWithIdenticalData.smallProducerList
              updatedAnswers.get.data mustBe userAnswersWithIdenticalData.data
            }
          }
        }
      }
    }

    "should return 400 with utr max length error" - {
      "when the question is answered with incorrect data" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers.copy(registerState = RegisterState.RequiresBusinessDetails))
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + path, Json.obj("utr" -> randomStringExceedingMaxLength, "postcode" -> "GU14 8NL")
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
  }
}
