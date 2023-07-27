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
import models.{CheckMode, LitresInBands, UserAnswers}
import pages.{HowManyImportsPage, ImportsPage, QuestionPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import views.summary.{ReturnDetailsSummaryListWithLitres, SummaryListRowLitresHelper}

object ImportsSummary extends ReturnDetailsSummaryListWithLitres  {

  override val page: QuestionPage[Boolean] = ImportsPage
  override val optLitresPage: Option[QuestionPage[LitresInBands]] = Some(HowManyImportsPage)
  override val summaryLitres: SummaryListRowLitresHelper = HowManyImportsSummary
  //LDS ignore
  override val key: String = "imports"
  override val action: String = routes.ImportsController.onPageLoad(CheckMode).url
  override val actionId: String = "change-imports"
  override val hiddenText: String = "imports"

  def headingAndSummary(userAnswers: UserAnswers, isCheckAnswers: Boolean = true)(implicit messages: Messages): Option[(String, SummaryList)] = {
    val list = summaryList(userAnswers = userAnswers, isCheckAnswers)
    list.rows.headOption.fold(Option.empty[(String, SummaryList)])(_ => Some("imports.checkYourAnswersLabel" -> list))
  }

}
