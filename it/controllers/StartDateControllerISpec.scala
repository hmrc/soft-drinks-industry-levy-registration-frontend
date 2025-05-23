package controllers

import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.StartDatePage
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.{FakeRequest, WsTestClient}

import java.time.LocalDate

class StartDateControllerISpec extends ControllerITTestHelper {
  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())

  val normalRoutePath = "/start-date"
  val checkRoutePath = "/change-start-date"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the StartDate page with no data populated" in {
        build
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("startDate" + ".title"))
            val dateInputs = page.getElementsByClass("govuk-date-input__item")
            dateInputs.size() mustBe 3
            dateInputs.get(0).getElementById("startDate.day").hasAttr("value") mustBe false
            dateInputs.get(1).getElementById("startDate.month").hasAttr("value") mustBe false
            dateInputs.get(2).getElementById("startDate.year").hasAttr("value") mustBe false
          }
        }
      }
    }

    s"when the userAnswers contains a date for the page" - {
      s"should return OK and render the page with the date populated" in {
        build
          .commonPrecondition

        val userAnswers = emptyUserAnswers.set(StartDatePage, date).success.value

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("startDate" + ".title"))
            val dateInputs = page.getElementsByClass("govuk-date-input__item")
            dateInputs.size() mustBe 3
            dateInputs.get(0).getElementById("startDate.day").hasAttr("value") mustBe true
            dateInputs.get(0).getElementById("startDate.day").attr("value") mustBe day.toString
            dateInputs.get(1).getElementById("startDate.month").hasAttr("value") mustBe true
            dateInputs.get(1).getElementById("startDate.month").attr("value") mustBe month.toString
            dateInputs.get(2).getElementById("startDate.year").hasAttr("value") mustBe true
            dateInputs.get(2).getElementById("startDate.year").attr("value") mustBe year.toString
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, messages("startDate" + ".title"
    ) )
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  "GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the StartDate page with no data populated" in {
        build
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("startDate" + ".title"))
            val dateInputs = page.getElementsByClass("govuk-date-input__item")
            dateInputs.size() mustBe 3
            dateInputs.get(0).getElementById("startDate.day").hasAttr("value") mustBe false
            dateInputs.get(1).getElementById("startDate.month").hasAttr("value") mustBe false
            dateInputs.get(2).getElementById("startDate.year").hasAttr("value") mustBe false
          }
        }
      }
    }

    s"when the userAnswers contains a date for the page" - {
      s"should return OK and render the page with the date populated" in {
        build
          .commonPrecondition

        val userAnswers = emptyUserAnswers.set(StartDatePage, date).success.value

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("startDate" + ".title"))
            val dateInputs = page.getElementsByClass("govuk-date-input__item")
            dateInputs.size() mustBe 3
            dateInputs.get(0).getElementById("startDate.day").hasAttr("value") mustBe true
            dateInputs.get(0).getElementById("startDate.day").attr("value") mustBe day.toString
            dateInputs.get(1).getElementById("startDate.month").hasAttr("value") mustBe true
            dateInputs.get(1).getElementById("startDate.month").attr("value") mustBe month.toString
            dateInputs.get(2).getElementById("startDate.year").hasAttr("value") mustBe true
            dateInputs.get(2).getElementById("startDate.year").attr("value") mustBe year.toString
          }
        }
      }
    }

    testOtherSuccessUserTypes(baseUrl + checkRoutePath, messages("startDate" + ".title"
    ) )
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  s"POST " + normalRoutePath - {
    "when the user inserts a valid day, month and year" - {
      "should update the session with the new value and redirect" - {
        "when the session contains no data for page" in {
          build
            .commonPrecondition

          setAnswers(largeProducerImportsTrueUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, validDateJson
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe date
            }
          }
        }

        "when the session already contains data for page" in {
          build
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(StartDatePage, date).success.value

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, validDateJson
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe date
            }
          }
        }
      }
    }

    "should return 400 with the correct error" - {
      dateMap.foreach { case (field, value) =>
        val dateMapExculdingField = dateMap.removed(field)
        val otherFields = dateMapExculdingField.keys.toArray

        "when only the " + field + "is populated" in {
          build
            .commonPrecondition

          val invalidJson = Json.obj("startDate." + field -> value.toString)

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + messages("startDate" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#startDate.day"
              errorSummary.text() mustBe messages("startDate" + ".error.required.two", otherFields(0), otherFields(1)
              )
            }
          }
        }

        "when " + field + "is missing" in {
          build
            .commonPrecondition

          val invalidJson = dateMapExculdingField.foldLeft(Json.obj()) { (a, b) =>
            a ++ Json.obj("startDate." + b._1 -> b._2.toString)
          }

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + messages("startDate" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#startDate.day"
              errorSummary.text() mustBe messages("startDate" + ".error.required", field
              )
            }
          }
        }
      }

      "when all fields are missing" in {
        build
          .commonPrecondition

        val invalidJson = dateMap.foldLeft(Json.obj()) { (a, b) =>
          a ++ Json.obj("startDate." + b._1 -> "")
        }

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, invalidJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("startDate" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#startDate.day"
            errorSummary.text() mustBe messages("startDate" + ".error.required.all"
            )
          }
        }
      }

      "when all fields are present but not a valid date" in {
        build
          .commonPrecondition

        val invalidJson = dateMap.foldLeft(Json.obj()) { (a, b) =>
          a ++ Json.obj("startDate." + b._1 -> "30")
        }

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, invalidJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("startDate" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#startDate.day"
            errorSummary.text() mustBe "The date you became liable to register must be a real date, like 31 7 2020"
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    "when the user inserts a valid day, month and year" - {
      "should update the session with the new value and redirect to Check your answers" - {
        "when the session contains no data for page" in {
          build
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>

            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, validDateJson
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
              val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe date
            }
          }
        }

        "when the session already contains data for page" in {
          build
            .commonPrecondition

          val userAnswers = emptyUserAnswers.set(StartDatePage, date).success.value

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, validDateJson
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
              val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe date
            }
          }
        }
      }
    }

    "should return 400 with the correct error" - {
      dateMap.foreach { case (field, value) =>
        val dateMapExculdingField = dateMap.removed(field)
        val otherFields = dateMapExculdingField.keys.toArray

        "when only the " + field + "is populated" in {
          build
            .commonPrecondition

          val invalidJson = Json.obj("startDate." + field -> value.toString)

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + messages("startDate" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#startDate.day"
              errorSummary.text() mustBe messages("startDate" + ".error.required.two", otherFields(0), otherFields(1)
              )
            }
          }
        }

        "when " + field + "is missing" in {
          build
            .commonPrecondition

          val invalidJson = dateMapExculdingField.foldLeft(Json.obj()) { (a, b) =>
            a ++ Json.obj("startDate." + b._1 -> b._2.toString)
          }

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + messages("startDate" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#startDate.day"
              errorSummary.text() mustBe messages("startDate" + ".error.required", field
              )
            }
          }
        }
      }

      "when all fields are missing" in {
        build
          .commonPrecondition

        val invalidJson = dateMap.foldLeft(Json.obj()) { (a, b) =>
          a ++ Json.obj("startDate." + b._1 -> "")
        }

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, invalidJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("startDate" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#startDate.day"
            errorSummary.text() mustBe messages("startDate" + ".error.required.all"
            )
          }
        }
      }

      "when all fields are present but not a valid date" in {
        build
          .commonPrecondition

        val invalidJson = dateMap.foldLeft(Json.obj()) { (a, b) =>
          a ++ Json.obj("startDate." + b._1 -> "30")
        }

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, invalidJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("startDate" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#startDate.day"
            errorSummary.text() mustBe "The date you became liable to register must be a real date, like 31 7 2020"
          }
        }
      }
    }

    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }
  "Post in normal mode" - {
    "Should redirect to the Pack-At-Business address controller when the user is a large producer and has answered Yes " +
      "to either Operate Packaging Sites or Contract Packer" in {
      build
        .commonPrecondition

      setAnswers(largeProducerImportsTrueUserAnswers)
      WsTestClient.withClient { client =>

        val result = createClientRequestPOST(
          client, baseUrl + normalRoutePath, validDateJson
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
          val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
          dataStoredForPage.nonEmpty mustBe true
          dataStoredForPage.get mustBe date
        }
      }
    }

    "Should redirect to the Pack-At-Business address controller when the user is a small producer and has answered Yes " +
      "to Contract Packer" in {
      build
        .commonPrecondition

      setAnswers(smallProducerUserAnswers)
      WsTestClient.withClient { client =>

        val result = createClientRequestPOST(
          client, baseUrl + normalRoutePath, validDateJson
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
          val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
          dataStoredForPage.nonEmpty mustBe true
          dataStoredForPage.get mustBe date
        }
      }
    }

    "Should redirect to the Pack-At-Business address controller when the user is a non producer and has answered Yes " +
      "to Contract Packer" in {
      build
        .commonPrecondition

      setAnswers(smallProducerUserAnswers)
      WsTestClient.withClient { client =>

        val result = createClientRequestPOST(
          client, baseUrl + normalRoutePath, validDateJson
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
          val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
          dataStoredForPage.nonEmpty mustBe true
          dataStoredForPage.get mustBe date
        }
      }
    }

    "Should redirect to the Ask secondary warehouse controller when the user is a large producer and has answered No " +
      "to both Operate Packaging Sites and Contract Packer" in {
      build
        .commonPrecondition

      setAnswers(largeProducerNoPackagingRouteUserAnswers)
      WsTestClient.withClient { client =>

        val result = createClientRequestPOST(
          client, baseUrl + normalRoutePath, validDateJson
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url)
          val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
          dataStoredForPage.nonEmpty mustBe true
          dataStoredForPage.get mustBe date
        }
      }
    }

    "Should redirect to the Ask Secondary Warehouse controller when the user is a small producer and has answered No " +
      "to Contract Packer" in {
      build
        .commonPrecondition

      setAnswers(nonProducerNoPackagingRouteUserAnswers)
      WsTestClient.withClient { client =>

        val result = createClientRequestPOST(
          client, baseUrl + normalRoutePath, validDateJson
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url)
          val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
          dataStoredForPage.nonEmpty mustBe true
          dataStoredForPage.get mustBe date
        }
      }
    }

    "Should redirect to the Ask Secondary Warehouse controller when the user is a non producer and has answered No " +
      "to Contract Packer" in {
      build
        .commonPrecondition

      setAnswers(nonProducerNoPackagingRouteUserAnswers)
      WsTestClient.withClient { client =>

        val result = createClientRequestPOST(
          client, baseUrl + normalRoutePath, validDateJson
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url)
          val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
          dataStoredForPage.nonEmpty mustBe true
          dataStoredForPage.get mustBe date
        }
      }
    }
  }

  "Post in checkmode" - {
    "should redirect to check your answers" - {
      "when the user is a large producer and has answered Yes " +
        "to either Operate Packaging Sites or Contract Packer" in {
        build
          .commonPrecondition

        setAnswers(largeProducerImportsTrueUserAnswers)
        WsTestClient.withClient { client =>

          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, validDateJson
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
            val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
            dataStoredForPage.nonEmpty mustBe true
            dataStoredForPage.get mustBe date
          }
        }
      }

      "when the user is a small producer and has answered Yes " +
        "to Contract Packer" in {
        build
          .commonPrecondition

        setAnswers(smallProducerUserAnswers)
        WsTestClient.withClient { client =>

          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, validDateJson
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
            val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
            dataStoredForPage.nonEmpty mustBe true
            dataStoredForPage.get mustBe date
          }
        }
      }

      "when the user is a non producer and has answered Yes " +
        "to Contract Packer" in {
        build
          .commonPrecondition

        setAnswers(smallProducerUserAnswers)
        WsTestClient.withClient { client =>

          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, validDateJson
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
            val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
            dataStoredForPage.nonEmpty mustBe true
            dataStoredForPage.get mustBe date
          }
        }
      }

      "when the user is a large producer and has answered No " +
        "to both Operate Packaging Sites and Contract Packer" in {
        build
          .commonPrecondition

        setAnswers(largeProducerNoPackagingRouteUserAnswers)
        WsTestClient.withClient { client =>

          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, validDateJson
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
            val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
            dataStoredForPage.nonEmpty mustBe true
            dataStoredForPage.get mustBe date
          }
        }
      }

      "when the user is a small producer and has answered No " +
        "to Contract Packer" in {
        build
          .commonPrecondition

        setAnswers(nonProducerNoPackagingRouteUserAnswers)
        WsTestClient.withClient { client =>

          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, validDateJson
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
            val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
            dataStoredForPage.nonEmpty mustBe true
            dataStoredForPage.get mustBe date
          }
        }
      }

      "when the user is a non producer and has answered No " +
        "to Contract Packer" in {
        build
          .commonPrecondition

        setAnswers(nonProducerNoPackagingRouteUserAnswers)
        WsTestClient.withClient { client =>

          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, validDateJson
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
            val dataStoredForPage = getAnswers(identifier).fold[Option[LocalDate]](None)(_.get(StartDatePage))
            dataStoredForPage.nonEmpty mustBe true
            dataStoredForPage.get mustBe date
          }
        }
      }
    }
  }
}
