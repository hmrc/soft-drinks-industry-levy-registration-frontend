package controllers

import models.alf.init.*
import models.backend.UkAddress
import models.{CheckMode, NormalMode, Warehouse}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import pages.AskSecondaryWarehousesPage
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.{FakeRequest, WsTestClient}
import testSupport.ALFTestHelper
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class AskSecondaryWarehousesControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/ask-secondary-warehouses"
  val checkRoutePath = "/change-ask-secondary-warehouses"

  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())
    
  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the AskSecondaryWarehouses page with no data populated" in {
        build
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("askSecondaryWarehouses" + ".title"))
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

    userAnswersForAskSecondaryWarehousesPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          build
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("askSecondaryWarehouses" + ".title"))
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
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, messages("askSecondaryWarehouses" + ".title"))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the AskSecondaryWarehouses page with no data populated" in {
        build
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("askSecondaryWarehouses" + ".title"))
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

    userAnswersForAskSecondaryWarehousesPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          build
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("askSecondaryWarehouses" + ".title"))
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

    testOtherSuccessUserTypes(baseUrl + checkRoutePath, messages("askSecondaryWarehouses" + ".title"))
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath)

  }

  s"POST " + normalRoutePath - {

    "when the user select no" - {
      "should update the session with the new value and redirect to the contact details page" - {
        "when the session contains no data for page" in {
          build
            .commonPrecondition

          setAnswers(emptyUserAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> radioNo)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ContactDetailsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(identifier).fold[Option[Boolean]](None)(_.get(AskSecondaryWarehousesPage))
              dataStoredForPage.get mustBe radioNo
            }
          }
        }

        "when the session already contains data for page" in {
          build
            .commonPrecondition

          setAnswers(userAnswersForAskSecondaryWarehousesPage.get("yes").get)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> radioNo)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ContactDetailsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(identifier).fold[Option[Boolean]](None)(_.get(AskSecondaryWarehousesPage))
              dataStoredForPage.get mustBe radioNo
            }
          }
        }
      }
    }

    "user selects yes and saves and continues updating the user answers and ramps onto ALF, also NOT wiping the warehouse list" in {
      val warehouseToRemain = Map("foo" -> Warehouse(aTradingName, UkAddress(List.empty, "", None)))
      setAnswers(emptyUserAnswers.copy(warehouseList = warehouseToRemain))

      val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8706/soft-drinks-industry-levy-registration/off-ramp/warehouses/${sdilNumber}",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
          accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-registration-frontend"),
          phaseFeedbackLink = None,
          deskProServiceName = None,
          showPhaseBanner = Some(false),
          alphaPhase = None,
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
            timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
            timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
          )),
          serviceHref = Some(frontendAppConfig.sdilHomeUrl),
          pageHeadingStyle = Some("govuk-heading-l")
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
                  organisationLabel = Some("Trading name"))
              ),
              confirmPageLabels = None,
              countryPickerLabels = None
            ))
          )),
        requestedVersion = None
      )
      val expectedResult: Some[JsObject] = Some(
        Json.obj(
          "askSecondaryWarehouses" -> true
        ))
      val alfOnRampURL: String = "http://onramp.com"

      build
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)

      WsTestClient.withClient { client =>
        val result1 = client.url(baseUrl + normalRoutePath)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
            "Csrf-Token" -> "nocheck")
          .withFollowRedirects(false)
          .post(Json.obj("value" -> true))

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
          getAnswers(identifier).map(userAnswers => userAnswers.data) mustBe expectedResult
          getAnswers(identifier).map(userAnswers => userAnswers.warehouseList).get mustBe warehouseToRemain

          ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfigToBePosted) mustBe true
        }

      }
    }

    "user selects yes where they had previously selected yes and had added some warehouses " +
      "saves and continues they should be redirected to warehouse details page " in {

      setAnswers(
        emptyUserAnswers.copy(warehouseList = warehouseListWith1)
          .set(AskSecondaryWarehousesPage, true).success.value
      )

      build
        .commonPrecondition

      val expectedResult: Some[JsObject] = Some(Json.obj("askSecondaryWarehouses" -> true))

      WsTestClient.withClient { client =>
        val result1 = client.url(baseUrl + normalRoutePath)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
            "Csrf-Token" -> "nocheck")
          .withFollowRedirects(false)
          .post(Json.obj("value" -> true))

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
          getAnswers(identifier).map(userAnswers => userAnswers.data) mustBe expectedResult
          getAnswers(identifier).map(userAnswers => userAnswers.warehouseList).get mustBe warehouseListWith1

        }

      }
    }

    "user selects no and saves and continues, user is taken to contact details, also wiping the warehouse list" in {
      val warehouseToBeWiped = Map("foo" -> Warehouse(aTradingName, UkAddress(List.empty, "", None)))
      setAnswers(emptyUserAnswers.copy(warehouseList = warehouseToBeWiped))

      build
        .commonPrecondition

      val expectedResult: Some[JsObject] = Some(
        Json.obj(
          "askSecondaryWarehouses" -> false
        ))

      WsTestClient.withClient { client =>
        val result1 = client.url(baseUrl + normalRoutePath)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
            "Csrf-Token" -> "nocheck")
          .withFollowRedirects(false)
          .post(Json.obj("value" -> false))

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.ContactDetailsController.onPageLoad(NormalMode).url)
          getAnswers(identifier).map(userAnswers => userAnswers.data) mustBe expectedResult
          getAnswers(identifier).map(userAnswers => userAnswers.warehouseList).get mustBe Map.empty

        }

      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        build
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("askSecondaryWarehouses" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("askSecondaryWarehouses" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))

  }

  s"POST " + checkRoutePath - {

    "user selects no and saves and continues, user is taken to check your answers, also wiping the warehouse list" in {
      val warehouseToBeWiped = Map("foo" -> Warehouse(aTradingName, UkAddress(List.empty, "", None)))
      setAnswers(emptyUserAnswers.copy(warehouseList = warehouseToBeWiped))

      build
        .commonPrecondition

      val expectedResult: Some[JsObject] = Some(
        Json.obj(
          "askSecondaryWarehouses" -> false
        ))

      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(
          client, baseUrl + checkRoutePath, Json.obj("value" -> radioNo)
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
          getAnswers(identifier).map(userAnswers => userAnswers.data) mustBe expectedResult
          getAnswers(identifier).map(userAnswers => userAnswers.warehouseList).get mustBe Map.empty

        }

      }
    }

    "when the user selects no" - {
      "should update the session with the new value and redirect to the checkAnswers controller" - {
        "when the session already contains data for page" in {
          build
            .commonPrecondition

          setAnswers(userAnswersForAskSecondaryWarehousesPage.get("yes").get)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.obj("value" -> radioNo)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
              val dataStoredForPage = getAnswers(identifier).fold[Option[Boolean]](None)(_.get(AskSecondaryWarehousesPage))
              dataStoredForPage.get mustBe radioNo
            }
          }
        }
      }
    }

    "user selects yes and saves and continues updating the user answers and ramps onto ALF, also NOT wiping the warehouse list" in {
      val warehouseToRemain = Map("foo" -> Warehouse(aTradingName, UkAddress(List.empty, "", None)))
      setAnswers(emptyUserAnswers.copy(warehouseList = warehouseToRemain))

      val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8706/soft-drinks-industry-levy-registration/off-ramp/warehouses/${sdilNumber}",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
          accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-registration-frontend"),
          phaseFeedbackLink = None,
          deskProServiceName = None,
          showPhaseBanner = Some(false),
          alphaPhase = None,
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
            timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
            timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
          )),
          serviceHref = Some(frontendAppConfig.sdilHomeUrl),
          pageHeadingStyle = Some("govuk-heading-l")
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
                  organisationLabel = Some("Trading name"))
              ),
              confirmPageLabels = None,
              countryPickerLabels = None
            ))
          )),
        requestedVersion = None
      )
      val expectedResult: Some[JsObject] = Some(
        Json.obj(
          "askSecondaryWarehouses" -> true
        ))
      val alfOnRampURL: String = "http://onramp.com"

      build
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)

      WsTestClient.withClient { client =>
        val result1 = client.url(baseUrl + checkRoutePath)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
            "Csrf-Token" -> "nocheck")
          .withFollowRedirects(false)
          .post(Json.obj("value" -> true))

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
          getAnswers(identifier).map(userAnswers => userAnswers.data) mustBe expectedResult
          getAnswers(identifier).map(userAnswers => userAnswers.warehouseList).get mustBe warehouseToRemain

          ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfigToBePosted) mustBe true
        }

      }
    }

    "user selects yes where they had previously selected yes and had added some warehouses " +
      "saves and continues they should be redirected to warehouse details page " in {

      setAnswers(
        emptyUserAnswers.copy(warehouseList = warehouseListWith1)
          .set(AskSecondaryWarehousesPage, true).success.value
      )

      build
        .commonPrecondition

      val expectedResult: Some[JsObject] = Some(Json.obj("askSecondaryWarehouses" -> true))

      WsTestClient.withClient { client =>
        val result1 = client.url(baseUrl + checkRoutePath)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
            "Csrf-Token" -> "nocheck")
          .withFollowRedirects(false)
          .post(Json.obj("value" -> true))

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehouseDetailsController.onPageLoad(CheckMode).url)
          getAnswers(identifier).map(userAnswers => userAnswers.data) mustBe expectedResult
          getAnswers(identifier).map(userAnswers => userAnswers.warehouseList).get mustBe warehouseListWith1

        }

      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        build
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("askSecondaryWarehouses" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("askSecondaryWarehouses" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
