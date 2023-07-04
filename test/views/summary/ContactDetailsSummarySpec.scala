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

package views.summary

import base.SpecBase
import models.{ContactDetails, UserAnswers}
import play.api.libs.json.Json
import viewmodels.summary.ContactDetailsSummary

class ContactDetailsSummarySpec extends SpecBase {

  val userContactDetails: ContactDetails = ContactDetails("Jane Doe", "CEO", "07700 09900", "sample@example.com")

  "row" - {

    "should return nothing when no contact details are passed in" in {
      val contactDetailsSummaryRow = ContactDetailsSummary.row(emptyUserAnswers)

      contactDetailsSummaryRow mustBe None
    }

    "should return a summary list row with the appropriate contact details if Contact details have been added" in {
      val userAnswersWithContact = UserAnswers("id", Json.obj("contactDetails" -> userContactDetails))

      val contactDetailsSummaryRow = ContactDetailsSummary.row(userAnswersWithContact)

      contactDetailsSummaryRow.head.key.content.asHtml.toString mustBe "Contact person details"
      contactDetailsSummaryRow.head.value.content.asHtml.toString mustBe "Jane Doe<br/>CEO<br/>07700 09900<br/>sample@example.com"
      contactDetailsSummaryRow.head.actions.toList.head.items.head.content.asHtml.toString() must include("Change")
    }
  }
}