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
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import views.html.RegistrationPendingView

class RegistrationPendingViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[RegistrationPendingView]
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-l"
  }

  "View" - {
    val utr = "foo"
    val address = HtmlContent("bar")
    val helpLineNumber = "wizz"
    val html = view(utr, address, helpLineNumber)(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include(Messages("registrationPending" + ".title"))
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual Messages("registrationPending" + ".heading")
    }
    "should have the expected utr" in {
      document.getElementById("utrField").text() mustBe utr + ":"
    }
    "should have the expected address" in {
      document.getElementById("pendingUTRAddress").text() mustBe address.value.toString()
    }
    "should have the expected text" in {
      document.getElementById("subText1").text() mustBe s"These are the details we hold for Unique Taxpayer Reference (UTR) $utr:"
      document.getElementById("subText2").text() mustBe s"If you have not got your registration number within 24 hours you need to call the Soft Drinks Industry Levy Helpline on $helpLineNumber."
      document.getElementById("subText3").text() mustBe "We will contact you using the information you gave in your application if there is a problem."
    }

    testBackLink(document)
    validateTimeoutDialog(document)

    validateAccessibilityStatementLinkPresent(document)
  }

}
