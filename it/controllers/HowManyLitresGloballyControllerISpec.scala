package controllers

import models.HowManyLitresGlobally.{Large, Small}
import models.{HowManyLitresGlobally, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.HowManyLitresGloballyPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class HowManyLitresGloballyControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/how-many-litres-globally"
  val checkRoutePath = "/change-how-many-litres-globally"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the HowManyLitresGlobally page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("howManyLitresGlobally" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe HowManyLitresGlobally.values.size

            HowManyLitresGlobally.values.zipWithIndex.foreach { case (radio1, index1) =>
              radioInputs.get(index1).attr("value") mustBe radio1.toString
              radioInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    HowManyLitresGlobally.values.zipWithIndex.foreach { case (radio, index) =>
      s"when the userAnswers contains data for the page with " + radio.toString + " selected" - {
        s"should return OK and render the page with " + radio.toString + " radio checked" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(HowManyLitresGloballyPage, radio).success.value

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("howManyLitresGlobally" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe HowManyLitresGlobally.values.size

              HowManyLitresGlobally.values.zipWithIndex.foreach { case (radio1, index1) =>
                radioInputs.get(index1).attr("value") mustBe radio1.toString
                radioInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, Messages("howManyLitresGlobally" + ".title"))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testWhoIsUnableToRegisterWithGivenUtr(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the HowManyLitresGlobally page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("howManyLitresGlobally" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe HowManyLitresGlobally.values.size

            HowManyLitresGlobally.values.zipWithIndex.foreach { case (radio1, index1) =>
              radioInputs.get(index1).attr("value") mustBe radio1.toString
              radioInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    HowManyLitresGlobally.values.zipWithIndex.foreach { case (radio, index) =>
      s"when the userAnswers contains data for the page with " + radio.toString + " selected" - {
        s"should return OK and render the page with " + radio.toString + " radio checked" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(HowManyLitresGloballyPage, radio).success.value


          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("howManyLitresGlobally" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe HowManyLitresGlobally.values.size

              HowManyLitresGlobally.values.zipWithIndex.foreach { case (radio1, index1) =>
                radioInputs.get(index1).attr("value") mustBe radio1.toString
                radioInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + checkRoutePath, Messages("howManyLitresGlobally" + ".title"))
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testWhoIsUnableToRegisterWithGivenUtr(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)

  }

  s"POST " + normalRoutePath - {
    HowManyLitresGlobally.values.foreach {
      case radio if radio == HowManyLitresGlobally.Large =>
      "when the user selects " + radio.toString - {
        "should update the session with the new value and redirect to the operate packaging sites" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> radio)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.OperatePackagingSitesController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(identifier).fold[Option[HowManyLitresGlobally]](None)(_.get(HowManyLitresGloballyPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe radio
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswers.set(HowManyLitresGloballyPage, radio).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> radio)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.OperatePackagingSitesController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[HowManyLitresGlobally]](None)(_.get(HowManyLitresGloballyPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe radio
              }
            }
          }
        }
      }
      case radio if radio == HowManyLitresGlobally.Small =>
        "when the user selects " + radio.toString - {
          "should update the session with the new value and redirect to the operate packaging sites" - {
            "when the session contains no data for page" in {
              given
                .commonPrecondition

              setAnswers(emptyUserAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, baseUrl + normalRoutePath, Json.obj("value" -> radio)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ThirdPartyPackagersController.onPageLoad(NormalMode).url)
                  val dataStoredForPage = getAnswers(identifier).fold[Option[HowManyLitresGlobally]](None)(_.get(HowManyLitresGloballyPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe radio
                }
              }
            }

            "when the session already contains data for page" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswers.set(HowManyLitresGloballyPage, radio).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, baseUrl + normalRoutePath, Json.obj("value" -> radio)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ThirdPartyPackagersController.onPageLoad(NormalMode).url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[HowManyLitresGlobally]](None)(_.get(HowManyLitresGloballyPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe radio
                }
              }
            }
          }
        }
      case radio =>
        "when the user selects " + radio.toString - {
          "should update the session with the new value and redirect to the operate packaging sites" - {
            "when the session contains no data for page" in {
              given
                .commonPrecondition

              setAnswers(emptyUserAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, baseUrl + normalRoutePath, Json.obj("value" -> radio)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ContractPackingController.onPageLoad(NormalMode).url)
                  val dataStoredForPage = getAnswers(identifier).fold[Option[HowManyLitresGlobally]](None)(_.get(HowManyLitresGloballyPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe radio
                }
              }
            }

            "when the session already contains data for page" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswers.set(HowManyLitresGloballyPage, radio).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, baseUrl + normalRoutePath, Json.obj("value" -> radio)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.ContractPackingController.onPageLoad(NormalMode).url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[HowManyLitresGlobally]](None)(_.get(HowManyLitresGloballyPage))
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
            page.title must include("Error: " + Messages("howManyLitresGlobally" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe Messages("howManyLitresGlobally" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testWhoIsUnableToRegisterWithGivenUtr(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    HowManyLitresGlobally.values.foreach { case selectedValue =>
      s"when the user selects $selectedValue" - {
        HowManyLitresGlobally.values.foreach { case previousSelectedValue =>
          if (previousSelectedValue == selectedValue) {
            s"that is the same as the answer stored in useranswers" - {
              "should update the database and redirect to check your answers" in {
                given
                  .commonPrecondition

                val userAnswers = emptyUserAnswers.set(HowManyLitresGloballyPage, previousSelectedValue).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(selectedValue))
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[HowManyLitresGlobally]](None)(_.get(HowManyLitresGloballyPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe selectedValue
                  }
                }
              }
            }
          } else {
            s"and the user previously selected $previousSelectedValue" - {
              val expectedUrl = selectedValue match {
                case Large => routes.OperatePackagingSitesController.onPageLoad(NormalMode).url
                case Small => routes.ThirdPartyPackagersController.onPageLoad(NormalMode).url
                case _ => routes.ContractPackingController.onPageLoad(NormalMode).url
              }
              s"should update the database and redirect to $expectedUrl " in {
                given
                  .commonPrecondition

                val userAnswers = emptyUserAnswers.set(HowManyLitresGloballyPage, previousSelectedValue).success.value

                setAnswers(userAnswers)
                WsTestClient.withClient { client =>
                  val result = createClientRequestPOST(
                    client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(selectedValue))
                  )

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                    val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[HowManyLitresGlobally]](None)(_.get(HowManyLitresGloballyPage))
                    dataStoredForPage.nonEmpty mustBe true
                    dataStoredForPage.get mustBe selectedValue
                  }
                }
              }
            }
          }
        }
        val expectedUrl = selectedValue match {
          case Large => routes.OperatePackagingSitesController.onPageLoad(NormalMode).url
          case Small => routes.ThirdPartyPackagersController.onPageLoad(NormalMode).url
          case _ => routes.ContractPackingController.onPageLoad(NormalMode).url
        }
        s"should update the database and redirect to $expectedUrl when no producerType in useranswers" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswers

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(selectedValue))
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[HowManyLitresGlobally]](None)(_.get(HowManyLitresGloballyPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe selectedValue
            }
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testWhoIsUnableToRegisterWithGivenUtr(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
