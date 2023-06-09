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

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.summary.ContactDetailsSummary

object RegistrationSummary {

  def summaryList(userAnswers: UserAnswers, isCheckYourAnswers: Boolean = true)
                 (implicit messages: Messages): Seq[(String, SummaryList)] = {
    val operatePackagingSites: Option[(String, SummaryList)] = OperatePackagingSitesSummary.headingAndSummary(userAnswers, isCheckYourAnswers)
    val contractPacking: Option[(String, SummaryList)] = ContractPackingSummary.headingAndSummary(userAnswers, isCheckYourAnswers)
    val imports: Option[(String, SummaryList)] = ImportsSummary.headingAndSummary(userAnswers, isCheckYourAnswers)
    val startDate: Option[(String, SummaryList)] = StartDateSummary.headingAndSummary(userAnswers)
    val contactDetails: Option[(String, SummaryList)] = ContactDetailsSummary.headingAndSummary(userAnswers, isCheckYourAnswers)
    Seq(
      operatePackagingSites,
      contractPacking,
      imports,
      startDate,
      contactDetails
    ).flatten
  }

}
