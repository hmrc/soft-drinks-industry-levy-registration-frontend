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
import forms.EnterBusinessDetailsFormProvider
import models.Identify
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.EnterBusinessDetailsView

import scala.util.Random


class EnterBusinessDetailsViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[EnterBusinessDetailsView]
  val formProvider = new EnterBusinessDetailsFormProvider
  val form = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val formGroup = "govuk-form-group"
    val heading = "govuk-heading-l"
    val body = "govuk-body-m"
    val hint = "govuk-hint"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
    val textArea = "govuk-input"
  }

  "View" - {
    val html = view(form)(request, messages(application), frontendAppConfig)
    val document = doc(html)
    val formGroup = document.getElementsByClass(Selectors.formGroup)
    "should contain the expected title" in {
      document.title() must include(Messages("enterBusinessDetails" + ".title"))
    }

    "should contain the expected subtext" in {
      val subtext = document.getElementsByClass(Selectors.body)
      subtext.size() mustBe 2
      subtext.get(0).text() mustBe Messages("enterBusinessDetails.subtext1")
      subtext.get(1).html() mustBe Messages("enterBusinessDetails.subtext2", "\"https://www.gov.uk/find-utr-number\"")
    }

    "should contain a govuk form group" - {
      "that contains the page heading" in {
        document.getElementsByClass(Selectors.heading)
          .text() mustBe Messages("enterBusinessDetails.heading")
      }

      "that contains the expected hint test" in {
        formGroup.get(0).getElementsByClass(Selectors.hint)
          .text() mustBe Messages("This is 10 numbers, for example 1234567890. It will be on tax returns and other letters about Corporation Tax. It may be called ‘reference’, ‘UTR’ or ‘official use’.")
      }

      "that contains a text area" in {
        formGroup.get(0).getElementsByClass(Selectors.textArea).size mustBe 1
        formGroup.get(1).getElementsByClass(Selectors.textArea).size mustBe 1
      }
    }

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }

    "contains a form with the correct action" in {
        val htmlAllSelected = view(form.fill(Identify("testing", "testing")))(request, messages(application), frontendAppConfig)
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.EnterBusinessDetailsController.onSubmit.url
    }

    "when a form error exists (utr invalid characters + no postcode)" - {
      val valueOutOfMaxRange = Random.nextString(10 + 1)

      val htmlWithErrors = view(form.bind(Map("utr" -> valueOutOfMaxRange)))(request, messages(application), frontendAppConfig)
      val documentWithErrors = doc(htmlWithErrors)
      "should have a title containing error" in {
        val titleMessage = Messages("enterBusinessDetails.title")
        documentWithErrors.title must include("Error: " + titleMessage)
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#utr"
        errorSummary.text() mustBe Messages("Unique Taxpayer Reference (UTR) must be 10 numbers Please enter a value")
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
