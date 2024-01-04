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

import models.CreatedSubscriptionAndAmountProducedGlobally
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.summary.{ BusinessDetailsSummary, ContactDetailsSummary }

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object RegistrationSummary {
  def summaryList(createdSubscriptionAndAmountProducedGlobally: CreatedSubscriptionAndAmountProducedGlobally, isCheckYourAnswers: Boolean = true)(implicit messages: Messages): Seq[(String, SummaryList)] = {

    val subscription = createdSubscriptionAndAmountProducedGlobally.subscription
    val howManyLitresGlobally = createdSubscriptionAndAmountProducedGlobally.howManyLitresGlobally

    val businessDetails: (String, SummaryList) = BusinessDetailsSummary.headingAndSummary(howManyLitresGlobally, subscription, isCheckYourAnswers)
    val thirdPartyPackersSummary: Option[(String, SummaryList)] = ThirdPartyPackersSummary.getOptHeadingAndSummary(subscription, howManyLitresGlobally, isCheckYourAnswers)
    val operatePackagingSites: Option[(String, SummaryList)] = OperatePackagingSitesSummary.getOptHeadingAndSummary(subscription, howManyLitresGlobally, isCheckYourAnswers)
    val contractPacking: (String, SummaryList) = ContractPackingSummary.getOptHeadingAndSummary(subscription, isCheckYourAnswers)
    val imports: (String, SummaryList) = ImportsSummary.getOptHeadingAndSummary(subscription, isCheckYourAnswers)
    val startDate: Option[(String, SummaryList)] = StartDateSummary.optHeadingAndSummary(subscription, howManyLitresGlobally, isCheckYourAnswers)
    val contactDetails: (String, SummaryList) = ContactDetailsSummary.headingAndSummary(subscription, isCheckYourAnswers)
    val packingDetails: Option[(String, SummaryList)] = UKSitesSummary.getHeadingAndSummary(subscription, createdSubscriptionAndAmountProducedGlobally.howManyLitresGlobally, isCheckYourAnswers)
    Seq(
      Some(businessDetails),
      thirdPartyPackersSummary,
      operatePackagingSites,
      Some(contractPacking),
      Some(imports),
      startDate,
      Some(contactDetails),
      packingDetails).flatten
  }

  def applicationSentFormattedDateTime(dateTime: LocalDateTime): String = {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("h:mma")

    val registeredDate = dateTime.format(dateFormatter)
    val registeredTime = dateTime.format(timeFormatter).toLowerCase

    s"$registeredDate at $registeredTime"
  }
}
