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
import models.Warehouse
import models.backend.{Site, UkAddress}
import pages.{AskSecondaryWarehousesPage, PackAtBusinessAddressPage}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Empty, Key, SummaryList, SummaryListRow, Text, Value}

class UKSitesSummarySpec extends SpecBase {

  val packagingSite1 = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    Some("Wild Lemonade Group"),
    None)

  lazy val packagingSiteListWith1 = Map(("78941132", packagingSite1))

  override val warehouse1 = Warehouse(
    Some("Warehouse One"),
    UkAddress(List("29 Station Place", "The Railyard", "Cambridge"), "CB1 2FP"))

  override lazy val warehouseListWith1 = Map(("78941132", warehouse1))

  "summaryList" - {

    "should return an None when no packaging site or warehouse list is passed in" in {
      val packagingSiteSummaryRowList = UKSitesSummary.summaryList(emptyUserAnswers,true)
      packagingSiteSummaryRowList mustBe None
    }

    "should return a Some summary for packing site when one packaging site and no warehouse is passed in" in {
      val packagingSiteSummaryRowList = UKSitesSummary.summaryList(emptyUserAnswers
        .copy(packagingSiteList = packagingSiteListWith1)
        .set(PackAtBusinessAddressPage, true).success.value
        .set(AskSecondaryWarehousesPage, false).success.value
        ,true)

      packagingSiteSummaryRowList mustBe
        Some((
          "checkYourAnswers.sites", SummaryList(List(SummaryListRow(Key(Text("You have 1 packaging site"), ""),
          Value(Empty, ""), "",
          Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-pack-at-business-address",
            Text("Change"), Some("the UK packaging site that you operate to produce liable drinks"), "",
            Map("id" -> "change-packaging-sites"))))))), None, "", Map())))
    }

    "should return summary for warehouses when one warehouse and no packing site is passed in" in {
      val warehouseSummaryRowList = UKSitesSummary.summaryList(emptyUserAnswers
        .copy(warehouseList = warehouseListWith1)
        .set(PackAtBusinessAddressPage, false).success.value
        .set(AskSecondaryWarehousesPage, true).success.value
        ,true)

      warehouseSummaryRowList mustBe
        Some(("checkYourAnswers.sites", SummaryList(List(SummaryListRow(Key(Text("You have 1 warehouse"), ""),
          Value(Empty, ""), "",
          Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-ask-secondary-warehouses",
            Text("Change"), Some("the UK warehouses you use to store liable drinks"), "",
            Map("id" -> "change-warehouse-sites"))))))), None, "", Map())))
    }


    "should return summary for warehouses and packing sites when one warehouse and one packing site is passed in" in {
      val warehouseSummaryRowList = UKSitesSummary.summaryList(emptyUserAnswers
        .copy(warehouseList = warehouseListWith1)
        .copy(packagingSiteList = packagingSiteListWith1)
        .set(PackAtBusinessAddressPage, true).success.value
        .set(AskSecondaryWarehousesPage, true).success.value
        ,true)

      warehouseSummaryRowList mustBe
        Some(("checkYourAnswers.sites", SummaryList(List(SummaryListRow(Key(Text("You have 1 packaging site"), ""),
          Value(Empty, ""), "", Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-pack-at-business-address",
            Text("Change"), Some("the UK packaging site that you operate to produce liable drinks"), "",
            Map("id" -> "change-packaging-sites")))))), SummaryListRow(Key(Text("You have 1 warehouse"), ""),
          Value(Empty, ""), "", Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-ask-secondary-warehouses",
            Text("Change"), Some("the UK warehouses you use to store liable drinks"), "",
            Map("id" -> "change-warehouse-sites"))))))), None, "", Map())))
    }
  }

}
