package controllers

import models.{CheckMode, LitresInBands, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.WsTestClient

class ImportsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/imports"
  val checkRoutePath = "/change-imports"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Imports page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "Do you bring liable drinks into the UK from anywhere outside of the UK? - Soft Drinks Industry Levy - GOV.UK"
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

    userAnswersForImportsPage.foreach { case (key, userAnswers) =>
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
              page.title mustBe "Do you bring liable drinks into the UK from anywhere outside of the UK? - Soft Drinks Industry Levy - GOV.UK"
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
    testOtherSuccessUserTypes(baseUrl + normalRoutePath,
      "Do you bring liable drinks into the UK from anywhere outside of the UK?")
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Imports page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "Do you bring liable drinks into the UK from anywhere outside of the UK? - Soft Drinks Industry Levy - GOV.UK"
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

    userAnswersForImportsPage.foreach { case (key, userAnswers) =>
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
              page.title mustBe "Do you bring liable drinks into the UK from anywhere outside of the UK? - Soft Drinks Industry Levy - GOV.UK"
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

    testOtherSuccessUserTypes(baseUrl + checkRoutePath,
      "Do you bring liable drinks into the UK from anywhere outside of the UK?")
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    userAnswersForImportsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session and redirect to next page" - {
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
                val expectedLocation = if (yesSelected) {
                  routes.HowManyImportsController.onPageLoad(NormalMode).url
                } else {
                  routes.DoNotRegisterController.onPageLoad.url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(emptyUserAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))

                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
                val dataStoreForNextPage = getAnswers(emptyUserAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoreForNextPage mustBe None

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
                val expectedLocation = if (yesSelected) {
                  routes.HowManyImportsController.onPageLoad(NormalMode).url
                } else {
                  routes.DoNotRegisterController.onPageLoad.url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
                val dataStoreForNextPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                if (yesSelected) {
                  dataStoreForNextPage.isDefined mustBe true
                } else {
                  dataStoreForNextPage mustBe None
                }
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
            page.title mustBe "Error: Do you bring liable drinks into the UK from anywhere outside of the UK? - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe "Select yes if you bring liable drinks into the UK from anywhere outside of the UK"
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    userAnswersForImportsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        val yesSelected = key == "yes"
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              val expectedUrl = if(yesSelected) {
                routes.HowManyImportsController.onPageLoad(CheckMode).url
              } else {
                routes.CheckYourAnswersController.onPageLoad.url
              }

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                val dataStoredForPage = getAnswers(emptyUserAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
                val dataStoreForNextPage = getAnswers(emptyUserAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                dataStoreForNextPage mustBe None

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
                val expectedUrl = if (yesSelected) {
                  routes.HowManyImportsController.onPageLoad(CheckMode).url
                } else {
                  routes.CheckYourAnswersController.onPageLoad.url
                }
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ImportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
                val dataStoreForNextPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyImportsPage))
                if (yesSelected) {
                  dataStoreForNextPage.isDefined mustBe true
                } else {
                  dataStoreForNextPage mustBe None
                }
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
            page.title mustBe "Error: Do you bring liable drinks into the UK from anywhere outside of the UK? - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe "Select yes if you bring liable drinks into the UK from anywhere outside of the UK"
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }

  "Post" - {
    "in normal mode" - {
      s"Should redirect to the $StartDatePage when the user is a large producer " in {
        given
          .commonPrecondition

        setAnswers(largeProducerNoPackagingRouteUserAnswers)
        WsTestClient.withClient { client =>

          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> "false")
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.StartDateController.onPageLoad(NormalMode).url)
          }
        }
      }

      s"Should redirect to the $StartDatePage when the user is a non producer and selected yes on the $ContractPackingPage" in {
        given
          .commonPrecondition

        setAnswers(nonProducerUserAnswers)
        WsTestClient.withClient { client =>

          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> "false")
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.StartDateController.onPageLoad(NormalMode).url)
          }
        }
      }

      s"Should redirect to the DoNotRegister page when the user is a non producer and selected no on the $ContractPackingPage" in {
        given
          .commonPrecondition

        setAnswers(nonProducerDeregisterUserAnswers)
        WsTestClient.withClient { client =>

          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> "false")
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.DoNotRegisterController.onPageLoad.url)
          }
        }
      }

      s"Should redirect to the $StartDatePage when the user is a small producer and selected yes on the $ContractPackingPage" in {
        given
          .commonPrecondition

        setAnswers(smallProducerUserAnswers)
        WsTestClient.withClient { client =>

          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> "false")
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.StartDateController.onPageLoad(NormalMode).url)
          }
        }
      }

      s"Should redirect to the $ContactDetailsPage if user selected yes on the $ThirdPartyPackagersPage, no on the $OperatePackagingSitesPage, " +
        s"and no on $ContractPackingPage" in {
        given
          .commonPrecondition

        setAnswers(smallProducerNoPackagingRouteUserAnswers)
        WsTestClient.withClient { client =>

          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> "false")
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.ContactDetailsController.onPageLoad(NormalMode).url)
          }
        }
      }

      s"Should redirect to the DoNotRegister page if user selected no on the $ThirdPartyPackagersPage, no on the $OperatePackagingSitesPage, " +
        s"and no on $ContractPackingPage" in {
        given
          .commonPrecondition

        setAnswers(smallProducerDoNotRegisterUserAnswers)
        WsTestClient.withClient { client =>

          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> "false")
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.DoNotRegisterController.onPageLoad.url)
          }
        }
      }
    }
  }
}
