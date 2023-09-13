package controllers

import models.{NormalMode, PackagingSiteName, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.PackagingSiteNamePage
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.WsTestClient

class PackagingSiteNameControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/packaging-site-name"
  val checkRoutePath = "/change-packaging-site-name"

  val packagingSiteNameJsObject: collection.Map[String, JsValue] = Json.toJson(packagingSiteName).as[JsObject].value
  val packagingSiteNameMap: collection.Map[String, String] = {
    packagingSiteNameJsObject.map { case (fName, fValue) => fName -> fValue.as[String] }
  }

  val userAnswers: UserAnswers = emptyUserAnswers.set(PackagingSiteNamePage, packagingSiteName).success.value

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackagingSiteName page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "What is your UK packaging site name? - Soft Drinks Industry Levy - GOV.UK"
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 1
            packagingSiteNameMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
              inputFields.get(index).text() mustBe "What is your UK packaging site name?"
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe false
            }
          }
        }
      }
    }

    s"when the userAnswers contains data for the page" - {
      s"should return OK and render the page with fields populated" in {
        given
          .commonPrecondition

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "What is your UK packaging site name? - Soft Drinks Industry Levy - GOV.UK"
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 1
            packagingSiteNameMap.zipWithIndex.foreach { case ((fieldName, fieldValue), index) =>
              inputFields.get(index).text() mustBe "What is your UK packaging site name?"
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe true
              inputFields.get(index).getElementById(fieldName).attr("value") mustBe fieldValue
            }
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, "What is your UK packaging site name?")
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  "GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackagingSiteName page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "What is your UK packaging site name? - Soft Drinks Industry Levy - GOV.UK"
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 1
            packagingSiteNameMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
              inputFields.get(index).text() mustBe "What is your UK packaging site name?"
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe false
            }
          }
        }
      }
    }

    s"when the userAnswers contains data for the page" - {
      s"should return OK and render the page with fields populated" in {
        given
          .commonPrecondition

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "What is your UK packaging site name? - Soft Drinks Industry Levy - GOV.UK"
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 1
            packagingSiteNameMap.zipWithIndex.foreach { case ((fieldName, fieldValue), index) =>
              inputFields.get(index).text() mustBe "What is your UK packaging site name?"
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe true
              inputFields.get(index).getElementById(fieldName).attr("value") mustBe fieldValue
            }
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    "when the user populates answers all questions" - {
      "should update the session with the new values and redirect to the CYA controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.toJson(packagingSiteNameDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[PackagingSiteName]](None)(_.get(PackagingSiteNamePage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe packagingSiteNameDiff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.toJson(packagingSiteNameDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[PackagingSiteName]](None)(_.get(PackagingSiteNamePage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe packagingSiteNameDiff
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      "when no questions are answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.toJson(PackagingSiteName(""))
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title mustBe "Error: What is your UK packaging site name? - Soft Drinks Industry Levy - GOV.UK"
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe packagingSiteNameMap.size
            packagingSiteNameMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
              val errorSummary = errorSummaryList.get(index)
              errorSummary
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummary.text() mustBe "Enter a packaging site name"
            }
          }
        }
      }
      packagingSiteNameMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
        "when no answer is given for field" + fieldName in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          val invalidJson = packagingSiteNameMap.foldLeft(Json.obj()) { case (current, (fn, fv)) =>
            val fieldValue = if (fn == fieldName) {
              ""
            } else {
              fv
            }
            current ++ Json.obj(fn -> fieldValue)
          }
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title mustBe "Error: What is your UK packaging site name? - Soft Drinks Industry Levy - GOV.UK"
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummaryList
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummaryList.text() mustBe "Enter a packaging site name"
            }
          }
        }
      }
    }

    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    "when the user populates answers all questions" - {
      "should update the session with the new values and redirect to the CYA page" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.toJson(packagingSiteNameDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[PackagingSiteName]](None)(_.get(PackagingSiteNamePage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe packagingSiteNameDiff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.toJson(packagingSiteNameDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[PackagingSiteName]](None)(_.get(PackagingSiteNamePage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe packagingSiteNameDiff
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      "when no questions are answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, Json.toJson(PackagingSiteName(""))
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title mustBe "Error: What is your UK packaging site name? - Soft Drinks Industry Levy - GOV.UK"
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe packagingSiteNameMap.size
            packagingSiteNameMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
              val errorSummary = errorSummaryList.get(index)
              errorSummary
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummary.text() mustBe "Enter a packaging site name"
            }
          }
        }
      }
    }

    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))  }
}
