package controllers

import models.NormalMode
import models.alf.init.{AppLevelLabels, ConfirmPageConfig, EditPageLabels, JourneyConfig, JourneyLabels, JourneyOptions, LanguageLabels, LookupPageLabels, SelectPageConfig, TimeoutConfig}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.WarehouseDetailsPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.Helpers.redirectLocation
import play.api.test.WsTestClient
import testSupport.ALFTestHelper

class WarehouseDetailsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/warehouses"
  val checkRoutePath = "/change-warehouses"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return SEE_OTHER and redirect to Ask Secondary Warehouse page" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url)
          }
        }
      }
    }

    userAnswersForWarehouseDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("warehouseDetails.title.heading","1",""))
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

    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the WarehouseDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url)
          }
        }
      }
    }

    userAnswersForWarehouseDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("warehouseDetails.title.heading","1",""))
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


    testUnauthorisedUser(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
  }

  "Get should return redirect to index controller page when 0 warehouses listed" in {
    given
      .commonPrecondition

    setAnswers(emptyUserAnswers)

    WsTestClient.withClient { client =>
      val result = createClientRequestGet(client, baseUrl + checkRoutePath)
      whenReady(result) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url)
      }
    }
  }

  "Get should return the correct title and header when 1 warehouse listed on page" in {
    given
      .commonPrecondition
    setAnswers(userAnswersWith1Warehouse)
    WsTestClient.withClient { client =>
      val result = createClientRequestGet(client, baseUrl + checkRoutePath)
      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.title must include(Messages("warehouseDetails.title.heading", "1", ""))
        page.getElementsByClass("remove-link").size() mustEqual 0
      }
    }
  }

  "Get should return the correct title and header when 2 warehouses listed on page" in {
    given
      .commonPrecondition
    setAnswers(userAnswersWith2Warehouses)
    WsTestClient.withClient { client =>
      val result = createClientRequestGet(client, baseUrl + checkRoutePath)
      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.title must include(Messages("warehouseDetails.title.heading", "2", "s"))
        page.getElementsByClass("remove-link").size() mustEqual 2
      }
    }
  }

  s"POST " + normalRoutePath - {
    userAnswersForWarehouseDetailsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad().url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad().url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
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

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("warehouseDetails.title.heading","0","s"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("warehouseDetails" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }


  "user selected yes, user should be taken to ALF" in {
    val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
      version = 2,
      options = JourneyOptions(
        continueUrl = s"http://localhost:8706/soft-drinks-industry-levy-registration-frontend/off-ramp/warehouses/${sdilNumber}",
        homeNavHref = None,
        signOutHref = Some(controllers.auth.routes.AuthController.signOut().url),
        accessibilityFooterUrl = None,
        phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-registration-frontend&backUrl=http%3A%2F%2Flocalhost%3A8703%2Fsoft-drinks-industry-levy-registration-frontend%2Fwarehouses"),
        deskProServiceName = None,
        showPhaseBanner = Some(false),
        alphaPhase = Some(false),
        includeHMRCBranding = Some(true),
        ukMode = Some(true),
        selectPageConfig = Some(SelectPageConfig(
          proposalListLimit = Some(10),
          showSearchAgainLink = Some(true)
        )),
        showBackButtons = Some(true),
        disableTranslations = Some(true),
        allowedCountryCodes = None,
        confirmPageConfig = Some(ConfirmPageConfig(
          showSearchAgainLink = Some(true),
          showSubHeadingAndInfo = Some(true),
          showChangeLink = Some(true),
          showConfirmChangeText = Some(true)
        )),
        timeoutConfig = Some(TimeoutConfig(
          timeoutAmount = 900,
          timeoutUrl = controllers.auth.routes.AuthController.signOut().url,
          timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
        )),
        serviceHref = Some(routes.IndexController.onPageLoad().url),
        pageHeadingStyle = Some("govuk-heading-m")
      ),
      labels = Some(
        JourneyLabels(
          en = Some(LanguageLabels(
            appLevelLabels = Some(AppLevelLabels(
              navTitle = Some("Soft Drinks Industry Levy"),
              phaseBannerHtml = None
            )),
            selectPageLabels = None,
            lookupPageLabels = Some(
              LookupPageLabels(
                title = Some("Find UK warehouse address"),
                heading = Some("Find UK warehouse address"),
                postcodeLabel = Some("Postcode"))),
            editPageLabels = Some(
              EditPageLabels(
                title = Some("Enter the UK warehouse address"),
                heading = Some("Enter the UK warehouse address"),
                line1Label = Some("Address line 1"),
                line2Label = Some("Address line 2"),
                line3Label = Some("Address line 3 (optional)"),
                townLabel = Some("Address line 4 (optional)"),
                postcodeLabel = Some("Postcode"),
                organisationLabel = Some("Trading name (optional)"))
            ),
            confirmPageLabels = None,
            countryPickerLabels = None
          ))
        )),
      requestedVersion = None
    )
    val expectedResultInDB: Some[JsObject] = Some(
      Json.obj(
        "warehouseDetails" -> true
      ))
    val alfOnRampURL: String = "http://onramp.com"
    setAnswers(emptyUserAnswers
      .set(WarehouseDetailsPage, true).success.value
      .copy(warehouseList = warehouseListWith1))
    given
      .commonPrecondition
      .alf.getSuccessResponseFromALFInit(alfOnRampURL)

    WsTestClient.withClient { client =>
      val result =
        client.url(s"$baseUrl/warehouses")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders("Csrf-Token" -> "nocheck")
          .withFollowRedirects(false)
          .post(Json.obj("value" -> "true"))


      whenReady(result) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe  Some("http://onramp.com")
        getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResultInDB

        ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfigToBePosted) mustBe true
      }
    }
  }

  s"POST " + checkRoutePath - {
    userAnswersForWarehouseDetailsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
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

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("warehouseDetails.title.heading","0","s"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("warehouseDetails" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
