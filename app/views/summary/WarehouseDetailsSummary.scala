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
import models.{CheckMode, NormalMode, UserAnswers, Warehouse}
import pages.WarehouseDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.AddressFormattingHelper

object WarehouseDetailsSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(WarehouseDetailsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "warehouseDetails.checkYourAnswersLabel",
          value   = ValueViewModel(value).withCssClass("govuk-!-text-align-right"),
          actions = Seq(
            ActionItemViewModel("site.change", routes.WarehouseDetailsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("warehouseDetails.change.hidden"))
          )
        )
    }

  def warehouseDetailsRow(warehouseList: Map[String, Warehouse])(implicit messages: Messages): List[SummaryListRow] = {
    val numberOfWarehouses = warehouseList.size
    warehouseList.map {
      warehouse =>
        SummaryListRow(
          key = Key(
            content = HtmlContent(AddressFormattingHelper.addressFormatting(warehouse._2.address, warehouse._2.tradingName)),
            classes = "govuk-!-font-weight-regular govuk-!-width-full"
          ),
          actions = if(numberOfWarehouses != 1) {
            Some(Actions("", Seq(
              ActionItemViewModel("site.remove", routes.RemoveWarehouseDetailsController.onPageLoad(NormalMode, warehouse._1).url)
                .withCssClass("remove-link")
                .withVisuallyHiddenText(messages("warehouseDetails.remove.hidden"))
            )))
          } else {
            None
          }
        )
    }.toList
  }
}
