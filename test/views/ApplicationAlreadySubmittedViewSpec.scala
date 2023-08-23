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
import viewmodels.AddressFormattingHelper
import views.html.ApplicationAlreadySubmittedView

class ApplicationAlreadySubmittedViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[ApplicationAlreadySubmittedView]
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-l"
  }

  "View" - {
    val registration = rosmRegistration.rosmRegistration
    val formattedAddress = AddressFormattingHelper.formatBusinessAddress(registration.address,Some(registration.organisationName))
    val html = view(formattedAddress)(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include(Messages("applicationAlreadySubmitted.heading.title"))
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual Messages("applicationAlreadySubmitted.heading.title")
    }

    "should have the expected address" in {
      document.getElementById("addressForUTR").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove Guildford GU14 8NL"
    }

    "should have the expected additional information paragraph" in {
      document.getElementById("already-submitted-message").text() mustBe "An application to register for the Soft " +
        "Drinks Industry Levy has been submitted for this business."
    }

    "should have a second paragraph with additional information including the account sign out link" in {
      document.getElementById("account-redirect").text() mustBe ("If you want to submit your return or check the status of your " +
        "application you need to sign in using the same Government Gateway account used to submit the application.")
      document.getElementById("account-link").attr("href") mustBe s"${frontendAppConfig.loginUrl}?continue=${frontendAppConfig.sdilHomeUrl}"
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
