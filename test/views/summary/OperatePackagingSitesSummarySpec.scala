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
import models.LitresInBands
import pages.{HowManyOperatePackagingSitesPage, OperatePackagingSitesPage}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions

class OperatePackagingSitesSummarySpec extends SpecBase {

  "summaryList" - {
    "should return correct elements when passed in with TRUE and litres provided and check answers is true" in {
      val userAnswers = emptyUserAnswers
        .set(OperatePackagingSitesPage, true).success.value
        .set(HowManyOperatePackagingSitesPage, LitresInBands(1000,2000)).success.value

      val res = OperatePackagingSitesSummary.summaryList(userAnswers, isCheckAnswers = true)
      res.rows.head.key.content.asHtml mustBe Html("Reporting own brands packaged at your own site?")
      res.rows.head.key.classes mustBe ""
      res.rows.head.value.content.asHtml mustBe Html("Yes")
      res.rows.head.value.classes.trim mustBe "govuk-!-text-align-right"
      res.rows.head.actions.head.items.head.href mustBe "/soft-drinks-industry-levy-registration/change-operate-packaging-sites"
      res.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-operatePackagingSites")
      res.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

      res.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
      res.rows(1).key.classes mustBe ""
      res.rows(1).value.content.asHtml mustBe Html("1,000")
      res.rows(1).value.classes.trim mustBe "govuk-!-text-align-right"
      res.rows(1).actions.head.items.head.href mustBe "/soft-drinks-industry-levy-registration/change-how-many-own-brands-next-12-months"
      res.rows(1).actions.head.items.head.attributes mustBe Map("id" -> "change-litresInLowBand-litreage-operatePackagingSites")
      res.rows(1).actions.head.items.head.content.asHtml mustBe Html("Change")

      res.rows(2).key.content.asHtml mustBe Html("Litres in the high band")
      res.rows(2).key.classes mustBe ""
      res.rows(2).value.content.asHtml mustBe Html("2,000")
      res.rows(2).value.classes.trim mustBe "govuk-!-text-align-right"
      res.rows(2).actions.head.items.head.href mustBe "/soft-drinks-industry-levy-registration/change-how-many-own-brands-next-12-months"
      res.rows(2).actions.head.items.head.attributes mustBe Map("id" -> "change-litresInHighBand-litreage-operatePackagingSites")
      res.rows(2).actions.head.items.head.content.asHtml mustBe Html("Change")

      res.rows.size mustBe 3
    }
    "should return correct elements when passed in with TRUE and litres provided and check answers is false" in {
      val userAnswers = emptyUserAnswers
        .set(OperatePackagingSitesPage, true).success.value
        .set(HowManyOperatePackagingSitesPage, LitresInBands(1000,2000)).success.value

      val res = OperatePackagingSitesSummary.summaryList(userAnswers, isCheckAnswers = false)
      res.rows.head.key.content.asHtml mustBe Html("Reporting own brands packaged at your own site?")
      res.rows.head.key.classes mustBe ""
      res.rows.head.value.content.asHtml mustBe Html("Yes")
      res.rows.head.value.classes.trim mustBe "govuk-!-text-align-right"
      res.rows.head.actions.get mustBe Actions("", List.empty)

      res.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
      res.rows(1).key.classes mustBe ""
      res.rows(1).value.content.asHtml mustBe Html("1,000")
      res.rows(1).value.classes.trim mustBe "govuk-!-text-align-right"
      res.rows(1).actions mustBe None

      res.rows(2).key.content.asHtml mustBe Html("Litres in the high band")
      res.rows(2).key.classes mustBe ""
      res.rows(2).value.content.asHtml mustBe Html("2,000")
      res.rows(2).value.classes.trim mustBe "govuk-!-text-align-right"
      res.rows(2).actions mustBe None

      res.rows.size mustBe 3
    }
    "should return correct elements when passed in with FALSE and NO litres provided" in {
      val userAnswers = emptyUserAnswers
        .set(OperatePackagingSitesPage, false).success.value

      val res = OperatePackagingSitesSummary.summaryList(userAnswers, isCheckAnswers = true)
      res.rows.head.key.content.asHtml mustBe Html("Reporting own brands packaged at your own site?")
      res.rows.head.key.classes mustBe ""
      res.rows.head.value.content.asHtml mustBe Html("No")
      res.rows.head.value.classes.trim mustBe "govuk-!-text-align-right"
      res.rows.head.actions.head.items.head.href mustBe "/soft-drinks-industry-levy-registration/change-operate-packaging-sites"
      res.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-operatePackagingSites")
      res.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

      res.rows.size mustBe 1
    }
    "should return correct elements when no elements provided" in {
      val userAnswers = emptyUserAnswers

      val res = OperatePackagingSitesSummary.summaryList(userAnswers, isCheckAnswers = true)
      res.rows.size mustBe 0
    }
  }
  "checkAnswersSummary" - {
    "should return correct elements when passed in with TRUE and litres provided and check answers is true" in {
      val userAnswers = emptyUserAnswers
        .set(OperatePackagingSitesPage, true).success.value
        .set(HowManyOperatePackagingSitesPage, LitresInBands(1000,2000)).success.value

      val res = OperatePackagingSitesSummary.headingAndSummary(userAnswers)
      res.get._1 mustBe "operatePackagingSites.checkYourAnswersLabel"
      val summaryList = res.get._2

      summaryList.rows.head.key.content.asHtml mustBe Html("Reporting own brands packaged at your own site?")
      summaryList.rows.head.key.classes mustBe ""
      summaryList.rows.head.value.content.asHtml mustBe Html("Yes")
      summaryList.rows.head.value.classes.trim mustBe "govuk-!-text-align-right"
      summaryList.rows.head.actions.head.items.head.href mustBe "/soft-drinks-industry-levy-registration/change-operate-packaging-sites"
      summaryList.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-operatePackagingSites")
      summaryList.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

      summaryList.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
      summaryList.rows(1).key.classes mustBe ""
      summaryList.rows(1).value.content.asHtml mustBe Html("1,000")
      summaryList.rows(1).value.classes.trim mustBe "govuk-!-text-align-right"
      summaryList.rows(1).actions.head.items.head.href mustBe "/soft-drinks-industry-levy-registration/change-how-many-own-brands-next-12-months"
      summaryList.rows(1).actions.head.items.head.attributes mustBe Map("id" -> "change-litresInLowBand-litreage-operatePackagingSites")
      summaryList.rows(1).actions.head.items.head.content.asHtml mustBe Html("Change")

      summaryList.rows(2).key.content.asHtml mustBe Html("Litres in the high band")
      summaryList.rows(2).key.classes mustBe ""
      summaryList.rows(2).value.content.asHtml mustBe Html("2,000")
      summaryList.rows(2).value.classes.trim mustBe "govuk-!-text-align-right"
      summaryList.rows(2).actions.head.items.head.href mustBe "/soft-drinks-industry-levy-registration/change-how-many-own-brands-next-12-months"
      summaryList.rows(2).actions.head.items.head.attributes mustBe Map("id" -> "change-litresInHighBand-litreage-operatePackagingSites")
      summaryList.rows(2).actions.head.items.head.content.asHtml mustBe Html("Change")

      summaryList.rows.size mustBe 3
    }
    "should return correct elements when passed in with TRUE and litres provided and check answers is false" in {
      val userAnswers = emptyUserAnswers
        .set(OperatePackagingSitesPage, true).success.value
        .set(HowManyOperatePackagingSitesPage, LitresInBands(1000,2000)).success.value

      val res = OperatePackagingSitesSummary.headingAndSummary(userAnswers, isCheckAnswers = false)
      res.get._1 mustBe "operatePackagingSites.checkYourAnswersLabel"
      val summaryList = res.get._2

      summaryList.rows.head.key.content.asHtml mustBe Html("Reporting own brands packaged at your own site?")
      summaryList.rows.head.key.classes mustBe ""
      summaryList.rows.head.value.content.asHtml mustBe Html("Yes")
      summaryList.rows.head.value.classes.trim mustBe "govuk-!-text-align-right"
      summaryList.rows.head.actions.get mustBe Actions("", List.empty)

      summaryList.rows(1).key.content.asHtml mustBe Html("Litres in the low band")
      summaryList.rows(1).key.classes mustBe ""
      summaryList.rows(1).value.content.asHtml mustBe Html("1,000")
      summaryList.rows(1).value.classes.trim mustBe "govuk-!-text-align-right"
      summaryList.rows(1).actions mustBe None

      summaryList.rows(2).key.content.asHtml mustBe Html("Litres in the high band")
      summaryList.rows(2).key.classes mustBe ""
      summaryList.rows(2).value.content.asHtml mustBe Html("2,000")
      summaryList.rows(2).value.classes.trim mustBe "govuk-!-text-align-right"
      summaryList.rows(2).actions mustBe None

      summaryList.rows.size mustBe 3
    }
    "should return correct elements when passed in with FALSE and NO litres provided" in {
      val userAnswers = emptyUserAnswers
        .set(OperatePackagingSitesPage, false).success.value

      val res = OperatePackagingSitesSummary.headingAndSummary(userAnswers)
      res.get._1 mustBe "operatePackagingSites.checkYourAnswersLabel"
      val summaryList = res.get._2

      summaryList.rows.head.key.content.asHtml mustBe Html("Reporting own brands packaged at your own site?")
      summaryList.rows.head.key.classes mustBe ""
      summaryList.rows.head.value.content.asHtml mustBe Html("No")
      summaryList.rows.head.value.classes.trim mustBe "govuk-!-text-align-right"
      summaryList.rows.head.actions.head.items.head.href mustBe "/soft-drinks-industry-levy-registration/change-operate-packaging-sites"
      summaryList.rows.head.actions.head.items.head.attributes mustBe Map("id" -> "change-operatePackagingSites")
      summaryList.rows.head.actions.head.items.head.content.asHtml mustBe Html("Change")

      summaryList.rows.size mustBe 1
    }
    "should return correct elements when no elements provided" in {
      val userAnswers = emptyUserAnswers

      val res = OperatePackagingSitesSummary.headingAndSummary(userAnswers)
      res mustBe None
    }
  }

}
