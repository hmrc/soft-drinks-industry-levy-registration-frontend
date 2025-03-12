package controllers

import models.NormalMode
import models.backend.{Site, UkAddress}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.PackagingSiteDetailsPage
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.{FakeRequest, WsTestClient}

class RemovePackagingSiteDetailsControllerISpec extends ControllerITTestHelper {
  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())
  
  val normalRoutePath = (ref: String)  => s"/packaging-site-details/remove/$ref"
  val ref: String = "12345678"
  val packagingSite: Map[String, Site] = Map(ref -> Site(
    UkAddress(List("a", "b"), "c"),
    None,
    aTradingName,
    None))
  val updatedUserAnswers = emptyUserAnswers.copy(packagingSiteList = packagingSiteListWith3)

  "GET " + normalRoutePath("ref") - {
    "when there is 1 or few packaging sites in the packaging site list" - {
      s"should redirect to the $PackagingSiteDetailsPage" in {
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers.copy(packagingSiteList = packagingSite))

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath(ref))

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
          }
        }
      }
    }

    "when the userAnswers contains no data" - {
      "should return OK and render the RemovePackagingSiteDetails page with no data populated" in {
        `given`
          .commonPrecondition

        setAnswers(updatedUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath(ref))

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "Are you sure you want to remove this packaging site? - Soft Drinks Industry Levy - GOV.UK"
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

    testOtherSuccessUserTypes(baseUrl + normalRoutePath(ref), messages("Are you sure you want to remove this packaging site?"), ua = updatedUserAnswers)
    testUnauthorisedUser(baseUrl + normalRoutePath(ref))
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath(ref))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath(ref))
  }

  s"POST " + normalRoutePath - {
    "when the user selects true" - {
      "should remove the packaging site details associated with the ref" in {
        `given`
          .commonPrecondition

        setAnswers(updatedUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath(ref), Json.obj("value" -> "true")
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
            val answersAfterSubmission = getAnswers(updatedUserAnswers.id).get
            answersAfterSubmission.packagingSiteList.isEmpty mustBe false
            answersAfterSubmission.data mustBe Json.obj()
          }
        }
      }
    }
    "when the user selects false" - {
      "should NOT remove the packaging site details associated with the ref" in {
        `given`
          .commonPrecondition

        setAnswers(updatedUserAnswers)
        getAnswers(updatedUserAnswers.id).get.packagingSiteList.size mustBe 3

        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath(ref), Json.obj("value" -> "false")
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
            val answersAfterSubmission = getAnswers(updatedUserAnswers.id).get
            answersAfterSubmission.packagingSiteList.size mustBe 3
            answersAfterSubmission.data mustBe Json.obj()
          }
        }
      }
    }
  }

  "when the user does not select yes or no" - {
    "should return 400 with required error" in {
      `given`
        .commonPrecondition

      setAnswers(updatedUserAnswers)
      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(
          client, baseUrl + normalRoutePath(ref), Json.obj("value" -> "")
        )

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.title must include("Error: " + messages("removePackagingSiteDetails" + ".title"))
          val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
            .first()
          errorSummary
            .select("a")
            .attr("href") mustBe "#value"
          errorSummary.text() mustBe messages("removePackagingSiteDetails" + ".error.required")
        }
      }
    }
  }

  testUnauthorisedUser(baseUrl + normalRoutePath(ref), Some(Json.obj("value" -> "true")))
  testUserWhoIsUnableToRegister(baseUrl + normalRoutePath(ref), Some(Json.obj("value" -> "true")))
  testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath(ref), Some(Json.obj("value" -> "true")))

}