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

import base.RegistrationSubscriptionHelper
import models.Contact
import play.twirl.api.Html
import viewmodels.summary.ContactDetailsSummary

class ContactDetailsSummarySpec extends RegistrationSubscriptionHelper {

  val NAME = "Jane Doe"
  val POSITION = "CEO"
  val PHONE = "07700 09900"
  val EMAIL = "sample@example.com"

  val userContact: Contact = Contact(
    Some(NAME),
    Some(POSITION),
    PHONE,
    EMAIL
  )

  "headingAndSummary" - {
    "should return the heading and summary list" - {
      val subscription = generateSubscription(allFieldsPopulated = false).copy(contact = contact)

      "that contains change links" - {
        "when called for check your answers" in {
          val res = ContactDetailsSummary.headingAndSummary(subscription, true)
          val (heading, summaryList) = res
          heading mustBe "Contact person details"
          val summaryRows = summaryList.rows
          summaryRows.size mustBe 4
          val fullNameRow = summaryRows.head
          fullNameRow.key.content.asHtml mustBe Html("Full name")
          fullNameRow.value.content.asHtml mustBe Html(NAME)
          fullNameRow.value.classes.trim mustBe "sdil-right-align--desktop"
          fullNameRow.actions.fold(0)(_.items.size) mustBe 1
          val fullNameAction = fullNameRow.actions.get.items.head
          fullNameAction.content.asHtml mustBe Html("Change")
          fullNameAction.href mustBe "/soft-drinks-industry-levy-registration/change-contact-details"
          fullNameAction.attributes mustBe Map("id" -> "change-fullName")

          val positionRow = summaryRows(1)
          positionRow.key.content.asHtml mustBe Html("Job title")
          positionRow.value.content.asHtml mustBe Html(POSITION)
          positionRow.value.classes.trim mustBe "sdil-right-align--desktop"
          positionRow.actions.fold(0)(_.items.size) mustBe 1
          val positionAction = positionRow.actions.get.items.head
          positionAction.content.asHtml mustBe Html("Change")
          positionAction.href mustBe "/soft-drinks-industry-levy-registration/change-contact-details"
          positionAction.attributes mustBe Map("id" -> "change-position")

          val phoneRow = summaryRows(2)
          phoneRow.key.content.asHtml mustBe Html("Telephone number")
          phoneRow.value.content.asHtml mustBe Html(PHONE)
          phoneRow.value.classes.trim mustBe "sdil-right-align--desktop"
          phoneRow.actions.fold(0)(_.items.size) mustBe 1
          val phoneAction = phoneRow.actions.get.items.head
          phoneAction.content.asHtml mustBe Html("Change")
          phoneAction.href mustBe "/soft-drinks-industry-levy-registration/change-contact-details"
          phoneAction.attributes mustBe Map("id" -> "change-phoneNumber")

          val emailRow = summaryRows(3)
          emailRow.key.content.asHtml mustBe Html("Email address")
          emailRow.value.content.asHtml mustBe Html(EMAIL)
          emailRow.value.classes.trim mustBe "sdil-right-align--desktop"
          emailRow.actions.fold(0)(_.items.size) mustBe 1
          val emailAction = emailRow.actions.get.items.head
          emailAction.content.asHtml mustBe Html("Change")
          emailAction.href mustBe "/soft-drinks-industry-levy-registration/change-contact-details"
          emailAction.attributes mustBe Map("id" -> "change-email")
        }
      }

      "that does not contains change links" - {
        "when not called for check your answers" in {
          val res = ContactDetailsSummary.headingAndSummary(subscription, false)
          val (heading, summaryList) = res
          heading mustBe "Contact person details"
          val summaryRows = summaryList.rows
          summaryRows.size mustBe 4
          val fullNameRow = summaryRows.head
          fullNameRow.key.content.asHtml mustBe Html("Full name")
          fullNameRow.value.content.asHtml mustBe Html(NAME)
          fullNameRow.value.classes.trim mustBe "sdil-right-align--desktop"
          fullNameRow.actions.fold(0)(_.items.size) mustBe 0

          val positionRow = summaryRows(1)
          positionRow.key.content.asHtml mustBe Html("Job title")
          positionRow.value.content.asHtml mustBe Html(POSITION)
          positionRow.value.classes.trim mustBe "sdil-right-align--desktop"
          positionRow.actions.fold(0)(_.items.size) mustBe 0

          val phoneRow = summaryRows(2)
          phoneRow.key.content.asHtml mustBe Html("Telephone number")
          phoneRow.value.content.asHtml mustBe Html(PHONE)
          phoneRow.value.classes.trim mustBe "sdil-right-align--desktop"
          phoneRow.actions.fold(0)(_.items.size) mustBe 0

          val emailRow = summaryRows(3)
          emailRow.key.content.asHtml mustBe Html("Email address")
          emailRow.value.content.asHtml mustBe Html(EMAIL)
          emailRow.value.classes.trim mustBe "sdil-right-align--desktop"
          emailRow.actions.fold(0)(_.items.size) mustBe 0
        }
      }
    }
  }
}