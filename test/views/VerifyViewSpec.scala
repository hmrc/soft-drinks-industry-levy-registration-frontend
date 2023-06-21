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
import forms.VerifyFormProvider
import models.{CheckMode, NormalMode, Verify}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import views.html.VerifyView

class VerifyViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[VerifyView]
  val formProvider = new VerifyFormProvider
  val form = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-fieldset__heading"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--m"
    val radios = "govuk-radios"
    val radiosInput = "govuk-radios__input"
    val radiosItems = "govuk-radios__item"
    val radiosLables = "govuk-label govuk-radios__label"
    val body = "govuk-body"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  "View" - {
    val address = HtmlContent("foo")
    val utr = "bar"
    val html = view(form, NormalMode, utr, address)(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include(Messages("verify" + ".title"))
    }
    "should include the expected subtext" in {
      document.getElementById("subText").text() mustBe s"These are the details we hold for Unique Taxpayer Reference (UTR) $utr:"
    }
    "should include the expected utr" in {
      document.getElementById("utrField").text() mustBe utr + ":"
    }
    "should include the expected address" in {
      document.getElementById("addressForUTR").text() mustBe address.value.toString()
    }
    "should include the expected subHeading" in {
      document.getElementById("subHeading").text() mustBe "Is this the business you want to register?"
    }

    "should not include a legend with the expected heading" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 0
    }

    "when the form is not preoccupied and has no errors" - {

      "should include the expected radio buttons" - {
        val radiobuttons = document.getElementsByClass(Selectors.radiosItems)

        "that has 2 items" in {
          radiobuttons.size() mustBe Verify.values.size
        }
        Verify.values.zipWithIndex.foreach { case (radio, index) =>
          s"that has the " + radio.toString + " to select and is unchecked" in {
            val radio1 = radiobuttons
              .get(index)
            radio1
              .getElementsByClass(Selectors.radiosLables)
              .text() mustBe Messages("verify." + radio.toString)
            val input = radio1
              .getElementsByClass(Selectors.radiosInput)
            input.attr("value") mustBe radio.toString
            input.hasAttr("checked") mustBe false
          }
        }
      }
    }

    Verify.values.foreach { radio =>
      val html1 = view(form.fill(radio), NormalMode, utr, address)(request, messages(application))
      val document1 = doc(html1)

      s"when the form is preoccupied with " + radio.toString + "selected and has no errors" - {
        "should have radiobuttons" - {
          val radiobuttons = document1.getElementsByClass(Selectors.radiosItems)
          Verify.values.zipWithIndex.foreach { case (radio1, index) =>
            if (radio1.toString == radio.toString) {
              s"that has the option to select" + radio1.toString + " and is checked" in {
                val radiobuttons1 = radiobuttons
                  .get(index)
                radiobuttons1
                  .getElementsByClass(Selectors.radiosLables)
                  .text() mustBe Messages("verify." + radio1.toString)
                val input = radiobuttons1
                  .getElementsByClass(Selectors.radiosInput)
                input.attr("value") mustBe radio1.toString
                input.hasAttr("checked") mustBe true
              }
            } else {
              s"that has the option to select " + radio1.toString + " and is unchecked" in {
                val radiobuttons1 = radiobuttons
                  .get(index)
                radiobuttons1
                  .getElementsByClass(Selectors.radiosLables)
                  .text() mustBe Messages("verify." + radio1.toString)
                val input = radiobuttons1
                  .getElementsByClass(Selectors.radiosInput)
                input.attr("value") mustBe radio1.toString
                input.hasAttr("checked") mustBe false
              }
            }
          }
        }
      }
    }

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.continue")
    }

    "contains a form with the correct action" - {
      "when in CheckMode" in {
        val htmlAllSelected = view(form.fill(Verify.values.head), CheckMode, utr, address)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.VerifyController.onSubmit(CheckMode).url
      }

      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill(Verify.values.head), NormalMode, utr, address)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.VerifyController.onSubmit(NormalMode).url
      }
    }

    "when there are form errors" - {
      val htmlWithErrors = view(form.bind(Map("value" -> "")), NormalMode, utr, address)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        val titleMessage = Messages("verify.title")
        documentWithErrors.title must include("Error: " + titleMessage)
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value_0"
        errorSummary.text() mustBe "Select if this is the business you want to register, if you want to register this business but want to add a new contact address for the Soft Drinks Industry Levy, or if you need to sign in using a different Government Gateway user ID"
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
