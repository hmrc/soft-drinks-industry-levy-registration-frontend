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
import models.backend.Site
import pages.{AskSecondaryWarehousesPage, ContactDetailsPage, PackAtBusinessAddressPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow}
import viewmodels.AddressFormattingHelper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import viewmodels.summary.ContactDetailsSummary.rows

object PackagingSiteDetailsSummary {



  def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean)
                 (implicit messages: Messages): Option[(String, SummaryList)] = {
    (userAnswers.get(PackAtBusinessAddressPage), userAnswers.get(AskSecondaryWarehousesPage)) match {
      case (Some(true), Some(false)) =>
          Option(
          SummaryListViewModel(
            rows = Seq(SummaryListRowViewModel(
              key = messages("packagingSiteDetails.checkYourAnswersLabel" ,
                userAnswers.packagingSiteList.size.toString, if(userAnswers.packagingSiteList.size > 1) {"s"} else {""}),
              value = ValueViewModel(userAnswers.packagingSiteList.size.toString).withCssClass("align-right"),
              actions = if (isCheckAnswers) {
                Seq(
                  ActionItemViewModel("site.change", routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url)
                    .withAttribute(("id", "change-packaging-sites"))
                    .withVisuallyHiddenText(messages("packAtBusinessAddress.change.hidden"))
                )
              } else {
                Seq.empty
              }
            )
            )
          )
        ).map(list => "checkYourAnswers.sites" -> list)
      case (Some(false), Some(true)) =>
        Some(
          SummaryListViewModel(
            rows = Seq(SummaryListRowViewModel(
              key = messages("warehouseDetails.checkYourAnswersLabel",
                userAnswers.warehouseList.size.toString, if(userAnswers.warehouseList.size > 1) {"s"} else {""}),
              value = ValueViewModel(userAnswers.warehouseList.size.toString).withCssClass("align-right"),
              actions = if (isCheckAnswers) {
                Seq(
                  ActionItemViewModel("site.change", routes.AskSecondaryWarehousesController.onPageLoad(CheckMode).url)
                    .withAttribute(("id", "change-packaging-sites"))
                    .withVisuallyHiddenText(messages("SecondaryWarehouse.change.hidden"))
                )
              } else {
                Seq.empty
              }
            )
            )
          )
        ).map(list => "checkYourAnswers.sites" -> list)
      case (Some(true), Some(true)) =>
        Some(
          SummaryListViewModel(
             Seq(
               SummaryListRowViewModel(
                 key = messages("packagingSiteDetails.checkYourAnswersLabel" ,
                   userAnswers.packagingSiteList.size.toString, if(userAnswers.packagingSiteList.size > 1) {"s"} else {""}),
                 value = ValueViewModel(userAnswers.packagingSiteList.size.toString).withCssClass("align-right"),
                 actions = if (isCheckAnswers) {
                   Seq(
                     ActionItemViewModel("site.change", routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url)
                       .withAttribute(("id", "change-packaging-sites"))
                       .withVisuallyHiddenText(messages("packAtBusinessAddress.change.hidden"))
                   )
                 } else {
                   Seq.empty
                 }
               ),
                SummaryListRowViewModel(
                key = messages("warehouseDetails.checkYourAnswersLabel",
                      userAnswers.warehouseList.size.toString, if(userAnswers.warehouseList.size > 1) {"s"} else {""}),
                value = ValueViewModel(userAnswers.warehouseList.size.toString).withCssClass("align-right"),
                actions = if (isCheckAnswers) {
                  Seq(
                    ActionItemViewModel("site.change", routes.AskSecondaryWarehousesController.onPageLoad(CheckMode).url)
                      .withAttribute(("id", "change-packaging-sites"))
                      .withVisuallyHiddenText(messages("SecondaryWarehouse.change.hidden"))
                  )
                } else {
                  Seq.empty
                }
              )
            )
          )
        ).map(list => "checkYourAnswers.sites" -> list)
      case _ => None
    }
  }

  def summaryList(packagingSiteList: Map[String, Site])(implicit messages: Messages): SummaryList = {
    SummaryListViewModel(
      rows = row2(packagingSiteList)
    )
  }

  def row2(packagingSiteList: Map[String, Site])(implicit messages: Messages): List[SummaryListRow] = {
    packagingSiteList.map {
      site =>
        SummaryListRow(
          key = Key(
            content =
              HtmlContent(AddressFormattingHelper.addressFormatting(site._2.address, site._2.tradingName)),
            classes = "govuk-!-font-weight-regular govuk-!-width-two-thirds"
          ),
          actions = if (packagingSiteList.size > 1) {
            Some(Actions("", Seq(
              ActionItemViewModel("site.remove", routes.RemovePackagingSiteDetailsController.onPageLoad(site._1).url)
                .withVisuallyHiddenText(messages("packagingSiteDetails.hidden"))
            )))
          } else {
            None
          }
        )
    }.toList
  }
}
