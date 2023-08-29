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
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions

class ContractPackingSummarySpec extends RegistrationSubscriptionHelper {

  "getOptHeadingAndSummary" - {
    s"when the litres is populated in the subscription" - {
      "should return a summary list with Yes and the number of litres" - {
        "with actions when isCheckAnswers" in {
          val subscription = generateSubscription(allFieldsPopulated = true)
          val res = ContractPackingSummary.getOptHeadingAndSummary(subscription, isCheckAnswers = true)
          val heading = res._1
          heading mustBe "contractPacking.checkYourAnswersLabel"
          val rows = res._2.rows
          rows.head.key.content.asHtml mustBe Html("Reporting contract packed at your own sites?")
          rows.head.key.classes mustBe ""
          rows.head.value.content.asHtml mustBe Html("Yes")
          rows.head.value.classes.trim mustBe "sdil-right-align--desktop"
          rows.head.actions.head.items.head.href must include("/change-contract-packing")
          rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-contractPacking")
          rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

          rows(1).key.content.asHtml mustBe Html("Litres in the low band")
          rows(1).key.classes mustBe ""
          rows(1).value.content.asHtml mustBe Html("1,000")
          rows(1).value.classes.trim mustBe "sdil-right-align--desktop"
          rows(1).actions.head.items.head.href must include("/change-how-many-contract-packing-next-12-months")
          rows(1).actions.head.items.head.attributes mustBe Map("id" -> "change-litresInLowBand-litreage-contractPacking")
          rows(1).actions.head.items.head.content.asHtml mustBe Html("Change")

          rows(2).key.content.asHtml mustBe Html("Litres in the high band")
          rows(2).key.classes mustBe ""
          rows(2).value.content.asHtml mustBe Html("2,000")
          rows(2).value.classes.trim mustBe "sdil-right-align--desktop"
          rows(2).actions.head.items.head.href must include("/change-how-many-contract-packing-next-12-months")
          rows(2).actions.head.items.head.attributes mustBe Map("id" -> "change-litresInHighBand-litreage-contractPacking")
          rows(2).actions.head.items.head.content.asHtml mustBe Html("Change")

          rows.size mustBe 3
        }

        "with actions when not isCheckAnswers" in {
          val subscription = generateSubscription(allFieldsPopulated = true)
          val res = ContractPackingSummary.getOptHeadingAndSummary(subscription, isCheckAnswers = false)
          val heading = res._1
          heading mustBe "contractPacking.checkYourAnswersLabel"
          val rows = res._2.rows
          rows.head.key.content.asHtml mustBe Html("Reporting contract packed at your own sites?")
          rows.head.key.classes mustBe ""
          rows.head.value.content.asHtml mustBe Html("Yes")
          rows.head.value.classes.trim mustBe "sdil-right-align--desktop"
          rows.head.actions.get mustBe Actions("", List.empty)


          rows(1).key.content.asHtml mustBe Html("Litres in the low band")
          rows(1).key.classes mustBe ""
          rows(1).value.content.asHtml mustBe Html("1,000")
          rows(1).value.classes.trim mustBe "sdil-right-align--desktop"
          rows(1).actions mustBe None


          rows(2).key.content.asHtml mustBe Html("Litres in the high band")
          rows(2).key.classes mustBe ""
          rows(2).value.content.asHtml mustBe Html("2,000")
          rows(2).value.classes.trim mustBe "sdil-right-align--desktop"
          rows(2).actions mustBe None
          rows.size mustBe 3
        }
      }
    }

    "and the litres is not populated in the subscription" - {
      "should return a summary list with No and not include number of litres" - {
        "with action when is checkAnswers" in {
          val subscription = generateSubscription(allFieldsPopulated = false)
          val res = ContractPackingSummary.getOptHeadingAndSummary(subscription, isCheckAnswers = true)
          val heading = res._1
          heading mustBe "contractPacking.checkYourAnswersLabel"
          val rows = res._2.rows
          rows.head.key.content.asHtml mustBe Html("Reporting contract packed at your own sites?")
          rows.head.key.classes mustBe ""
          rows.head.value.content.asHtml mustBe Html("No")
          rows.head.value.classes.trim mustBe "sdil-right-align--desktop"
          rows.head.actions.head.items.head.href must include("/change-contract-packing")
          rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-contractPacking")
          rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

          rows.size mustBe 1
        }
        "with no action when not checkAnswers" in {
          val subscription = generateSubscription(allFieldsPopulated = false)
          val res = ContractPackingSummary.getOptHeadingAndSummary(subscription, isCheckAnswers = false)
          val heading = res._1
          heading mustBe "contractPacking.checkYourAnswersLabel"
          val rows = res._2.rows
          rows.head.key.content.asHtml mustBe Html("Reporting contract packed at your own sites?")
          rows.head.key.classes mustBe ""
          rows.head.value.content.asHtml mustBe Html("No")
          rows.head.value.classes.trim mustBe "sdil-right-align--desktop"
          rows.head.actions.get mustBe Actions("", List.empty)

          rows.size mustBe 1
        }
      }
    }
  }
}
