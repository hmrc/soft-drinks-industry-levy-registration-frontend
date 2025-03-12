package controllers

import models.Verify.{No, YesNewAddress, YesRegister}
import models.backend.UkAddress
import models.{NormalMode, Verify}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import pages.VerifyPage
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.{FakeRequest, WsTestClient}
import org.scalatestplus.mockito.MockitoSugar.mock
import testSupport.preConditions.PreconditionHelpers

class VerifyControllerISpec extends ControllerITTestHelper {

  override val preconditionHelpers: PreconditionHelpers = mock[PreconditionHelpers]
  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())
  
  val normalRoutePath = "/verify"
  val checkRoutePath = "/change-verify"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Verify page with no data populated" in {
        preconditionHelpers
          .commonPrecondition

        setAnswers(emptyUserAnswers)(using timeout)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("verify" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe Verify.values.size
            page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove Guildford GU14 8NL"
            page.getElementById("utrField").text() mustBe "0000001611:"
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
          preconditionHelpers
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(VerifyPage, radio).success.value

          setAnswers(userAnswers)(using timeout)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("verify" + ".title"))
              page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove Guildford GU14 8NL"
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe Verify.values.size
              page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove Guildford GU14 8NL"
              page.getElementById("utrField").text() mustBe "0000001611:"
              Verify.values.zipWithIndex.foreach { case (radio1, index1) =>
                radioInputs.get(index1).attr("value") mustBe radio1.toString
                radioInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, messages("verify" + ".title"))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Verify page with no data populated" in {
        preconditionHelpers
          .commonPrecondition

        setAnswers(emptyUserAnswers)(using timeout)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("verify" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe Verify.values.size
            page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove Guildford GU14 8NL"
            page.getElementById("utrField").text() mustBe "0000001611:"
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
          preconditionHelpers
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(VerifyPage, radio).success.value


          setAnswers(userAnswers)(using timeout)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("verify" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe Verify.values.size
              page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove Guildford GU14 8NL"
              page.getElementById("utrField").text() mustBe "0000001611:"
              Verify.values.zipWithIndex.foreach { case (radio1, index1) =>
                radioInputs.get(index1).attr("value") mustBe radio1.toString
                radioInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + checkRoutePath, messages("verify" + ".title"))
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)

  }

  s"POST " + normalRoutePath - {
    s"when the user selects $YesRegister" - {
      "should update the session with the value and set business address as rosm address and redirect to the Organisation Type controller" - {
        "when the session contains no data for page" in {
          preconditionHelpers
            .commonPrecondition

          setAnswers(emptyUserAnswers)(using timeout)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> Json.toJson(YesRegister.toString))
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.OrganisationTypeController.onPageLoad(NormalMode).url)
              val userAnswersAfterPOST = getAnswers(identifier)(using timeout)
              val dataStoredForPage = userAnswersAfterPOST.fold[Option[Verify]](None)(_.get(VerifyPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe YesRegister
              userAnswersAfterPOST.get.address.get mustBe UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL", None)
            }
          }
        }

        "when the session already contains data for page" in {
          preconditionHelpers
            .commonPrecondition

          val userAnswers = {
            emptyUserAnswers.set(VerifyPage, YesNewAddress).success.value
              .copy(address = Some(UkAddress(List.empty,"")))
          }

          setAnswers(userAnswers)(using timeout)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> Json.toJson(YesRegister.toString))
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.OrganisationTypeController.onPageLoad(NormalMode).url)
              val userAnswersAfterPOST = getAnswers(identifier)(using timeout)
              val dataStoredForPage = userAnswersAfterPOST.fold[Option[Verify]](None)(_.get(VerifyPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe YesRegister
              userAnswersAfterPOST.get.address.get mustBe UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL", None)
            }
          }
        }
      }
    }
    s"when the user selects $YesNewAddress" - {
      "should update the session with the new value and redirect to the ALF" - {
        "when the session contains no data for page" in {
          preconditionHelpers
            .commonPrecondition
            .alf.getSuccessResponseFromALFInit("foo")

          setAnswers(emptyUserAnswers)(using timeout)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> Json.toJson(YesNewAddress.toString))
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some("foo")
              val userAnswersAfterPOST = getAnswers(identifier)(using timeout)
              val dataStoredForPage = userAnswersAfterPOST.fold[Option[Verify]](None)(_.get(VerifyPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe YesNewAddress
              userAnswersAfterPOST.get.address.isEmpty mustBe true

            }
          }
        }

        "when the session already contains data for page" in {
          preconditionHelpers
            .commonPrecondition
            .alf.getSuccessResponseFromALFInit("foo")

          val userAnswers = {
            emptyUserAnswers.set(VerifyPage, YesRegister).success.value
              .copy(address = Some(UkAddress(List.empty,"")))
          }

          setAnswers(userAnswers)(using timeout)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> Json.toJson(YesNewAddress.toString))
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some("foo")
              val userAnswersAfterPOST = getAnswers(identifier)(using timeout)
              val dataStoredForPage = userAnswersAfterPOST.fold[Option[Verify]](None)(_.get(VerifyPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe YesNewAddress
              userAnswersAfterPOST.get.address.isEmpty mustBe false
            }
          }
        }
      }
    }
    s"when the user selects $No" - {
      "should NOT update the session with the new value and redirect to the Auth sign out controller" in {
        preconditionHelpers
            .commonPrecondition

          setAnswers(emptyUserAnswers)(using timeout)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> Json.toJson(No.toString))
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(auth.routes.AuthController.signOutNoSurvey.url)

            }
          }

      }
    }

    "when the user does not select an option" - {
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
            page.title must include("Error: " + messages("verify" + ".title"))
            page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove Guildford GU14 8NL"
            page.getElementById("utrField").text() mustBe "0000001611:"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe "Select to confirm or change these business details"
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {

      s"when the user selects $YesRegister" - {
        "should update the session with the new value and wipe the business address and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            preconditionHelpers
              .commonPrecondition

            setAnswers(emptyUserAnswers)(using timeout)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(YesRegister.toString))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
                val userAnswersAfterPOST = getAnswers(identifier)(using timeout)
                val dataStoredForPage = userAnswersAfterPOST.fold[Option[Verify]](None)(_.get(VerifyPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe YesRegister
                userAnswersAfterPOST.get.address.get mustBe UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL", None)

              }
            }
          }

          "when the session already contains data for page" in {
            preconditionHelpers
              .commonPrecondition

            val userAnswers = emptyUserAnswers.set(VerifyPage, YesRegister).success.value

            setAnswers(userAnswers)(using timeout)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(YesRegister.toString))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
                val userAnswersAfterPOST = getAnswers(identifier)(using timeout)
                val dataStoredForPage = userAnswersAfterPOST.fold[Option[Verify]](None)(_.get(VerifyPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe YesRegister
                userAnswersAfterPOST.get.address.get mustBe UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL", None)
              }
            }
          }
        }
      }
    s"when the user selects $YesNewAddress" - {
      "should update the session with the new value and redirect to the ALF" - {
        "when the session contains no data for page" in {
          preconditionHelpers
            .commonPrecondition
            .alf.getSuccessResponseFromALFInit("foo")

          setAnswers(emptyUserAnswers)(using timeout)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(YesNewAddress.toString))
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some("foo")
              val userAnswersAfterPOST = getAnswers(identifier)(using timeout)
              val dataStoredForPage = userAnswersAfterPOST.fold[Option[Verify]](None)(_.get(VerifyPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe YesNewAddress
              userAnswersAfterPOST.get.address.isEmpty mustBe true

            }
          }
        }

        "when the session already contains data for page" in {
          preconditionHelpers
            .commonPrecondition
            .alf.getSuccessResponseFromALFInit("foo")

          val userAnswers = {
            emptyUserAnswers.set(VerifyPage, YesRegister).success.value
              .copy(address = Some(UkAddress(List.empty,"")))
          }

          setAnswers(userAnswers)(using timeout)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(YesNewAddress.toString))
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some("foo")
              val userAnswersAfterPOST = getAnswers(identifier)(using timeout)
              val dataStoredForPage = userAnswersAfterPOST.fold[Option[Verify]](None)(_.get(VerifyPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe YesNewAddress
              userAnswersAfterPOST.get.address.isEmpty mustBe false
            }
          }
        }
      }
    }
    s"when the user selects $No" - {
      "should NOT update the session with the new value and redirect to the Auth sign out controller" in {
        preconditionHelpers
          .commonPrecondition

        setAnswers(emptyUserAnswers)(using timeout)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(No.toString))
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(auth.routes.AuthController.signOutNoSurvey.url)

          }
        }
      }
    }

    "when the user does not select and option" - {
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
            page.title must include("Error: " + messages("verify" + ".title"))
            page.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove Guildford GU14 8NL"
            page.getElementById("utrField").text() mustBe "0000001611:"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe "Select to confirm or change these business details"
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
