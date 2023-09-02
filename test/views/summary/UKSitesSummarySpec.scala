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

import base.RegistrationSubscriptionHelper
import models.backend.Site
import uk.gov.hmrc.govukfrontend.views.Aliases._

class UKSitesSummarySpec extends RegistrationSubscriptionHelper {

  val defaultSubscriptionNoSites = generateSubscription(allFieldsPopulated = false)


  "getHeadingAndSummary" - {

    "should return an None when no packaging site or warehouse list is passed in" in {
      val subscription = defaultSubscriptionNoSites
      val packagingSiteSummaryRowList = UKSitesSummary.getHeadingAndSummary(subscription, true)
      packagingSiteSummaryRowList mustBe None
    }

    "should return a Some summary for packing site when one packaging site and no warehouse is passed in" in {
      val subscription = defaultSubscriptionNoSites.copy(productionSites = packagingSiteListWith1.values.toSeq)

      val packagingSiteSummaryRowList = UKSitesSummary.getHeadingAndSummary(subscription, true)

      packagingSiteSummaryRowList mustBe
        Some((
          "checkYourAnswers.sites", SummaryList(List(SummaryListRow(Key(Text("You have 1 packaging site"), ""),
          Value(Empty, ""), "",
          Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-packaging-site-details",
            Text("Change"), Some("the UK packaging site that you operate to produce liable drinks"), "",
            Map("id" -> "change-packaging-sites"))))))), None, "", Map())))
    }

    "should return summary for warehouses when one warehouse and no packing site is passed in" in {
      val subscription = defaultSubscriptionNoSites.copy(warehouseSites = warehouseListWith1.values.map(Site.fromWarehouse(_)).toSeq)

      val warehouseSummaryRowList = UKSitesSummary.getHeadingAndSummary(subscription, true)

      warehouseSummaryRowList mustBe
        Some(("checkYourAnswers.sites", SummaryList(List(SummaryListRow(Key(Text("You have 1 warehouse"), ""),
          Value(Empty, ""), "",
          Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-warehouses",
            Text("Change"), Some("the UK warehouses you use to store liable drinks"), "",
            Map("id" -> "change-warehouse-sites"))))))), None, "", Map())))
    }


    "should return summary for warehouses and packing sites when one warehouse and one packing site is passed in" in {
      val subscription = defaultSubscriptionNoSites.copy(
        productionSites = packagingSiteListWith1.values.toSeq,
        warehouseSites = warehouseListWith1.values.map(Site.fromWarehouse(_)).toSeq)

      val warehouseSummaryRowList = UKSitesSummary.getHeadingAndSummary(subscription, true)

      warehouseSummaryRowList mustBe
        Some(("checkYourAnswers.sites", SummaryList(List(SummaryListRow(Key(Text("You have 1 packaging site"), ""),
          Value(Empty, ""), "", Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-packaging-site-details",
            Text("Change"), Some("the UK packaging site that you operate to produce liable drinks"), "",
            Map("id" -> "change-packaging-sites")))))), SummaryListRow(Key(Text("You have 1 warehouse"), ""),
          Value(Empty, ""), "", Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-warehouses",
            Text("Change"), Some("the UK warehouses you use to store liable drinks"), "",
            Map("id" -> "change-warehouse-sites"))))))), None, "", Map())))
    }
  }

}
