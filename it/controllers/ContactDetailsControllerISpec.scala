package controllers

import models.{ContactDetails, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.ContactDetailsPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.WsTestClient

class ContactDetailsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/contact-details"
  val checkRoutePath = "/change-contact-details"

  val contactDetailsJsObject: collection.Map[String, JsValue] = Json.toJson(contactDetails).as[JsObject].value
  val contactDetailsMap: collection.Map[String, String] = {
    contactDetailsJsObject.map { case (fName, fValue) => fName -> fValue.as[String] }
  }

  val fieldNameToLabels = Map("fullName" -> "Full name", "position" -> "Job title", "phoneNumber" -> "Telephone number", "email" -> "Email address")
  val userAnswers: UserAnswers = emptyUserAnswers.set(ContactDetailsPage, contactDetails).success.value

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the ContactDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("Contact person details - Soft Drinks Industry Levy - GOV.UK")
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 4
            contactDetailsMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
              inputFields.get(index).text() mustBe fieldNameToLabels(fieldName)
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
            page.title must include("Contact person details - Soft Drinks Industry Levy - GOV.UK")
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 4
            contactDetailsMap.zipWithIndex.foreach { case ((fieldName, fieldValue), index) =>
              inputFields.get(index).text() mustBe fieldNameToLabels(fieldName)
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe true
              inputFields.get(index).getElementById(fieldName).attr("value") mustBe fieldValue
            }
          }
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, Messages("contactDetails" + ".title"))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  "GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the ContactDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("Contact person details - Soft Drinks Industry Levy - GOV.UK")
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 4
            contactDetailsMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
              inputFields.get(index).text() mustBe fieldNameToLabels(fieldName)
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
            page.title must include("Contact person details - Soft Drinks Industry Levy - GOV.UK")
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 4
            contactDetailsMap.zipWithIndex.foreach { case ((fieldName, fieldValue), index) =>
              inputFields.get(index).text() mustBe fieldNameToLabels(fieldName)
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
      "should update the session with the new values and redirect to the Check Your Answers controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.toJson(contactDetailsDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[ContactDetails]](None)(_.get(ContactDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe contactDetailsDiff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.toJson(contactDetailsDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[ContactDetails]](None)(_.get(ContactDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe contactDetailsDiff
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
            client, baseUrl + normalRoutePath, Json.toJson(ContactDetails("", "", "", ""))
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: Contact person details - Soft Drinks Industry Levy - GOV.UK")
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe 4
            contactDetailsMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
              val errorSummary = errorSummaryList.get(index)
              errorSummary
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummary.text() mustBe Messages("contactDetails.error." + fieldName + ".required")
            }
          }
        }
      }
      contactDetailsMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
        "when no answer is given for field " + fieldName in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          val invalidJson = contactDetailsMap.foldLeft(Json.obj()) { case (current, (fn, fv)) =>
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
              page.title must include("Error: Contact person details - Soft Drinks Industry Levy - GOV.UK")
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummaryList

                .select("a")
                .attr("href") mustBe "#" + fieldName

              errorSummaryList.text() must include (Messages("contactDetails.error." + fieldName + ".required"))
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
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.toJson(contactDetailsDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[ContactDetails]](None)(_.get(ContactDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe contactDetailsDiff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.toJson(contactDetailsDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[ContactDetails]](None)(_.get(ContactDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe contactDetailsDiff
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
            client, baseUrl + checkRoutePath, Json.toJson(ContactDetails("", "", "", ""))
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: Contact person details - Soft Drinks Industry Levy - GOV.UK")
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe 4
            contactDetailsMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
              val errorSummary = errorSummaryList.get(index)
              errorSummary
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummary.text() mustBe Messages("contactDetails.error." + fieldName + ".required")
            }
          }
        }
      }
      contactDetailsMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
        "when no answer is given for field " + fieldName in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          val invalidJson = contactDetailsMap.foldLeft(Json.obj()) { case (current, (fn, fv)) =>
            val fieldValue = if (fn == fieldName) {
              ""
            } else {
              fv
            }
            current ++ Json.obj(fn -> fieldValue)
          }
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: Contact person details - Soft Drinks Industry Levy - GOV.UK")
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummaryList
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummaryList.text() must include (Messages("contactDetails.error." + fieldName + ".required"))
            }
          }
        }
      }
    }

    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))  }
}
