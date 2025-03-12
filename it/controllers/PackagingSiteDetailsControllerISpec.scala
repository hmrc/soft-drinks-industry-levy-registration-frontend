package controllers

import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.{PackAtBusinessAddressPage, PackagingSiteDetailsPage}
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.{FakeRequest, WsTestClient}

class PackagingSiteDetailsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/packaging-site-details"
  val checkRoutePath = "/change-packaging-site-details"

  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())

  "GET " + normalRoutePath - {
    "when the userAnswers contains 0 packaging sites" - {
      s"must redirect to $PackAtBusinessAddressPage on a GET if the packaging site list is empty" in {
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
          }
        }
      }
    }

    "when the userAnswers contains 1 packaging site and no data" - {
      "should return OK and render the PackagingSiteDetails page with no radio items selected" in {
        `given`
          .commonPrecondition

        setAnswers(userAnswersWith1PackingSite)
        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("packagingSiteDetails" + ".title1Site"))
            val summaryList = page.getElementsByClass("govuk-summary-list")
            summaryList.size mustBe 1
            val summaryListRows = summaryList.get(0).getElementsByClass("govuk-summary-list__row")
            summaryListRows.size() mustBe 1
            val summaryRow = summaryListRows.get(0)
            summaryRow.text() must include((packagingSite1.address.lines :+ packagingSite1.address.postCode).mkString(", "))
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

    "when the userAnswers contains more than 1 packaging site and no data" - {
      "should return OK and render the PackagingSiteDetails page with no data populated" in {
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers.copy(packagingSiteList = packagingSiteListWith3))

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("packagingSiteDetails" + ".titleMultipleSites", 3))
            val summaryList = page.getElementsByClass("govuk-summary-list")
            summaryList.size mustBe 1
            val summaryListRows = summaryList.get(0).getElementsByClass("govuk-summary-list__row")
            summaryListRows.size() mustBe 3
            packagingSiteListWith3.zipWithIndex.foreach { case ((_, site), index) =>
              val summaryRow = summaryListRows.get(index)
              summaryRow.text() must include(site.address.lines.mkString(", ") + s", ${site.address.postCode}")
            }
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

    userAnswersForPackagingSiteDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio unchecked" in {
          `given`
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("packagingSiteDetails" + ".title1Site"))
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
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, messages("packagingSiteDetails" + ".title1Site"), userAnswersWith1PackingSite)
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains 1 packaging site and no data" - {
      "should return OK and render the PackagingSiteDetails page with no data populated" in {
        `given`
          .commonPrecondition

        setAnswers(userAnswersWith1PackingSite)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("packagingSiteDetails" + ".title1Site"))
            val summaryList = page.getElementsByClass("govuk-summary-list")
            summaryList.size mustBe 1
            val summaryListRows = summaryList.get(0).getElementsByClass("govuk-summary-list__row")
            summaryListRows.size() mustBe 1
            val summaryRow = summaryListRows.get(0)
            summaryRow.text() must include((packagingSite1.address.lines :+ packagingSite1.address.postCode).mkString(", "))
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

    "when the userAnswers contains more than 1 packaging site and no data" - {
      "should return OK and render the PackagingSiteDetails page with no data populated" in {
        `given`
          .commonPrecondition

        setAnswers(emptyUserAnswers.copy(packagingSiteList = packagingSiteListWith3))

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("packagingSiteDetails" + ".titleMultipleSites", 3))
            val summaryList = page.getElementsByClass("govuk-summary-list")
            summaryList.size mustBe 1
            val summaryListRows = summaryList.get(0).getElementsByClass("govuk-summary-list__row")
            summaryListRows.size() mustBe 3
            packagingSiteListWith3.zipWithIndex.foreach{case((_, site), index) =>
            val summaryRow = summaryListRows.get(index)
              summaryRow.text() must include(site.address.lines.mkString(", ") + s", ${site.address.postCode}")
            }
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

    userAnswersForPackagingSiteDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio unchecked" in {
          `given`
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("packagingSiteDetails" + ".title1Site"))
              val summaryList = page.getElementsByClass("govuk-summary-list")
              summaryList.size mustBe 1
              val summaryListRows = summaryList.get(0).getElementsByClass("govuk-summary-list__row")
              summaryListRows.size() mustBe 1
              val summaryRow = summaryListRows.get(0)
              summaryRow.text() must include(packagingSite1.address.lines.mkString(", ") + s", ${packagingSite1.address.postCode}")
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

    testOtherSuccessUserTypes(baseUrl + checkRoutePath, messages("packagingSiteDetails" + ".title1Site"), userAnswersWith1PackingSite)
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {

    "when user selects yes" in {
      val alfOnRampURL: String = "https://onramp.com"
      setAnswers(emptyUserAnswers.set(PackagingSiteDetailsPage, true).success.value
        .copy(packagingSiteList = packagingSiteListWith1))
      `given`
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)
      WsTestClient.withClient { client =>
        emptyUserAnswers
          .set(PackagingSiteDetailsPage, true).success.value
          .copy(packagingSiteList = packagingSiteListWith1)
        val result = createClientRequestPOST(
          client, baseUrl + normalRoutePath, Json.obj("value" -> true)
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
        }
      }
    }

    "when user selects no" in {
      val alfOnRampURL: String = "https://onramp.com"
      setAnswers(emptyUserAnswers.set(PackagingSiteDetailsPage, true).success.value
        .copy(packagingSiteList = packagingSiteListWith1))
      `given`
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)
      WsTestClient.withClient { client =>
        emptyUserAnswers
          .set(PackagingSiteDetailsPage, true).success.value
          .copy(packagingSiteList = packagingSiteListWith1)
        val result = createClientRequestPOST(
          client, baseUrl + normalRoutePath, Json.obj("value" -> false)
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url)
        }
      }
    }

    userAnswersForPackagingSiteDetailsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            val alfOnRampURL: String = "https://onramp.com"
            `given`
              .commonPrecondition
              .alf.getSuccessResponseFromALFInit(alfOnRampURL)

            setAnswers(userAnswersWith1PackingSite)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackagingSiteDetailsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            val alfOnRampURL: String = "https://onramp.com"
            `given`
              .commonPrecondition
              .alf.getSuccessResponseFromALFInit(alfOnRampURL)

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackagingSiteDetailsPage))
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
        `given`
          .commonPrecondition

        setAnswers(userAnswersWith1PackingSite)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("packagingSiteDetails" + ".title1Site"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("packagingSiteDetails" + ".error.required")
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
      val alfOnRampURL: String = "https://onramp.com"
      setAnswers(emptyUserAnswers.set(PackagingSiteDetailsPage, true).success.value
        .copy(packagingSiteList = packagingSiteListWith1))
      `given`
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)
      WsTestClient.withClient { client =>
        emptyUserAnswers
          .set(PackagingSiteDetailsPage, true).success.value
          .copy(packagingSiteList = packagingSiteListWith1)
        val result = createClientRequestPOST(
          client, baseUrl + checkRoutePath, Json.obj("value" -> true)
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
        }
      }
    }

    "when user selects no should redirect to check your answers" in {
      val alfOnRampURL: String = "https://onramp.com"
      setAnswers(emptyUserAnswers.set(PackagingSiteDetailsPage, true).success.value
        .copy(packagingSiteList = packagingSiteListWith1))
      `given`
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)
      WsTestClient.withClient { client =>
        emptyUserAnswers
          .set(PackagingSiteDetailsPage, true).success.value
          .copy(packagingSiteList = packagingSiteListWith1)
        val result = createClientRequestPOST(
          client, baseUrl + checkRoutePath, Json.obj("value" -> false)
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
        }
      }
    }

    userAnswersForPackagingSiteDetailsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            val alfOnRampURL: String = "https://onramp.com"
            `given`
              .commonPrecondition
              .alf.getSuccessResponseFromALFInit(alfOnRampURL)

            setAnswers(userAnswersWith1PackingSite)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackagingSiteDetailsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            val alfOnRampURL: String = "https://onramp.com"
            `given`
              .commonPrecondition
              .alf.getSuccessResponseFromALFInit(alfOnRampURL)

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackagingSiteDetailsPage))
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
        `given`
          .commonPrecondition

        setAnswers(userAnswersWith1PackingSite)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("packagingSiteDetails" + ".title1Site"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("packagingSiteDetails" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
