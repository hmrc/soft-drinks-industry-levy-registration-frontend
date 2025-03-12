package controllers

import models.OrganisationType.Partnership
import models.{NormalMode, OrganisationType}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.OrganisationTypePage
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.{FakeRequest, WsTestClient}

class OrganisationTypeIntegrationSpec extends ControllerITTestHelper {
  val normalRoutePath = "/organisation-type"
  val checkRoutePath = "/change-organisation-type"

  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())
  
  "GET" + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the OrganisationType page with no data populated" in {
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("organisationType.title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe OrganisationType.values.size

            OrganisationType.values.zipWithIndex.foreach { case (radio1, index1) =>
              radioInputs.get(index1).attr("value") mustBe radio1.toString
              radioInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    OrganisationType.values.zipWithIndex.foreach { case (radio, index) =>
      s"when the userAnswers contains data for the page with " + radio.toString + " selected" - {
        s"should return OK and render the page with " + radio.toString + " radio checked" in {
          `given`
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(OrganisationTypePage, radio).success.value

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("organisationType.title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe OrganisationType.values.size

              OrganisationType.values.zipWithIndex.foreach { case (radio1, index1) =>
                radioInputs.get(index1).attr("value") mustBe radio1.toString
                radioInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, messages("organisationType.title"))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the OrganisationType page with no data populated" in {
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("organisationType.title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe OrganisationType.values.size

            OrganisationType.values.zipWithIndex.foreach { case (radio1, index1) =>
              radioInputs.get(index1).attr("value") mustBe radio1.toString
              radioInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    OrganisationType.values.zipWithIndex.foreach { case (radio, index) =>
      s"when the userAnswers contains data for the page with " + radio.toString + " selected" - {
        s"should return OK and render the page with " + radio.toString + " radio checked" in {
          `given`
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(OrganisationTypePage, radio).success.value


          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("organisationType.title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe OrganisationType.values.size

              OrganisationType.values.zipWithIndex.foreach { case (radio1, index1) =>
                radioInputs.get(index1).attr("value") mustBe radio1.toString
                radioInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + checkRoutePath, messages("organisationType.title"))
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)

  }

  s"POST " + normalRoutePath - {
    OrganisationType.values.foreach {
      case radio if radio != Partnership=>
        "when the user selects " + radio.toString - {
          "should update the session with the new value and redirect to the how many litres globally" - {
            "when the session contains no data for page" in {
              `given`
                .commonPrecondition

              setAnswers(emptyUserAnswers)

              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, baseUrl + normalRoutePath, Json.obj("value" -> radio)
                )
                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.HowManyLitresGloballyController.onPageLoad(NormalMode).url)
                  val dataStoredForPage = getAnswers(identifier).fold[Option[OrganisationType]](None)(_.get(OrganisationTypePage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe radio
                }
              }
            }

            "when the session already contains data for page" in {
              `given`
                .commonPrecondition

              val userAnswers = emptyUserAnswers.set(OrganisationTypePage, radio).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, baseUrl + normalRoutePath, Json.obj("value" -> radio)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.HowManyLitresGloballyController.onPageLoad(NormalMode).url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[OrganisationType]](None)(_.get(OrganisationTypePage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe radio
                }
              }
            }
          }
        }
      case radio =>
        "when the user selects " + radio.toString - {
          "should update the session with the new value and redirect to the how many litres globally" - {
            "when the session contains no data for page" in {
              `given`
                .commonPrecondition

              setAnswers(emptyUserAnswers)

              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, baseUrl + normalRoutePath, Json.obj("value" -> radio)
                )
                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.CannotRegisterPartnershipController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(identifier).fold[Option[OrganisationType]](None)(_.get(OrganisationTypePage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe radio
                }
              }
            }

            "when the session already contains data for page" in {
              `given`
                .commonPrecondition

              val userAnswers = emptyUserAnswers.set(OrganisationTypePage, radio).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, baseUrl + normalRoutePath, Json.obj("value" -> radio)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.CannotRegisterPartnershipController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[OrganisationType]](None)(_.get(OrganisationTypePage))
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
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("organisationType" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe messages("organisationType" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    OrganisationType.values.foreach {
      case radio if radio != Partnership=>
        "when the user selects " + radio.toString - {
          "should update the session with the new value and redirect to the checkAnswers controller" - {
            "when the session contains no data for page" in {
              `given`
                .commonPrecondition

              setAnswers(emptyUserAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(radio))
                )
                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(identifier).fold[Option[OrganisationType]](None)(_.get(OrganisationTypePage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe radio
                }
              }
            }

            "when the session already contains data for page" in {
              `given`
                .commonPrecondition

              val userAnswers = emptyUserAnswers.set(OrganisationTypePage, radio).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(radio))
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[OrganisationType]](None)(_.get(OrganisationTypePage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe radio
                }
              }
            }
          }
        }
      case radio =>
        "when the user selects " + radio.toString - {
          "should update the session with the new value and redirect to the checkAnswers controller" - {
            "when the session contains no data for page" in {
              `given`
                .commonPrecondition

              setAnswers(emptyUserAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(radio))
                )
                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.CannotRegisterPartnershipController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(identifier).fold[Option[OrganisationType]](None)(_.get(OrganisationTypePage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe radio
                }
              }
            }

            "when the session already contains data for page" in {
              `given`
                .commonPrecondition

              val userAnswers = emptyUserAnswers.set(OrganisationTypePage, radio).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(radio))
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.CannotRegisterPartnershipController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[OrganisationType]](None)(_.get(OrganisationTypePage))
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
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("organisationType" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe messages("organisationType" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
