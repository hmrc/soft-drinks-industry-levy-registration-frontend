package controllers

import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient
import models.TestCB

class TestCBControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/testCB"
  val checkRoutePath = "/changeTestCB"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the TestCB page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("testCB" + ".title"))
            val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
            checkBoxInputs.size() mustBe TestCB.values.size

            TestCB.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
              checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
              checkBoxInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    TestCB.values.zipWithIndex.foreach { case (checkboxItem, index) =>
      s"when the userAnswers contains data for the page with " + checkboxItem.toString + " selected" - {
        s"should return OK and render the page with " + checkboxItem.toString + " checkboxItem checked" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(TestCBPage, Set(checkboxItem)).success.value

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("testCB" + ".title"
              ) )
              val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
              checkBoxInputs.size() mustBe TestCB.values.size

              TestCB.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
                checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
                checkBoxInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }

    "when the userAnswers contains data for the page with all checkbox items" - {
      "should return OK and render the page with all checkboxes checked" in {
        given
          .commonPrecondition

        val userAnswers = emptyUserAnswers.set(TestCBPage, TestCB.values.toSet).success.value

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("testCB" + ".title"
            ) )
            val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
            checkBoxInputs.size() mustBe TestCB.values.size

            TestCB.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
              checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
              checkBoxInputs.get(index1).hasAttr("checked") mustBe true
            }
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, Messages("testCB" + ".title"))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the TestCB page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("testCB" + ".title"
            ) )
            val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
            checkBoxInputs.size() mustBe TestCB.values.size

            TestCB.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
              checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
              checkBoxInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    TestCB.values.zipWithIndex.foreach { case (checkboxItem, index) =>
      s"when the userAnswers contains data for the page with " + checkboxItem.toString + " selected" - {
        s"should return OK and render the page with " + checkboxItem.toString + " checkboxItem checked" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(TestCBPage, Set(checkboxItem)).success.value


          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("testCB" + ".title"
              ) )
              val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
              checkBoxInputs.size() mustBe TestCB.values.size

              TestCB.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
                checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
                checkBoxInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }

    "when the userAnswers contains data for the page with all checkbox items" - {
      "should return OK and render the page with all checkboxes checked" in {
        given
          .commonPrecondition

        val userAnswers = emptyUserAnswers.set(TestCBPage, TestCB.values.toSet).success.value

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("testCB" + ".title"
            ) )
            val checkBoxInputs = page.getElementsByClass("govuk-checkboxes__input")
            checkBoxInputs.size() mustBe TestCB.values.size

            TestCB.values.zipWithIndex.foreach { case (checkBoxItem1, index1) =>
              checkBoxInputs.get(index1).attr("value") mustBe checkBoxItem1.toString
              checkBoxInputs.get(index1).hasAttr("checked") mustBe true
            }
          }
        }
      }
    }

    testOtherSuccessUserTypes(baseUrl + checkRoutePath, Messages("testCB" + ".title"))
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    TestCB.values.foreach { case checkboxItem =>
      "when the user selects " + checkboxItem.toString - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> Set(checkboxItem))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.TestSummaryController.onPageLoad.url)
                val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[TestCB]]](None)(_.get(TestCBPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get.head mustBe checkboxItem
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswers.set(TestCBPage, Set(checkboxItem)).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + normalRoutePath, Json.obj("value" -> Set(checkboxItem))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.TestSummaryController.onPageLoad.url)
                val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[TestCB]]](None)(_.get(TestCBPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get.head mustBe checkboxItem
              }
            }
          }
        }
      }
    }

    "when the user selects all checkboxItems" - {
      "should update the session with the new value and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> TestCB.values)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.TestSummaryController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[TestCB]]](None)(_.get(TestCBPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe TestCB.values.toSet
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(TestCBPage, TestCB.values.toSet).success.value

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> TestCB.values)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.TestSummaryController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[TestCB]]](None)(_.get(TestCBPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe TestCB.values.toSet
            }
          }
        }
      }
    }

    "when the user does not select any checkbox" - {
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
            page.title must include("Error: " + Messages("testCB" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe Messages("testCB" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    TestCB.values.foreach { case checkboxItem =>
      "when the user selects " + checkboxItem.toString - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> Set(checkboxItem))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
                val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[TestCB]]](None)(_.get(TestCBPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get.head mustBe checkboxItem
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswers.set(TestCBPage, Set(checkboxItem)).success.value

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, baseUrl + checkRoutePath, Json.obj("value" -> Set(checkboxItem))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
                val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[TestCB]]](None)(_.get(TestCBPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get.head mustBe checkboxItem
              }
            }
          }
        }
      }
    }

    "when the user selects all checkboxItems" - {
      "should update the session with the new value and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.obj("value" -> TestCB.values)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[TestCB]]](None)(_.get(TestCBPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe TestCB.values.toSet
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(TestCBPage, TestCB.values.toSet).success.value

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.obj("value" -> TestCB.values)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Set[TestCB]]](None)(_.get(TestCBPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe TestCB.values.toSet
            }
          }
        }
      }
    }

    "when the user does not select any checkbox" - {
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
            page.title must include("Error: " + Messages("testCB" + ".title"
            ) )
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe Messages("testCB" + ".error.required"
            )
          }
        }
      }
    }

    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
