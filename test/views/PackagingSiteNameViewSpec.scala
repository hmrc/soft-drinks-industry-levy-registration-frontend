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
import forms.PackagingSiteNameFormProvider
import models.{CheckMode, NormalMode, PackagingSiteName}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.PackagingSiteNameView



class PackagingSiteNameViewSpec extends ViewSpecHelper {

  val view: PackagingSiteNameView = application.injector.instanceOf[PackagingSiteNameView]
  val formProvider = new PackagingSiteNameFormProvider
  val form: Form[PackagingSiteName] = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()
  val sdilId = "foo"

  object Selectors {
    val formGroup = "govuk-form-group"
    val label = "govuk-label"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  val packagingSiteName: PackagingSiteName = PackagingSiteName("1")
  val packagingSiteNameJsObject: collection.Map[String, JsValue] = Json.toJson(packagingSiteName).as[JsObject].value
  val packagingSiteNameMap: collection.Map[String, String] =
  packagingSiteNameJsObject.map { case (fName, fValue) => fName -> fValue.toString }

  "View" - {
    val html = view(form, NormalMode, sdilId)(request, messages(application))
    val document = doc(html)
    val questionItems = document.getElementsByClass(Selectors.formGroup)
    "should contain the expected title" in {
      document.title() mustBe "What is your UK packaging site name? - Soft Drinks Industry Levy - GOV.UK"
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.label).text() mustEqual "What is your UK packaging site name?"
    }

    "should contain 1 question" in {
      questionItems.size() mustBe 1
    }

    "when the form is not prepopulated and has no errors" - {
      "should include the expected question field" - {

        "that has the name field PackagingSiteName " in {
          val questionItem1 = questionItems
            .get(0)
          questionItem1
            .getElementsByClass(Selectors.label)
            .text() mustBe "What is your UK packaging site name?"
        }
      }
    }

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }

    "contains a form with the correct action" - {
      "when in CheckMode" in {
        val htmlAllSelected = view(form.fill(packagingSiteName), CheckMode, sdilId)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.PackagingSiteNameController.onSubmit(CheckMode, sdilId).url
      }

      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill(packagingSiteName), NormalMode, sdilId)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.PackagingSiteNameController.onSubmit(NormalMode, sdilId).url
      }
    }

    val htmlWithErrors = view(form.bind(Map("packagingSiteName" -> "")), NormalMode, sdilId)(request, messages(application))
    val documentWithErrors = doc(htmlWithErrors)

    "when site name is empty" - {
      "should have a title containing error" in {
        documentWithErrors.title mustBe "Error: What is your UK packaging site name? - Soft Drinks Industry Levy - GOV.UK"
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#" + "packagingSiteName"
        errorSummary.text() mustBe "Enter a packaging site name"
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)

    validateAccessibilityStatementLinkPresent(document)
  }
}
