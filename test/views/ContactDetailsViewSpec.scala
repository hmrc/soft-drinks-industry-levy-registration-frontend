/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views

import controllers.routes
import forms.ContactDetailsFormProvider
import models.{CheckMode, NormalMode, ContactDetails}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.ContactDetailsView
import play.api.libs.json.{JsObject, Json}



class ContactDetailsViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[ContactDetailsView]
  val formProvider = new ContactDetailsFormProvider
  val form = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-m"
    val formGroup = "govuk-form-group"
    val label = "govuk-label"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  val Contactdetails = ContactDetails("1", "2")
  val ContactdetailsJsObject = Json.toJson(Contactdetails).as[JsObject].value
  val ContactdetailsMap: collection.Map[String, String] =
  ContactdetailsJsObject.map { case (fName, fValue) => fName -> fValue.toString }

  "View" - {
    val html = view(form, NormalMode)(request, messages(application))
    val document = doc(html)
    val questionItems = document.getElementsByClass(Selectors.formGroup)
    "should contain the expected title" in {
      document.title() must include(Messages("contactDetails" + ".title"))
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual Messages("contactDetails" + ".heading")
    }

    "should contain" + ContactdetailsMap.size + " questions" in {
      questionItems.size() mustBe ContactdetailsMap.size
    }

    ContactdetailsMap.zipWithIndex.foreach { case ((fieldName, fieldValue), index) =>

      "when the form is not prepopulated and has no errors" - {
        "should include the expected question fields" - {

          "that has the field " + fieldName in {
            val questionItem1 = questionItems
              .get(index)
            questionItem1
              .getElementsByClass(Selectors.label)
              .text() mustBe fieldName
          }
        }
      }
    }

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.continue")
    }

    "contains a form with the correct action" - {
      "when in CheckMode" in {
        val htmlAllSelected = view(form.fill(Contactdetails), CheckMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.ContactDetailsController.onSubmit(CheckMode).url
      }

      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill(Contactdetails), NormalMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.ContactDetailsController.onSubmit(NormalMode).url
      }
    }


    ContactdetailsMap.foreach { case (fieldName, _) =>
      val fieldWithError = ContactdetailsMap + ((fieldName -> ""))
      val htmlWithErrors = view(form.bind(fieldWithError.toMap), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "when " + fieldName + "is empty" - {
        "should have a title containing error" in {
          val titleMessage = Messages("contactDetails.title")
          documentWithErrors.title must include("Error: " + titleMessage)
        }

        "contains a message that links to field with error" in {
          val errorSummary = documentWithErrors
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          errorSummary
            .select("a")
            .attr("href") mustBe "#" + fieldName
          errorSummary.text() mustBe Messages("contactDetails.error." + fieldName + ".required")
        }
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
