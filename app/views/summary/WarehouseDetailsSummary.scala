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
import models.{Mode, NormalMode, Warehouse}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WarehouseDetailsSummary  {

  def warehouseDetailsRow(warehouseList: Map[String, Warehouse], mode: Mode)(implicit messages: Messages): List[SummaryListRow] = {
    val numberOfWarehouses = warehouseList.size
    warehouseList.map {
      warehouse =>
        SummaryListRow(
          key = Key(
            content = HtmlContent(AddressFormattingHelper.addressFormatting(warehouse._2.address, warehouse._2.tradingName)),
            classes = "govuk-!-font-weight-regular govuk-!-width-full"
          ),
          actions =
            Some(Actions("", Seq(
              ActionItemViewModel("site.remove", routes.RemoveWarehouseDetailsController.onPageLoad(mode, warehouse._1).url)
                .withCssClass("remove-link")
                .withVisuallyHiddenText(messages("warehouseDetails.remove.hidden", warehouse._2.tradingName.getOrElse(""), warehouse._2.address.lines.head)))
            ))
        )
    }.toList
  }
}
