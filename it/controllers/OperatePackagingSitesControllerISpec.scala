package controllers

import models.{CheckMode, LitresInBands, NormalMode, RegisterState, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import pages.{HowManyOperatePackagingSitesPage, OperatePackagingSitesPage}
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.{FakeRequest, WsTestClient}
import org.scalatestplus.mockito.MockitoSugar.mock
import testSupport.preConditions.PreconditionHelpers

class OperatePackagingSitesControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/operate-packaging-sites"
  val checkRoutePath = "/change-operate-packaging-sites"

  override val preconditionHelpers: PreconditionHelpers = mock[PreconditionHelpers]
  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())
  
  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the OperatePackagingSites page with no data populated" in {
        preconditionHelpers
          .commonPrecondition

        setAnswers(emptyUserAnswers)(using timeout)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("operatePackagingSites" + ".title"))
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

    userAnswersForOperatePackagingSitesPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          preconditionHelpers
            .commonPrecondition

          setAnswers(userAnswers)(using timeout)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("operatePackagingSites" + ".title"))
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
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, messages("operatePackagingSites" + ".title"))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the OperatePackagingSites page with no data populated" in {
        preconditionHelpers
          .commonPrecondition

        setAnswers(emptyUserAnswers)(using timeout)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("operatePackagingSites" + ".title"))
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

    userAnswersForOperatePackagingSitesPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          preconditionHelpers
            .commonPrecondition

          setAnswers(userAnswers)(using timeout)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("operatePackagingSites" + ".title"))
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

    testOtherSuccessUserTypes(baseUrl + checkRoutePath, messages("operatePackagingSites" + ".title"))
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    userAnswersForOperatePackagingSitesPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the OperatePackagingSites/ContractPacker controller" - {
          "when the session contains no data for page" in {
            preconditionHelpers
              .commonPrecondition

            setAnswers(emptyUserAnswers)(using timeout)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if (yesSelected) {
                  routes.HowManyOperatePackagingSitesController.onPageLoad(NormalMode).url
                } else {
                  routes.ContractPackingController.onPageLoad(NormalMode).url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id)(using timeout).fold[Option[Boolean]](None)(_.get(OperatePackagingSitesPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            preconditionHelpers
              .commonPrecondition

            setAnswers(userAnswers)(using timeout)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if (yesSelected) {
                  routes.HowManyOperatePackagingSitesController.onPageLoad(NormalMode).url
                } else {
                  routes.ContractPackingController.onPageLoad(NormalMode).url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id)(using timeout).fold[Option[Boolean]](None)(_.get(OperatePackagingSitesPage))
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
            page.title must include("Error: " + messages("operatePackagingSites" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("operatePackagingSites" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    userAnswersForOperatePackagingSitesPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        val yesSelected = key == "yes"
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            preconditionHelpers
              .commonPrecondition

            setAnswers(emptyUserAnswers)(using timeout)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if(yesSelected) {
                  routes.HowManyOperatePackagingSitesController.onPageLoad(CheckMode).url
                } else {
                  routes.CheckYourAnswersController.onPageLoad.url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id)(using timeout).fold[Option[Boolean]](None)(_.get(OperatePackagingSitesPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            preconditionHelpers
              .commonPrecondition

            setAnswers(userAnswers)(using timeout)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if (yesSelected) {
                  routes.HowManyOperatePackagingSitesController.onPageLoad(CheckMode).url
                } else {
                  routes.CheckYourAnswersController.onPageLoad.url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id)(using timeout).fold[Option[Boolean]](None)(_.get(OperatePackagingSitesPage))
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
            page.title must include("Error: " + messages("operatePackagingSites" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("operatePackagingSites" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }

  "POST must clear litres data when the session already contained data but no is selected" in {
    preconditionHelpers
      .commonPrecondition

    val previouslyFilledAnswers =
      UserAnswers("some-id", RegisterState.RegisterWithAuthUTR, Json.obj(
        OperatePackagingSitesPage.toString -> true,
        HowManyOperatePackagingSitesPage.toString -> Json.obj("lowBand" -> "123", "highBand" -> "123")))

    setAnswers(previouslyFilledAnswers)(using timeout)
    WsTestClient.withClient { client =>
      val result = createClientRequestPOST(
        client, baseUrl + normalRoutePath, Json.obj("value" -> false)
      )

      whenReady(result) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ContractPackingController.onPageLoad(NormalMode).url)
        val litresData = getAnswers(previouslyFilledAnswers.id)(using timeout).fold[Option[LitresInBands]](None)(_.get(HowManyOperatePackagingSitesPage))
        litresData mustBe None
      }
    }
  }

}
