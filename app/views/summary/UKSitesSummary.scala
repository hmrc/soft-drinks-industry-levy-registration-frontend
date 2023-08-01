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
import models.{CheckMode, UserAnswers}
import pages.{AskSecondaryWarehousesPage, PackAtBusinessAddressPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{SummaryList, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, Empty, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import javax.swing.text.AbstractDocument.Content
import scala.reflect.internal.util.NoSourceFile.content

object UKSitesSummary {

  private def getPackAtBusinessAddressRow(userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages): SummaryListRow = {
      SummaryListRowViewModel(
        key =  if(userAnswers.packagingSiteList.size > 1){
          messages("checkYourAnswers.packing.checkYourAnswersLabel.multiple",  {userAnswers.packagingSiteList.size.toString})}else{
          messages("checkYourAnswers.packing.checkYourAnswersLabel.one")
        },
        value = Value(),
        actions = if (isCheckAnswers) {
          Seq(
            ActionItemViewModel("site.change", routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url)
              .withAttribute(("id", "change-packaging-sites"))
              .withVisuallyHiddenText(messages("checkYourAnswers.sites.packing.change.hidden"))
          )
        } else {
          Seq.empty
        }
      )
  }

  private def getAskSecondaryWarehouseRow (userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages): SummaryListRow = {
      SummaryListRowViewModel(
        key = if(userAnswers.warehouseList.size > 1){
          messages("checkYourAnswers.warehouse.checkYourAnswersLabel.multiple",  {userAnswers.warehouseList.size.toString})}else{
          messages("checkYourAnswers.warehouse.checkYourAnswersLabel.one")
        },
        value = Value(),
        actions = if (isCheckAnswers) {
          Seq(
            ActionItemViewModel("site.change", routes.AskSecondaryWarehousesController.onPageLoad(CheckMode).url)
              .withAttribute(("id", "change-warehouse-sites"))
              .withVisuallyHiddenText(messages("checkYourAnswers.sites.warehouse.change.hidden"))
          )
        } else {
          Seq.empty
        }
      )
  }

  def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean)
                 (implicit messages: Messages): Option[(String, SummaryList)] = {
    (userAnswers.get(PackAtBusinessAddressPage), userAnswers.get(AskSecondaryWarehousesPage)) match {
      case (Some(true), Some(false)) =>
        Option(
          SummaryListViewModel(
            rows = Seq(getPackAtBusinessAddressRow(userAnswers, isCheckAnswers))
          )
        ).map(list => "checkYourAnswers.sites" -> list)
      case (Some(false), Some(true)) =>
        Some(
          SummaryListViewModel(
            rows = Seq(getAskSecondaryWarehouseRow(userAnswers, isCheckAnswers))
          )
        ).map(list => "checkYourAnswers.sites" -> list)
      case (Some(true), Some(true)) =>
        Some(
          SummaryListViewModel(
            Seq(
              getPackAtBusinessAddressRow(userAnswers, isCheckAnswers),
              getAskSecondaryWarehouseRow(userAnswers, isCheckAnswers)
            )
          )
        ).map(list => "checkYourAnswers.sites" -> list)
      case _ => None
    }
  }

}