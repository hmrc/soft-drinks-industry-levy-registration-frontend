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

import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow, Value}
import views.html.RegistrationConfirmationView

import java.time.LocalDateTime

class RegistrationConfirmationViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[RegistrationConfirmationView]
  implicit val request: Request[_] = FakeRequest()
  val companyName = "Super Lemonade Lmt"
  val submittedDateTime = LocalDateTime.of(2023, 7, 10, 14, 30)
  val emailAddress = "test@email.com"
  val summaryList: Seq[(String, SummaryList)] = {
    Seq(
      "foo" -> SummaryList(Seq(SummaryListRow(value = Value(content = HtmlContent("bar"))))),
      "wizz" -> SummaryList(Seq(SummaryListRow(value = Value(content = HtmlContent("bang")))))
    )
  }

  object Selectors {
    val heading = "govuk-heading-m"
    val panel = "govuk-panel govuk-panel--confirmation"
    val panel_title = "govuk-panel__title"
    val panel_body = "govuk-panel__body"
    val bodyM = "govuk-body-m"
    val body = "govuk-body"
    val link = "govuk-link"
    val details = "govuk-details"
    val detailsText = "govuk-details__summary-text"
    val detailsContent = "govuk-details__text"
    val summaryListHeading = "govuk-heading-m"
    val summaryList = "govuk-summary-list"
    val summaryRow = "govuk-summary-list__row"
    val summaryValue = "govuk-summary-list__value"
  }

  "View" - {
    val html = view(summaryList, submittedDateTime, companyName, emailAddress)(request, messages(application), frontendAppConfig)
    val document = doc(html)
    "should contain the expected title" in {
      document.title() mustEqual "Application complete - Soft Drinks Industry Levy - GOV.UK"
    }

    "should include the expected panel" in {
      val panel = document.getElementsByClass(Selectors.panel).get(0)
      panel.getElementsByClass(Selectors.panel_title).text() mustEqual "Application complete"
      panel.getElementsByClass(Selectors.panel_body).text() mustEqual s"We have received your application to register $companyName for the Soft Drinks Industry Levy"
    }

    "should include a link to print page" in {
      val printPageElements = document.getElementById("printPage")
      printPageElements.className() mustBe Selectors.bodyM
      val link = printPageElements.getElementsByClass(Selectors.link)
      link.text() mustEqual Messages("site.print")
      link.attr("href") mustEqual "javascript:window.print()"
    }

    "that has a body" - {
      "which states the registration sent" in {
        val applicationSentAt = document.getElementById("applicationSentAt")
        applicationSentAt.text() mustBe "Your application to register for the Soft Drinks Industry Levy was sent on 10 July 2023 at 2:30pm."
      }

      "which states a confirmation email sent" in {
        val applicationSentEmailed = document.getElementById("applicationSentEmailed")
        applicationSentEmailed.text() mustBe s"We have sent a registration confirmation email to $emailAddress."
      }

      "which includes a what happens next section" - {
        "that has the expected subheading" in {
          val subHeading = document.getElementById("whatNextHeader")
          subHeading.text() mustEqual "What happens next"
        }
        "that has the expected body" in {
          val p1 = document.getElementById("whatNextTextP1")
          p1.text() mustEqual "You do not need to do anything at this time."
          val p2 = document.getElementById("whatNextTextP2")
          p2.text() mustEqual s"We will send your Soft Drinks Industry Levy reference number to $emailAddress within 24 hours."
        }
      }

      "which includes a need help with this service" - {
        "that has the expected subheading" in {
          val subHeading = document.getElementById("needHelp")
          subHeading.text() mustEqual "Help using this service"
        }
        "that has the expected body" in {
          val p1 = document.getElementById("needHelpP1")
          p1.text() mustEqual "Call the Soft Drinks Industry Helpline on 0300 200 3700 if you:"
          val listItems = document.getElementById("helpList").getElementsByTag("li")
          listItems.size() mustBe 2
          listItems.get(0).text() mustBe "do not receive your reference number"
          listItems.get(1).text() mustBe "need to make a change to your application"
        }
      }

      "which include a details section" - {
        val details = document.getElementsByClass(Selectors.details).get(0)
        "that has the expected details summary text" in {
          details.getElementsByClass(Selectors.detailsText).text() mustEqual Messages("registrationConfirmation.detailsSummary")
        }

        "that has the expected content" in {
          val detailsContent = details.getElementsByClass(Selectors.detailsContent).first()
          detailsContent.getElementsByClass(Selectors.summaryListHeading).first().text() mustBe "foo"
          detailsContent.getElementsByClass(Selectors.summaryList)
            .first()
            .getElementsByClass(Selectors.summaryRow)
            .first()
            .getElementsByClass(Selectors.summaryValue).first().text() mustBe "bar"
          detailsContent.getElementsByClass(Selectors.summaryListHeading).last().text() mustBe "wizz"
          detailsContent.getElementsByClass(Selectors.summaryList)
            .last()
            .getElementsByClass(Selectors.summaryRow)
            .first()
            .getElementsByClass(Selectors.summaryValue).first().text() mustBe "bang"
        }
      }
    }

    testNoBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
