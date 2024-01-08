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
import models.{CheckMode, ContactDetails, NormalMode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.ContactDetailsView



class ContactDetailsViewSpec extends ViewSpecHelper {

  val view: ContactDetailsView = application.injector.instanceOf[ContactDetailsView]
  val formProvider = new ContactDetailsFormProvider
  val form: Form[ContactDetails] = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-l"
    val formGroup = "govuk-form-group"
    val label = "govuk-label"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  val Contactdetails: ContactDetails = ContactDetails("Jane Doe", "CEO", "(07700) 099009", "name@example.com")
  val ContactdetailsJsObject: collection.Map[String, JsValue] = Json.toJson(Contactdetails).as[JsObject].value
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
      document.getElementsByClass(Selectors.heading).text() mustEqual Messages("contactDetails" + ".title")
    }

    "should contain" + ContactdetailsMap.size + " questions" in {
      questionItems.size() mustBe 4
    }

      "when the form is not pre-populated and has no errors" - {
        "should include the expected question fields" - {

          "that has the field fullName" in {
            val questionItem1 = questionItems
              .get(0)
            questionItem1
              .getElementsByClass(Selectors.label)
              .text() mustBe "Full name"
          }

          "that has the field position" in {
            val questionItem1 = questionItems
              .get(1)
            questionItem1
              .getElementsByClass(Selectors.label)
              .text() mustBe "Job title"
          }
          "that has the field phoneNumber" in {
            val questionItem1 = questionItems
              .get(2)
            questionItem1
              .getElementsByClass(Selectors.label)
              .text() mustBe "Telephone number"
          }
          "that has the field email" in {
            val questionItem1 = questionItems
              .get(3)
            questionItem1
              .getElementsByClass(Selectors.label)
              .text() mustBe "Email address"
          }
        }
      }


    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
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

      "when fullName is empty and valid information is entered" - {
        val fieldWithError = ContactdetailsMap ++ Map(("fullName", ""), ("position", "CEO"), ("phoneNumber", "07700 099 990"),
          ("email", "name@example.com"))
        val htmlWithErrors = view(form.bind(fieldWithError.toMap), NormalMode)(request, messages(application))
        val documentWithErrors = doc(htmlWithErrors)
        "should have a title containing error" in {
          documentWithErrors.title mustBe "Error: Contact person details - Soft Drinks Industry Levy - GOV.UK"
        }

        "contains a message that links to field with error" in {
          val errorSummary = documentWithErrors
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          errorSummary
            .select("a")
            .attr("href") mustBe ("#" + "fullName")
          errorSummary.text() mustBe "Enter full name"
        }
      }

    "when position is empty and valid information is entered" - {
      val fieldWithError = ContactdetailsMap ++ Map(("fullName", "Jane Doe"), ("position", ""), ("phoneNumber", "07700 099 990"),
        ("email", "name@example.com"))
      val htmlWithErrors = view(form.bind(fieldWithError.toMap), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe ("#" + "position")
        errorSummary.text() mustBe "Enter job title"
      }
    }

    "when phoneNumber is empty and valid information is entered" - {
      val fieldWithError = ContactdetailsMap ++ Map(("fullName", "Jane Doe"), ("position", "CEO"), ("phoneNumber", ""),
        ("email", "name@example.com"))
      val htmlWithErrors = view(form.bind(fieldWithError.toMap), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe ("#" + "phoneNumber")
        errorSummary.text() mustBe "Enter telephone number"
      }
    }

    "when email is empty and valid information is entered" - {
      val fieldWithError = ContactdetailsMap ++ Map(("fullName", "Jane Doe"), ("position", "CEO"), ("phoneNumber", "07700 099 990"),
        ("email", ""))
      val htmlWithErrors = view(form.bind(fieldWithError.toMap), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe ("#" + "email")
        errorSummary.text() mustBe "Enter email address"
      }
    }

    "when fullName has invalid information entered" - {
      val fieldWithError = ContactdetailsMap ++ Map(("fullName", "J@ne Doe2"), ("position", "CEO"), ("phoneNumber", "07700 099 990"),
        ("email", "name@example.com"))
      val htmlWithErrors = view(form.bind(fieldWithError.toMap), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should display a message that contains a valid error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary.text() mustBe "Full name must only include letters a to z, apostrophes, hyphens and spaces"
      }
    }

    "when position has invalid information entered" - {
      val fieldWithError = ContactdetailsMap ++ Map(("fullName", "Jane Doe"), ("position", "The Best 30+ CEO"), ("phoneNumber", "07700 099 990"),
        ("email", "name@example.com"))
      val htmlWithErrors = view(form.bind(fieldWithError.toMap), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should display a message that contains a valid error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary.text() mustBe "Job title must only include letters a to z, apostrophes, dashes, hyphens and spaces"
      }
    }

    "when phoneNumber has invalid information entered" - {
      val fieldWithError = ContactdetailsMap ++ Map(("fullName", "Jane Doe"), ("position", "CEO"), ("phoneNumber", "$07700 099 990"),
        ("email", "name@example.com"))
      val htmlWithErrors = view(form.bind(fieldWithError.toMap), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should display a message that contains a valid error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary.text() mustBe "Enter a telephone number, like 01632 960999 or 07700 900999"
      }
    }

    "when email has invalid information entered" - {
      val fieldWithError = ContactdetailsMap ++ Map(("fullName", "Jane Doe"), ("position", "CEO"), ("phoneNumber", "07700 099 990"),
        ("email", "name.example.com"))
      val htmlWithErrors = view(form.bind(fieldWithError.toMap), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should display a message that contains a valid error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary.text() mustBe "Enter a real email address, like name@example.com"
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)

    validateAccessibilityStatementLinkPresent(document)
  }
}
