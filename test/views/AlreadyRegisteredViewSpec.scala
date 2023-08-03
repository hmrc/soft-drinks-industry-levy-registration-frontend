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

import base.SpecBase

import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import viewmodels.AddressFormattingHelper
import views.html.AlreadyRegisteredView

class AlreadyRegisteredViewSpec extends ViewSpecHelper with SpecBase {

  val view = application.injector.instanceOf[AlreadyRegisteredView]
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-l"
  }

  "View" - {
    val registration = rosmRegistration.rosmRegistration
    val formattedAddress = AddressFormattingHelper.formatBusinessAddress(registration.address,Some(registration.organisationName))
    val html = view(utr, formattedAddress)(request, messages(application))
    val document = doc(html)

    "should contain the expected title" in {
      document.title() must include(Messages("alreadyRegistered.heading.title"))
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual Messages("alreadyRegistered.heading.title")
    }

    "should have the expected subheading" in {
      document.getElementById("subheader").text() mustEqual s"These are the details we hold for Unique Taxpayer Reference (UTR) $utr:"
    }

    "should have the expected address" in {
      document.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove Guildford GU14 8NL"
    }

    "should have the expected account link" in {
      document.getElementById("account-redirect").text() mustBe "To view your registration details, go to your Soft Drinks Industry Levy account."
      document.getElementById("account-link").attr("href") mustBe frontendAppConfig.accountFrontendHomeUrl
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
