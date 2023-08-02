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
import models.backend.UkAddress
import models.{HowManyLitresGlobally, OrganisationDetails, RosmRegistration, RosmWithUtr}
import pages.HowManyLitresGloballyPage
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.summary.BusinessDetailsSummary

class BusinessDetailsSummarySpec extends SpecBase {

  val ROSM_ADDRESS = UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP")
  val CHANGED_ADDRESS = UkAddress(List("117 Jerusalem Court", "St Albans"), "AL10 3UJ")
  val UTR = "1234567890"
  val ORG_NAME = "wizzbang"
  val ROSM_REGISTRATION = RosmRegistration("safeId", Some(OrganisationDetails(ORG_NAME)), None, ROSM_ADDRESS)
  val ROSM_WITH_UTR = RosmWithUtr(UTR, ROSM_REGISTRATION)

  def formatAddress(address: UkAddress) = {
    address.lines.mkString("<br/>") + "<br/>" + address.postCode
  }

  def expectedLitresContent(numberOfLitresGlobally: HowManyLitresGlobally): String = numberOfLitresGlobally match {
    case HowManyLitresGlobally.Large => "1 million litres or more"
    case HowManyLitresGlobally.Small => "Less than 1 million litres"
    case _ => "None"
  }


  "headingAndSummary" - {
    "should return the expected heading and summary list" - {
      HowManyLitresGlobally.values.foreach { numberOfLitresGlobally =>
        s"when the user produced $numberOfLitresGlobally globally over the last 12 months," - {
          val userAnswersWithGloballyProduced = emptyUserAnswers.set(HowManyLitresGloballyPage, numberOfLitresGlobally).success.value
          " did not change their business address" - {
            "and the check your answers page is being viewed" in {
              val res = BusinessDetailsSummary.headingAndSummary(userAnswersWithGloballyProduced, ROSM_WITH_UTR, true)
              res mustBe defined
              val (heading, summaryList) = res.get
              heading mustBe "Business details"
              val summaryRows = summaryList.rows
              summaryRows.size mustBe 4
              val utrRow = summaryRows.head
              utrRow.key.content.asHtml mustBe Html("Unique Taxpayer Reference (UTR)")
              utrRow.value.content.asHtml mustBe Html(UTR)
              utrRow.value.classes.trim mustBe "sdil-right-align--desktop"
              utrRow.actions.fold(0)(_.items.size) mustBe 0

              val nameRow = summaryRows(1)
              nameRow.key.content.asHtml mustBe Html("Business name")
              nameRow.value.content.asHtml mustBe Html(ORG_NAME)
              nameRow.value.classes.trim mustBe "sdil-right-align--desktop"
              nameRow.actions.fold(0)(_.items.size) mustBe 0

              val addressRow = summaryRows(2)
              addressRow.key.content.asHtml mustBe Html("Business address")
              addressRow.value.content mustBe HtmlContent(formatAddress(ROSM_ADDRESS))
              addressRow.value.classes.trim mustBe "sdil-right-align--desktop"
              addressRow.actions.fold(0)(_.items.size) mustBe 1
              val addressAction = addressRow.actions.get.items.head
              addressAction.content.asHtml mustBe Html("Change")
              addressAction.href mustBe "/soft-drinks-industry-levy-registration/change-verify"
              addressAction.attributes mustBe Map("id" -> "change-businessAddress")

              val globalLitresRow = summaryRows(3)
              globalLitresRow.key.content.asHtml mustBe Html("Litres of your own brands of liable drinks packaged globally in the past 12 months")
              globalLitresRow.value.content.asHtml mustBe Html(expectedLitresContent(numberOfLitresGlobally))
              globalLitresRow.value.classes.trim mustBe "sdil-right-align--desktop"
              globalLitresRow.actions.fold(0)(_.items.size) mustBe 1
              val globalLitresAction = globalLitresRow.actions.get.items.head
              globalLitresAction.content.asHtml mustBe Html("Change")
              globalLitresAction.href mustBe "/soft-drinks-industry-levy-registration/change-how-many-litres-globally"
              globalLitresAction.attributes mustBe Map("id" -> "change-howManyLitresGlobally")
            }

            "and the registration sent page is being viewed" in {
              val res = BusinessDetailsSummary.headingAndSummary(userAnswersWithGloballyProduced, ROSM_WITH_UTR, false)
              res mustBe defined
              val (heading, summaryList) = res.get
              heading mustBe "Business details"
              val summaryRows = summaryList.rows
              summaryRows.size mustBe 4
              val utrRow = summaryRows.head
              utrRow.key.content.asHtml mustBe Html("Unique Taxpayer Reference (UTR)")
              utrRow.value.content.asHtml mustBe Html(UTR)
              utrRow.value.classes.trim mustBe "sdil-right-align--desktop"
              utrRow.actions.fold(0)(_.items.size) mustBe 0

              val nameRow = summaryRows(1)
              nameRow.key.content.asHtml mustBe Html("Business name")
              nameRow.value.content.asHtml mustBe Html(ORG_NAME)
              nameRow.value.classes.trim mustBe "sdil-right-align--desktop"
              nameRow.actions.fold(0)(_.items.size) mustBe 0

              val addressRow = summaryRows(2)
              addressRow.key.content.asHtml mustBe Html("Business address")
              addressRow.value.content mustBe HtmlContent(formatAddress(ROSM_ADDRESS))
              addressRow.value.classes.trim mustBe "sdil-right-align--desktop"
              addressRow.actions.fold(0)(_.items.size) mustBe 0

              val globalLitresRow = summaryRows(3)
              globalLitresRow.key.content.asHtml mustBe Html("Litres of your own brands of liable drinks packaged globally in the past 12 months")
              globalLitresRow.value.content.asHtml mustBe Html(expectedLitresContent(numberOfLitresGlobally))
              globalLitresRow.value.classes.trim mustBe "sdil-right-align--desktop"
              globalLitresRow.actions.fold(0)(_.items.size) mustBe 0
            }
          }
          " changed their business address" - {
            val userAnswersWithLitresAndUpdatedAddress = userAnswersWithGloballyProduced.copy(address = Some(CHANGED_ADDRESS))
            "and the check your answers page is being viewed" in {
              val res = BusinessDetailsSummary.headingAndSummary(userAnswersWithLitresAndUpdatedAddress, ROSM_WITH_UTR, true)
              res mustBe defined
              val (heading, summaryList) = res.get
              heading mustBe "Business details"
              val summaryRows = summaryList.rows
              summaryRows.size mustBe 4
              val utrRow = summaryRows.head
              utrRow.key.content.asHtml mustBe Html("Unique Taxpayer Reference (UTR)")
              utrRow.value.content.asHtml mustBe Html(UTR)
              utrRow.value.classes.trim mustBe "sdil-right-align--desktop"
              utrRow.actions.fold(0)(_.items.size) mustBe 0

              val nameRow = summaryRows(1)
              nameRow.key.content.asHtml mustBe Html("Business name")
              nameRow.value.content.asHtml mustBe Html(ORG_NAME)
              nameRow.value.classes.trim mustBe "sdil-right-align--desktop"
              nameRow.actions.fold(0)(_.items.size) mustBe 0

              val addressRow = summaryRows(2)
              addressRow.key.content.asHtml mustBe Html("Business address")
              addressRow.value.content mustBe HtmlContent(formatAddress(CHANGED_ADDRESS))
              addressRow.value.classes.trim mustBe "sdil-right-align--desktop"
              addressRow.actions.fold(0)(_.items.size) mustBe 1
              val addressAction = addressRow.actions.get.items.head
              addressAction.content.asHtml mustBe Html("Change")
              addressAction.href mustBe "/soft-drinks-industry-levy-registration/change-verify"
              addressAction.attributes mustBe Map("id" -> "change-businessAddress")

              val globalLitresRow = summaryRows(3)
              globalLitresRow.key.content.asHtml mustBe Html("Litres of your own brands of liable drinks packaged globally in the past 12 months")
              globalLitresRow.value.content.asHtml mustBe Html(expectedLitresContent(numberOfLitresGlobally))
              globalLitresRow.value.classes.trim mustBe "sdil-right-align--desktop"
              globalLitresRow.actions.fold(0)(_.items.size) mustBe 1
              val globalLitresAction = globalLitresRow.actions.get.items.head
              globalLitresAction.content.asHtml mustBe Html("Change")
              globalLitresAction.href mustBe "/soft-drinks-industry-levy-registration/change-how-many-litres-globally"
              globalLitresAction.attributes mustBe Map("id" -> "change-howManyLitresGlobally")
            }

            "and the registration sent page is being viewed" in {
              val res = BusinessDetailsSummary.headingAndSummary(userAnswersWithLitresAndUpdatedAddress, ROSM_WITH_UTR, false)
              res mustBe defined
              val (heading, summaryList) = res.get
              heading mustBe "Business details"
              val summaryRows = summaryList.rows
              summaryRows.size mustBe 4
              val utrRow = summaryRows.head
              utrRow.key.content.asHtml mustBe Html("Unique Taxpayer Reference (UTR)")
              utrRow.value.content.asHtml mustBe Html(UTR)
              utrRow.value.classes.trim mustBe "sdil-right-align--desktop"
              utrRow.actions.fold(0)(_.items.size) mustBe 0

              val nameRow = summaryRows(1)
              nameRow.key.content.asHtml mustBe Html("Business name")
              nameRow.value.content.asHtml mustBe Html(ORG_NAME)
              nameRow.value.classes.trim mustBe "sdil-right-align--desktop"
              nameRow.actions.fold(0)(_.items.size) mustBe 0

              val addressRow = summaryRows(2)
              addressRow.key.content.asHtml mustBe Html("Business address")
              addressRow.value.content mustBe HtmlContent(formatAddress(CHANGED_ADDRESS))
              addressRow.value.classes.trim mustBe "sdil-right-align--desktop"
              addressRow.actions.fold(0)(_.items.size) mustBe 0

              val globalLitresRow = summaryRows(3)
              globalLitresRow.key.content.asHtml mustBe Html("Litres of your own brands of liable drinks packaged globally in the past 12 months")
              globalLitresRow.value.content.asHtml mustBe Html(expectedLitresContent(numberOfLitresGlobally))
              globalLitresRow.value.classes.trim mustBe "sdil-right-align--desktop"
              globalLitresRow.actions.fold(0)(_.items.size) mustBe 0
            }
          }
        }
      }
    }
    "should return None when the number of litres globally is not within the useranswers" in {
      val res = BusinessDetailsSummary.headingAndSummary(emptyUserAnswers, ROSM_WITH_UTR)
      res mustBe None
    }
  }
}