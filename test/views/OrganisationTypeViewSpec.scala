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
import forms.OrganisationTypeFormProvider
import models.{CheckMode, NormalMode, OrganisationType}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.OrganisationTypeView

class OrganisationTypeViewSpec extends ViewSpecHelper {

  val view: OrganisationTypeView = application.injector.instanceOf[OrganisationTypeView]
  val formProvider = new OrganisationTypeFormProvider
  val form: Form[OrganisationType] = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()
  val withoutSoleTrader: Boolean = true

  object Selectors {
    val heading = "govuk-fieldset__heading"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--l"
    val radios = "govuk-radios"
    val radiosInput = "govuk-radios__input"
    val radiosItems = "govuk-radios__item"
    val radiosLabels = "govuk-label govuk-radios__label"
    val body = "govuk-body"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  "View when withCTEnrollment is false" - {
    val html = view(form, NormalMode, withoutSoleTrader = false)(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include ("Which of these best describes your business? - Soft Drinks Industry Levy - GOV.UK")
    }

    "should include a legend with the expected heading" in {
      val legend = document.getElementsByClass(Selectors.legend)
      legend.size() mustBe 1
      legend.get(0).getElementsByClass(Selectors.legend).text() mustEqual "Which of these best describes your business?"
    }

    "when the form is not preoccupied and has no errors" - {

      "should include the expected radio buttons" - {
        val radiobuttons = document.getElementsByClass(Selectors.radiosItems)

        "that has 5 items" in {
          radiobuttons.size() mustBe 5
        }
        OrganisationType.valuesWithST.zipWithIndex.foreach { case (radio, index) =>
          s"that has the " + radio.toString + " to select and is unchecked" in {
            val radio1 = radiobuttons
              .get(index)
            radio1
              .getElementsByClass(Selectors.radiosLabels)
              .text() mustBe Messages("organisationType." + radio.toString)
            val input = radio1
              .getElementsByClass(Selectors.radiosInput)
            input.attr("value") mustBe radio.toString
            input.hasAttr("checked") mustBe false
          }
        }
      }
    }

    "View when withCTEnrollment is true" - {
      val html = view(form, NormalMode, withoutSoleTrader)(request, messages(application))
      val document = doc(html)
      "when the form is not preoccupied and has no errors" - {

        "should include the expected radio buttons" - {
          val radiobuttons = document.getElementsByClass(Selectors.radiosItems)

          "that has 4 items" in {
            radiobuttons.size() mustBe 4
          }

          OrganisationType.values.zipWithIndex.foreach { case (radio, index) =>
            s"that has the " + radio.toString + " to select and is unchecked" in {
              val radio1 = radiobuttons
                .get(index)
              radio1
                .getElementsByClass(Selectors.radiosLabels)
                .text() mustBe Messages("organisationType." + radio.toString)
              val input = radio1
                .getElementsByClass(Selectors.radiosInput)
              input.attr("value") mustBe radio.toString
              input.hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    OrganisationType.values.foreach { radio =>
      val html1 = view(form.fill(radio), NormalMode, withoutSoleTrader)(request, messages(application))
      val document1 = doc(html1)

      s"when the form is preoccupied with " + radio.toString + "selected and has no errors" - {
        "should have radio buttons" - {
          val radiobuttons = document1.getElementsByClass(Selectors.radiosItems)
          OrganisationType.values.zipWithIndex.foreach { case (radio1, index) =>
            if (radio1.toString == radio.toString) {
              s"that has the option to select" + radio1.toString + " and is checked" in {
                val radiobuttons1 = radiobuttons
                  .get(index)
                radiobuttons1
                  .getElementsByClass(Selectors.radiosLabels)
                  .text() mustBe Messages("organisationType." + radio1.toString)
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
                  .getElementsByClass(Selectors.radiosLabels)
                  .text() mustBe Messages("organisationType." + radio1.toString)
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
        val htmlAllSelected = view(form.fill(OrganisationType.values.head), CheckMode, withoutSoleTrader)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.OrganisationTypeController.onSubmit(CheckMode).url
      }

      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill(OrganisationType.values.head), NormalMode, withoutSoleTrader)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.OrganisationTypeController.onSubmit(NormalMode).url
      }
    }

    "when there are form errors" - {
      val htmlWithErrors = view(form.bind(Map("value" -> "")), NormalMode, withoutSoleTrader)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "should have a title containing error" in {
        documentWithErrors.title must include("Error: Which of these best describes your business?")
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value_0"
        errorSummary.text() mustBe "Select the option that best describes your business"
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
