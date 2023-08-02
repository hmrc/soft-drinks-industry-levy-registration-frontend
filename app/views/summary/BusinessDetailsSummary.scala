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
import models.{CheckMode, HowManyLitresGlobally, RosmWithUtr, UserAnswers}
import pages.HowManyLitresGloballyPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, SummaryList, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BusinessDetailsSummary  {

  def headingAndSummary(userAnswers: UserAnswers, rosmWithUtr: RosmWithUtr, isCheckAnswers: Boolean = true)
                       (implicit messages: Messages): Option[(String, SummaryList)] = {
    userAnswers.get(HowManyLitresGloballyPage).map { numberOfLitres =>
      val heading = messages("checkYourAnswers.businessDetails.subHeader")
      val address = userAnswers.address.getOrElse(rosmWithUtr.rosmRegistration.address)
      val formattedAddress = AddressFormattingHelper.formatBusinessAddress(address, None)
      val rows = if(isCheckAnswers) {
        rowsWithActions(rosmWithUtr, formattedAddress, numberOfLitres)
      } else {
        rowsWithNoActions(rosmWithUtr, formattedAddress, numberOfLitres)
      }
      val summaryList = SummaryListViewModel(
        rows
      )
      heading -> summaryList
    }
  }

  private def rowsWithActions(rosmWithUtr: RosmWithUtr, formattedAddress: Content, numberOfLitres: HowManyLitresGlobally)
                  (implicit messages: Messages): Seq[SummaryListRow] = {
    val businessAddressAction = getAction("businessAddress", routes.VerifyController.onPageLoad(CheckMode).url)
    val litresGloballyAction = getAction("howManyLitresGlobally", routes.HowManyLitresGloballyController.onPageLoad(CheckMode).url)
    val rosmRegistration = rosmWithUtr.rosmRegistration
    Seq(
      createSummaryListItem("utr", Text(rosmWithUtr.utr)),
      createSummaryListItem("name", Text(rosmRegistration.organisationName)),
      createSummaryListItem("address", formattedAddress, businessAddressAction),
      createSummaryListItem("litresGlobally", Text(messages(s"howManyLitresGlobally.${numberOfLitres.toString}")), litresGloballyAction)
    )
  }

  private def rowsWithNoActions(rosmWithUtr: RosmWithUtr, formattedAddress: Content, numberOfLitres: HowManyLitresGlobally)
                             (implicit messages: Messages): Seq[SummaryListRow] = {
    val rosmRegistration = rosmWithUtr.rosmRegistration
    Seq(
      createSummaryListItem("utr", Text(rosmWithUtr.utr)),
      createSummaryListItem("name", Text(rosmRegistration.organisationName)),
      createSummaryListItem("address", formattedAddress),
      createSummaryListItem("litresGlobally", Text(messages(s"howManyLitresGlobally.${numberOfLitres.toString}")))
    )
  }


  private def createSummaryListItem(fieldName: String, content: Content, actions: Seq[ActionItem] = Seq.empty[ActionItem])
                                   (implicit messages: Messages): SummaryListRow = {
    SummaryListRowViewModel(
      key = s"checkYourAnswers.businessDetails.$fieldName",
      value = ValueViewModel(content).withCssClass("sdil-right-align--desktop"),
      actions = actions
    )
  }

  private def getAction(fieldName: String, url: String)
                       (implicit messages: Messages): Seq[ActionItem] = {
    Seq(ActionItemViewModel("site.change", url)
      .withAttribute(("id", s"change-$fieldName"))
      .withVisuallyHiddenText(messages(s"businessDetails.$fieldName.change.hidden")))
  }
}
