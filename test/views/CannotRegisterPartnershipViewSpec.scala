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

import config.FrontendAppConfig
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.CannotRegisterPartnershipView

class CannotRegisterPartnershipViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[CannotRegisterPartnershipView]
  val config = application.injector.instanceOf[FrontendAppConfig]
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-l"
    val body = "govuk-body"
  }

  "View" - {
    val html = view(config.helpdeskPhoneNumber)(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include(Messages("cannotRegisterPartnership" + ".title"))
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual Messages("cannotRegisterPartnership" + ".heading")
    }

    "should have the expected body" in {
      document.getElementsByClass(Selectors.body).text() mustEqual Messages("cannotRegisterPartnership" + ".subText", config.helpdeskPhoneNumber)
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
