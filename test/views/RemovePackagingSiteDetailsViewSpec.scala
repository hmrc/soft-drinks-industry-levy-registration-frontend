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
import forms.RemovePackagingSiteDetailsFormProvider
import models.{ CheckMode, NormalMode }
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.RemovePackagingSiteDetailsView

class RemovePackagingSiteDetailsViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[RemovePackagingSiteDetailsView]
  val formProvider = new RemovePackagingSiteDetailsFormProvider
  val form = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-fieldset__heading"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--l"
    val radios = "govuk-radios__item"
    val radioInput = "govuk-radios__input"
    val radioLabels = "govuk-label govuk-radios__label"
    val body = "govuk-body"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  "View" - {
    List(NormalMode, CheckMode).foreach { mode =>
      s"In ${mode.toString}" - {
        val ref = "foo"
        val address = Html("bar")
        val html = view(form, mode, ref, address)(request, messages(application))
        val document = doc(html)
        "should contain the expected title" in {
          document.title() must include(Messages("removePackagingSiteDetails" + ".title"))
        }

        "have the correct address passed in" in {
          document.getElementById("packagingSiteDetails").text() mustBe address.body
        }

        "should include a legend with the expected heading" in {
          val legend = document.getElementsByClass(Selectors.legend)
          legend.size() mustBe 1
          legend.get(0).getElementsByClass(Selectors.legend).text() mustEqual Messages("Are you sure you want to remove this packaging site?")
        }

        "when the form is not preoccupied and has no errors" - {

          "should have radio buttons" - {
            val radioButtons = document.getElementsByClass(Selectors.radios)
            "that has the option to select Yes and is unchecked" in {
              val radioButton1 = radioButtons
                .get(0)
              radioButton1
                .getElementsByClass(Selectors.radioLabels)
                .text() mustBe "Yes"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .attr("value") mustBe "true"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .hasAttr("checked") mustBe false
            }

            "that has the option to select No and is unchecked" in {
              val radioButton1 = radioButtons
                .get(1)
              radioButton1
                .getElementsByClass(Selectors.radioLabels)
                .text() mustBe "No"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .attr("value") mustBe "false"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .hasAttr("checked") mustBe false
            }
          }
        }

        "when the form is preoccupied with yes and has no errors" - {
          val html1 = view(form.fill(true), mode, ref, address)(request, messages(application))
          val document1 = doc(html1)
          "should have radio buttons" - {
            val radioButtons = document1.getElementsByClass(Selectors.radios)
            "that has the option to select Yes and is checked" in {
              val radioButton1 = radioButtons
                .get(0)
              radioButton1
                .getElementsByClass(Selectors.radioLabels)
                .text() mustBe "Yes"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .attr("value") mustBe "true"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .hasAttr("checked") mustBe true
            }

            "that has the option to select No and is unchecked" in {
              val radioButton1 = radioButtons
                .get(1)
              radioButton1
                .getElementsByClass(Selectors.radioLabels)
                .text() mustBe "No"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .attr("value") mustBe "false"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .hasAttr("checked") mustBe false
            }
          }
        }

        "when the form is preoccupied with no and has no errors" - {
          val html1 = view(form.fill(false), mode, ref, address)(request, messages(application))
          val document1 = doc(html1)
          "should have radio buttons" - {
            val radioButtons = document1.getElementsByClass(Selectors.radios)
            "that has the option to select Yes and is unchecked" in {
              val radioButton1 = radioButtons
                .get(0)
              radioButton1
                .getElementsByClass(Selectors.radioLabels)
                .text() mustBe "Yes"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .attr("value") mustBe "true"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .hasAttr("checked") mustBe false
            }

            "that has the option to select No and is checked" in {
              val radioButton1 = radioButtons
                .get(1)
              radioButton1
                .getElementsByClass(Selectors.radioLabels)
                .text() mustBe "No"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .attr("value") mustBe "false"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .hasAttr("checked") mustBe true
            }
          }
        }

        "contain the correct button" - {
          document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
        }

        "contains a form with the correct action" - {
          val htmlYesSelected = view(form.fill(true), mode, ref, address)(request, messages(application))
          val documentYesSelected = doc(htmlYesSelected)

          val htmlNoSelected = view(form.fill(false), mode, ref, address)(request, messages(application))
          val documentNoSelected = doc(htmlNoSelected)
          "and yes is selected" in {
            documentYesSelected.select(Selectors.form)
              .attr("action") mustEqual routes.RemovePackagingSiteDetailsController.onSubmit(mode, ref).url
          }

          "and no is selected" in {
            documentNoSelected.select(Selectors.form)
              .attr("action") mustEqual routes.RemovePackagingSiteDetailsController.onSubmit(mode, ref).url
          }
        }

        "when there are form errors" - {
          val htmlWithErrors = view(form.bind(Map("value" -> "")), mode, ref, address)(request, messages(application))
          val documentWithErrors = doc(htmlWithErrors)

          "should have a title containing error" in {
            val titleMessage = Messages("removePackagingSiteDetails.title")
            documentWithErrors.title must include("Error: " + titleMessage)
          }

          "contains a message that links to field with error" in {
            val errorSummary = documentWithErrors
              .getElementsByClass(Selectors.errorSummaryList)
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("removePackagingSiteDetails.error.required")
          }
        }

        testBackLink(document)
        validateTimeoutDialog(document)
        validateTechnicalHelpLinkPresent(document)
        validateAccessibilityStatementLinkPresent(document)
      }
    }
  }

}
