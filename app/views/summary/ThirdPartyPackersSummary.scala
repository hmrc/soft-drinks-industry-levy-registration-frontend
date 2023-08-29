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
import models.backend.Subscription
import models.{CheckMode, HowManyLitresGlobally}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.govuk.summarylist._

object ThirdPartyPackersSummary extends RegisterDetailsSummaryRowHelper  {

  //LDS ignore
  override val key: String = "thirdPartyPackagers"
  override val action: String = routes.ThirdPartyPackagersController.onPageLoad(CheckMode).url
  override val actionId: String = "change-thirdPartyPackagers"
  override val hiddenText: String = "thirdPartyPackagers"

  def getOptHeadingAndSummary(subscription: Subscription, howManyLitresGlobally: HowManyLitresGlobally, isCheckAnswers: Boolean)
                             (implicit messages: Messages): Option[(String, SummaryList)] = {
    if(howManyLitresGlobally == HowManyLitresGlobally.Small) {
      Some(s"$key.checkYourAnswersLabel" -> SummaryListViewModel(rows =
        row(subscription.activity.Copackee, isCheckAnswers)
      ))
    } else {
      None
    }
  }

}
