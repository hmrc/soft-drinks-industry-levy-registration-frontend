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

package viewmodels.summary

import controllers.routes
import models.{CheckMode, ContactDetails, UserAnswers}
import pages.ContactDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, SummaryList, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ContactDetailsSummary  {

  def headingAndSummary(userAnswers: UserAnswers, isCheckAnswers: Boolean = true)
                         (implicit messages: Messages): Option[(String, SummaryList)] = {
    userAnswers.get(ContactDetailsPage).map { contactDetails =>
      val heading = messages("contactDetails.title")
      val summaryList = SummaryListViewModel(
        rows(contactDetails, isCheckAnswers)
      )
      heading -> summaryList
    }
  }

  private def rows(contactDetails: ContactDetails, isCheckAnswers: Boolean)
          (implicit messages: Messages): Seq[SummaryListRow] = {
    Seq(
      createSummaryListItem("fullName", contactDetails.fullName, isCheckAnswers),
      createSummaryListItem("position", contactDetails.position, isCheckAnswers),
      createSummaryListItem("phoneNumber", contactDetails.phoneNumber, isCheckAnswers),
      createSummaryListItem("email", contactDetails.email, isCheckAnswers)
    )
  }


  private def createSummaryListItem(fieldName: String, fieldValue: String, isCheckAnswers: Boolean)
                                   (implicit messages: Messages): SummaryListRow = {
    SummaryListRowViewModel(
      key = s"contactDetails.$fieldName",
      value = ValueViewModel(Text(fieldValue)).withCssClass("govuk-!-text-align-right"),
      actions = if (isCheckAnswers) {
          Seq(
            getAction(fieldName)
          )
        } else {
          Seq.empty[ActionItem]
        }
    )
  }

  private def getAction(fieldName: String)
                       (implicit messages: Messages): ActionItem = {
    ActionItemViewModel("site.change", routes.ContactDetailsController.onPageLoad(CheckMode).url)
      .withAttribute(("id", s"change-$fieldName"))
      .withVisuallyHiddenText(messages(s"contactDetails.$fieldName.change.hidden"))
  }
}
