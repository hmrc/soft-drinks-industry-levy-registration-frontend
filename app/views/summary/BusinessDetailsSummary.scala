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

import controllers.routes
import models.backend.Subscription
import models.{ CheckMode, HowManyLitresGlobally }
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ SummaryList, Text }
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ Actions, SummaryListRow }
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BusinessDetailsSummary {

  def headingAndSummary(howManyLitresGlobally: HowManyLitresGlobally, subscription: Subscription, isCheckAnswers: Boolean = true)(implicit messages: Messages): (String, SummaryList) = {
    val heading = messages("checkYourAnswers.businessDetails.subHeader")
    val formattedAddress = AddressFormattingHelper.formatBusinessAddress(subscription.address, None)
    val rows = if (isCheckAnswers) {
      rowsWithActions(subscription, formattedAddress, howManyLitresGlobally)
    } else {
      rowsWithNoActions(subscription, formattedAddress, howManyLitresGlobally)
    }
    val summaryList = SummaryListViewModel(
      rows)
    heading -> summaryList
  }

  private def rowsWithActions(subscription: Subscription, formattedAddress: Content, numberOfLitres: HowManyLitresGlobally)(implicit messages: Messages): Seq[SummaryListRow] = {
    val businessAddressAction = getAction("businessAddress", routes.VerifyController.onPageLoad(CheckMode).url)
    val litresGloballyAction = getAction("howManyLitresGlobally", routes.HowManyLitresGloballyController.onPageLoad(CheckMode).url)
    Seq(
      createSummaryListItem("utr", Text(subscription.utr)),
      createSummaryListItem("name", Text(subscription.orgName)),
      createSummaryListItem("address", formattedAddress, Some(businessAddressAction)),
      createSummaryListItem("litresGlobally", Text(messages(s"howManyLitresGlobally.${numberOfLitres.toString}")), Some(litresGloballyAction)))
  }

  private def rowsWithNoActions(subscription: Subscription, formattedAddress: Content, numberOfLitres: HowManyLitresGlobally)(implicit messages: Messages): Seq[SummaryListRow] = {
    Seq(
      createSummaryListItem("utr", Text(subscription.utr)),
      createSummaryListItem("name", Text(subscription.orgName)),
      createSummaryListItem("address", formattedAddress),
      createSummaryListItem("litresGlobally", Text(messages(s"howManyLitresGlobally.${numberOfLitres.toString}"))))
  }

  private def createSummaryListItem(fieldName: String, content: Content, actions: Option[Actions] = None)(implicit messages: Messages): SummaryListRow = {
    SummaryListRow(
      key = s"checkYourAnswers.businessDetails.$fieldName",
      value = ValueViewModel(content).withCssClass("sdil-right-align--desktop"),
      classes = "govuk-summary-list__row",
      actions = actions.headOption)
  }

  private def getAction(fieldName: String, url: String)(implicit messages: Messages): Actions = {
    val actionItems = Seq(ActionItemViewModel("site.change", url)
      .withAttribute(("id", s"change-$fieldName"))
      .withVisuallyHiddenText(messages(s"businessDetails.$fieldName.change.hidden")))
    Actions("", actionItems)
  }
}
