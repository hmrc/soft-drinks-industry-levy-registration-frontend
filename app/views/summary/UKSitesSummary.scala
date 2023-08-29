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
import models.CheckMode
import models.backend.Subscription
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{SummaryList, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UKSitesSummary {

  private def getPackAtBusinessAddressRow(subscription: Subscription, isCheckAnswers: Boolean)(implicit messages: Messages): SummaryListRow = {
      SummaryListRowViewModel(
        key =  if(subscription.productionSites.size > 1){
          messages("checkYourAnswers.packing.checkYourAnswersLabel.multiple",  {subscription.productionSites.size.toString})} else{
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

  private def getAskSecondaryWarehouseRow (subscription: Subscription, isCheckAnswers: Boolean)(implicit messages: Messages): SummaryListRow = {
      SummaryListRowViewModel(
        key = if(subscription.warehouseSites.size > 1){
          messages("checkYourAnswers.warehouse.checkYourAnswersLabel.multiple",  {subscription.warehouseSites.size.toString})} else {
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

  def summaryList(subscription: Subscription, isCheckAnswers: Boolean)
                 (implicit messages: Messages): Option[(String, SummaryList)] = {

    (subscription.productionSites.nonEmpty, subscription.warehouseSites.nonEmpty) match {
      case (true, false) =>
        Option(
          SummaryListViewModel(
            rows = Seq(getPackAtBusinessAddressRow(subscription, isCheckAnswers))
          )
        ).map(list => "checkYourAnswers.sites" -> list)
      case (false, true) =>
        Some(
          SummaryListViewModel(
            rows = Seq(getAskSecondaryWarehouseRow(subscription, isCheckAnswers))
          )
        ).map(list => "checkYourAnswers.sites" -> list)
      case (true, true) =>
        Some(
          SummaryListViewModel(
            Seq(
              getPackAtBusinessAddressRow(subscription, isCheckAnswers),
              getAskSecondaryWarehouseRow(subscription, isCheckAnswers)
            )
          )
        ).map(list => "checkYourAnswers.sites" -> list)
      case _ => None
    }
  }

}
