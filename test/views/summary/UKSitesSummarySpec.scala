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
import models.HowManyLitresGlobally
import uk.gov.hmrc.govukfrontend.views.Aliases._

class UKSitesSummarySpec extends RegistrationSubscriptionHelper {

  "getHeadingAndSummary" - {

    HowManyLitresGlobally.values.foreach { case howManyLitresGlobally =>
      "when the user is not voluntary" - {
        s"and is a $howManyLitresGlobally producer type" - {
          "that has both production sites and warehouses in the subscription" - {
            "should return summary for warehouses and packing sites" in {
              val subscription = generateSubscription(litresGlobally = howManyLitresGlobally, allFieldsPopulated = true)

              val headingAndSummary = UKSitesSummary.getHeadingAndSummary(subscription, howManyLitresGlobally, true)

              headingAndSummary mustBe defined
              val (heading, summary) = headingAndSummary.get
              heading mustBe "checkYourAnswers.sites"
              summary mustBe SummaryList(List(SummaryListRow(Key(Text("You have 1 packaging site"), ""),
                Value(Empty, ""), "", Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-packaging-site-details",
                  Text("Change"), Some("the UK packaging site that you operate to produce liable drinks"), "",
                  Map("id" -> "change-packaging-sites")))))), SummaryListRow(Key(Text("You have 1 warehouse"), ""),
                Value(Empty, ""), "", Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-warehouses",
                  Text("Change"), Some("the UK warehouses you use to store liable drinks"), "",
                  Map("id" -> "change-warehouse-sites"))))))), None, "", Map())
            }
          }

          "that has only production sites in the subscription" - {
            "should return summary for warehouses and packing sites" in {
              val subscription = generateSubscription(litresGlobally = howManyLitresGlobally, allFieldsPopulated = true)
                .copy(warehouseSites = Seq.empty)

              val headingAndSummary = UKSitesSummary.getHeadingAndSummary(subscription, howManyLitresGlobally, true)

              headingAndSummary mustBe defined
              val (heading, summary) = headingAndSummary.get
              heading mustBe "checkYourAnswers.sites"
              summary mustBe SummaryList(List(SummaryListRow(Key(Text("You have 1 packaging site"), ""),
                Value(Empty, ""), "", Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-packaging-site-details",
                  Text("Change"), Some("the UK packaging site that you operate to produce liable drinks"), "",
                  Map("id" -> "change-packaging-sites")))))), SummaryListRow(Key(Text("You have 0 warehouses"), ""),
                Value(Empty, ""), "", Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-ask-secondary-warehouses",
                  Text("Change"), Some("the UK warehouses you use to store liable drinks"), "",
                  Map("id" -> "change-warehouse-sites"))))))), None, "", Map())
            }
          }

          "that only has warehouse sites in the subscription" - {
            "should return summary for warehouses only" in {
              val subscription = generateSubscription(litresGlobally = howManyLitresGlobally, allFieldsPopulated = true)
                .copy(productionSites = Seq.empty)

              val headingAndSummary = UKSitesSummary.getHeadingAndSummary(subscription, howManyLitresGlobally, true)

              headingAndSummary mustBe defined
              val (heading, summary) = headingAndSummary.get
              heading mustBe "checkYourAnswers.sites"
              summary mustBe SummaryList(List(SummaryListRow(Key(Text("You have 1 warehouse"), ""),
                Value(Empty, ""), "", Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-warehouses",
                  Text("Change"), Some("the UK warehouses you use to store liable drinks"), "",
                  Map("id" -> "change-warehouse-sites"))))))), None, "", Map())
            }
          }

          "that has no production or warehouse sites in the subscription" - {
            "should return summary for warehouses only" in {
              val subscription = generateSubscription(litresGlobally = howManyLitresGlobally, allFieldsPopulated = true)
                .copy(warehouseSites = Seq.empty, productionSites = Seq.empty)

              val headingAndSummary = UKSitesSummary.getHeadingAndSummary(subscription, howManyLitresGlobally, true)

              headingAndSummary mustBe defined
              val (heading, summary) = headingAndSummary.get
              heading mustBe "checkYourAnswers.sites"
              summary mustBe SummaryList(List(SummaryListRow(Key(Text("You have 0 warehouses"), ""),
                Value(Empty, ""), "", Some(Actions("", List(ActionItem("/soft-drinks-industry-levy-registration/change-ask-secondary-warehouses",
                  Text("Change"), Some("the UK warehouses you use to store liable drinks"), "",
                  Map("id" -> "change-warehouse-sites"))))))), None, "", Map())
            }
          }
        }
      }
    }

    "when the user is voluntary" - {
      "and there is no packaging sites or warehouses" - {
        "should return None" in {
          val headingAndSummary = UKSitesSummary.getHeadingAndSummary(voluntarySubscription, HowManyLitresGlobally.Small, true)

          headingAndSummary mustBe None
        }
      }
    }
  }
}
