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
import forms.PackagingSiteDetailsFormProvider
import models.backend.{Site, UkAddress}
import models.{CheckMode, Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.PackagingSiteDetailsView

class PackagingSiteDetailsViewSpec extends ViewSpecHelper {

  val view: PackagingSiteDetailsView = application.injector.instanceOf[PackagingSiteDetailsView]
  val formProvider = new PackagingSiteDetailsFormProvider
  val form: Form[Boolean] = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

  val PackagingSite1: Site = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    Some("Wild Lemonade Group"),
    None)
  val address45Characters: Site = Site(
    UkAddress(List("29 Station Pl.", "The Railyard", "Cambridge"), "CB1 2FP"),
    None,
    None,
    None)

  val address47Characters: Site = Site(
    UkAddress(List("29 Station Place", "The Railyard", "Cambridge"), "CB1 2FP"),
    Some("10"),
    None,
    None)

  val address49Characters: Site = Site(
    UkAddress(List("29 Station PlaceDr", "The Railyard", "Cambridge"), "CB1 2FP"),
    None,
    None,
    None)

  lazy val packagingSiteListWith1: Map[String, Site] = Map(("78941132", PackagingSite1))


  object Selectors {
    val heading = "govuk-fieldset__heading"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--m"
    val radios = "govuk-radios__item"
    val radioInput = "govuk-radios__input"
    val radioLabels = "govuk-label govuk-radios__label"
    val body = "govuk-body"
    val summaryList = "govuk-summary-list"
    val summaryListRow = "govuk-summary-list__row"
    val summaryListKey = "govuk-summary-list__key"
    val summaryListActions = "govuk-summary-list__value"
    val hidden = "govuk-visually-hidden"
    val link = "govuk-link"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  def getDocument(packagingSites: Map[String, Site], mode: Mode) = {
    val html = view(form, mode, packagingSites)(request, messages(application))
    doc(html)
  }

  "View" - {
    List(NormalMode, CheckMode).foreach { mode =>
      s"In ${mode.toString}" - {
        List(packagingSiteListWith1, packagingSiteListWith3).foreach { packagingSites =>
          val numberOfPackagingSites = packagingSites.size
          val document = getDocument(packagingSites, mode)
          s"when $numberOfPackagingSites packaging site has been added" - {
            "should contain the expected title" in {
              val expectedTitle = if (numberOfPackagingSites > 1) {
                Messages("packagingSiteDetails.titleMultipleSites", numberOfPackagingSites)
              } else {
                Messages("packagingSiteDetails.title1Site")
              }
              document.title() must include(expectedTitle)
            }

            "should include the expected h1 heading" in {
              val expectedHeading = if (numberOfPackagingSites > 1) {
                Messages("packagingSiteDetails.headingMultipleSites", numberOfPackagingSites)
              } else {
                Messages("packagingSiteDetails.heading1Site")
              }
              val h1 = document.getElementsByTag("h1")
              h1.size() mustBe 1
              h1.text() mustEqual expectedHeading
            }

            "should include the expected body" in {
              val p = document.getElementsByTag("p")
              p.size() mustBe 1
              p.text() mustEqual "You must always have at least one packaging site listed"
            }

            "should include the expected summary list" - {
              val summaryListRows = document.getElementsByClass(Selectors.summaryListRow)
              s"that contains $numberOfPackagingSites rows" in {
                summaryListRows.size() mustEqual numberOfPackagingSites
              }

              packagingSites.zipWithIndex.foreach { case ((id, site), index) =>
                s"that includes a summary row for ${site.address.lines.head}" in {
                  val summaryRow = summaryListRows.get(index)
                  summaryRow.getElementsByTag("dt").text() must include((site.address.lines :+ site.address.postCode).mkString(", "))
                  val action = summaryRow.getElementsByClass(Selectors.link)
                  if (numberOfPackagingSites > 1) {
                    action.text() must include("Remove")
                    action.attr("href") mustBe routes.RemovePackagingSiteDetailsController.onPageLoad(NormalMode, id).url
                  } else {
                    Messages("packagingSiteDetails.title1Site")
                  }
//                  TODO: Implement this
//                  document.title() must include(expectedTitle)
                }

                "should include the expected h1 heading" in {
                  val expectedHeading = if (numberOfPackagingSites > 1) {
                    Messages("packagingSiteDetails.headingMultipleSites", numberOfPackagingSites)
                  } else {
                    Messages("packagingSiteDetails.heading1Site")
                  }
                  val h1 = document.getElementsByTag("h1")
                  h1.size() mustBe 1
                  h1.text() mustEqual expectedHeading
                }

                "should include the expected body" in {
                  val p = document.getElementsByTag("p")
                  p.size() mustBe 1
                  p.text() mustEqual "You must always have at least one packaging site listed"
                }

                "should include the expected summary list" - {
                  val summaryListRows = document.getElementsByClass(Selectors.summaryListRow)
                  s"that contains $numberOfPackagingSites rows" in {
                    summaryListRows.size() mustEqual numberOfPackagingSites
                  }

                  packagingSites.zipWithIndex.foreach { case ((id, site), index) =>
                    s"that includes a summary row for ${site.address.lines.head}" in {
                      val summaryRow = summaryListRows.get(index)
                      summaryRow.getElementsByTag("dt").text() must include((site.address.lines :+ site.address.postCode).mkString(", "))
                      val action = summaryRow.getElementsByClass(Selectors.link)
                      if (numberOfPackagingSites > 1) {
                        action.text() must include("Remove packaging site")
                        action.attr("href") mustBe routes.RemovePackagingSiteDetailsController.onPageLoad(mode, id).url
                      } else {
                        action.size() mustEqual 0
                      }
                    }
                  }
                }

                "should include the expected details section" in {
                  val expectedDetailsLinkInfo = if (numberOfPackagingSites == 1) {
                    Messages("packagingSiteDetails.detailsLink")
                  } else {
                    ""
                  }
                  val details = document.getElementsByClass("govuk-details__summary-text")
                  details.text() mustEqual expectedDetailsLinkInfo
                }
              }
            }

            val document1PackagingSite = getDocument(packagingSiteListWith1, mode)

            "should include the expected legend heading" in {
              val legend = document1PackagingSite.getElementsByClass(Selectors.legend)
              legend.size() mustBe 1
              legend.text() mustEqual Messages("packagingSiteDetails.heading2")
            }

            "when the form is not preoccupied and has no errors" - {

              "should have radio buttons" - {
                val radioButtons = document1PackagingSite.getElementsByClass(Selectors.radios)
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
              val html1 = view(form.fill(true), mode, packagingSiteListWith1)(request, messages(application))
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
              val html1 = view(form.fill(false), mode, packagingSiteListWith1)(request, messages(application))
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
              document1PackagingSite.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
            }

            "contains a form with the correct action" - {
              "when in CheckMode" - {
                val htmlYesSelected = view(form.fill(true), CheckMode, packagingSiteListWith1)(request, messages(application))
                val documentYesSelected = doc(htmlYesSelected)

                val htmlNoSelected = view(form.fill(false), CheckMode, packagingSiteListWith1)(request, messages(application))
                val documentNoSelected = doc(htmlNoSelected)
                "and yes is selected" in {
                  documentYesSelected.select(Selectors.form)
                    .attr("action") mustEqual routes.PackagingSiteDetailsController.onSubmit(CheckMode).url
                }

                "and no is selected" in {
                  documentNoSelected.select(Selectors.form)
                    .attr("action") mustEqual routes.PackagingSiteDetailsController.onSubmit(CheckMode).url
                }
              }

              "when in mode" - {
                val htmlYesSelected = view(form.fill(true), mode, packagingSiteListWith1)(request, messages(application))
                val documentYesSelected = doc(htmlYesSelected)

                val htmlNoSelected = view(form.fill(false), mode, packagingSiteListWith1)(request, messages(application))
                val documentNoSelected = doc(htmlNoSelected)
                "and yes is selected" in {
                  documentYesSelected.select(Selectors.form)
                    .attr("action") mustEqual routes.PackagingSiteDetailsController.onSubmit(mode).url
                }

                "and no is selected" in {
                  documentNoSelected.select(Selectors.form)
                    .attr("action") mustEqual routes.PackagingSiteDetailsController.onSubmit(mode).url
                }
              }
            }

            "when there are form errors" - {
              val htmlWithErrors = view(form.bind(Map("value" -> "")), mode, packagingSiteListWith1)(request, messages(application))
              val documentWithErrors = doc(htmlWithErrors)

              "should have a title containing error" in {
                val titleMessage = Messages("packagingSiteDetails.title1Site", 1)
                documentWithErrors.title must include("Error: " + titleMessage)
              }

              "contains a message that links to field with error" in {
                val errorSummary = documentWithErrors
                  .getElementsByClass(Selectors.errorSummaryList)
                  .first()
                errorSummary
                  .select("a")
                  .attr("href") mustBe "#value"
                errorSummary.text() mustBe Messages("packagingSiteDetails.error.required")
              }
            }

            testBackLink(document1PackagingSite)
            validateTimeoutDialog(document1PackagingSite)
            validateTechnicalHelpLinkPresent(document1PackagingSite)
            validateAccessibilityStatementLinkPresent(document1PackagingSite)

          }
        }
      }
    }
  }
}
