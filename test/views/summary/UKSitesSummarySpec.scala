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


  "summaryList" - {

    "should return summary with link to pack-at-business-address for packaging sites and ask-secondary-warehouses for warehouses " +
      "when no packaging site or warehouse list is passed in" in {
      val subscription = defaultSubscriptionNoSites
      val ukSitesSummary = UKSitesSummary.summaryList(subscription,true)
      ukSitesSummary mustBe Some(("checkYourAnswers.sites",
        SummaryList(List(
          SummaryListRow(Key(Text("You have 0 packaging sites"), ""), Value(Empty, ""), "",
            Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-pack-at-business-address",
              Text("Change"), Some("the UK packaging site that you operate to produce liable drinks"), "", Map("id" -> "change-packaging-sites")))))),
          SummaryListRow(Key(Text("You have 0 warehouses"), ""), Value(Empty, ""), "",
            Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-ask-secondary-warehouses",
              Text("Change"), Some("the UK warehouses you use to store liable drinks"), "", Map("id" -> "change-warehouse-sites"))))))),
          None, "", Map())))
    }

    "should return summary with link to packaging-site-details for packaging sites and ask-secondary-warehouses for warehouses " +
      "when one packaging site and no warehouse are passed in" in {
      val subscription = defaultSubscriptionNoSites.copy(productionSites = packagingSiteListWith1.values.toSeq)

      val ukSitesSummary = UKSitesSummary.summaryList(subscription,true)

      ukSitesSummary mustBe Some(("checkYourAnswers.sites",
        SummaryList(List(
          SummaryListRow(Key(Text("You have 1 packaging site"), ""), Value(Empty, ""), "",
            Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-packaging-site-details",
              Text("Change"), Some("the UK packaging site that you operate to produce liable drinks"), "", Map("id" -> "change-packaging-sites")))))),
          SummaryListRow(Key(Text("You have 0 warehouses"), ""), Value(Empty, ""), "",
            Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-ask-secondary-warehouses",
              Text("Change"), Some("the UK warehouses you use to store liable drinks"), "", Map("id" -> "change-warehouse-sites"))))))),
          None, "", Map())))
    }

    "should return summary with link to pack-at-business-address for packaging sites and warehouse-details for warehouses " +
      "when no packaging site and one warehouse are passed in" in {
      val subscription = defaultSubscriptionNoSites.copy(warehouseSites = warehouseListWith1.values.map(Site.fromWarehouse(_)).toSeq)

      val ukSitesSummary = UKSitesSummary.summaryList(subscription,true)

      ukSitesSummary mustBe Some(("checkYourAnswers.sites",
        SummaryList(List(
          SummaryListRow(Key(Text("You have 0 packaging sites"), ""), Value(Empty, ""), "",
            Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-pack-at-business-address",
              Text("Change"), Some("the UK packaging site that you operate to produce liable drinks"), "", Map("id" -> "change-packaging-sites")))))),
          SummaryListRow(Key(Text("You have 1 warehouse"), ""), Value(Empty, ""), "",
            Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-warehouses",
              Text("Change"), Some("the UK warehouses you use to store liable drinks"), "", Map("id" -> "change-warehouse-sites"))))))),
          None, "", Map())))
    }

    "should return summary with link to packaging-site-details for packaging sites and warehouse-details for warehouses " +
      "when one packaging site and one warehouse are passed in" in {
      val subscription = defaultSubscriptionNoSites.copy(
        productionSites = packagingSiteListWith1.values.toSeq,
        warehouseSites = warehouseListWith1.values.map(Site.fromWarehouse(_)).toSeq)

      val ukSitesSummary = UKSitesSummary.summaryList(subscription, true)

      ukSitesSummary mustBe Some(("checkYourAnswers.sites",
        SummaryList(List(
          SummaryListRow(Key(Text("You have 1 packaging site"), ""), Value(Empty, ""), "",
            Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-packaging-site-details",
              Text("Change"), Some("the UK packaging site that you operate to produce liable drinks"), "", Map("id" -> "change-packaging-sites")))))),
          SummaryListRow(Key(Text("You have 1 warehouse"), ""), Value(Empty, ""), "",
            Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-warehouses",
              Text("Change"), Some("the UK warehouses you use to store liable drinks"), "", Map("id" -> "change-warehouse-sites"))))))),
          None, "", Map())))
    }

  }

}
