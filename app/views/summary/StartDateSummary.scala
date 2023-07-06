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
import models.{CheckMode, UserAnswers}
import pages.{QuestionPage, StartDatePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.time.LocalDate
import java.time.format.DateTimeFormatter


object StartDateSummary {

  def row(answers: UserAnswers, isCheckAnswers: Boolean = false)(implicit messages: Messages): Seq[SummaryListRow] = {
    answers.get(page) match {
      case None => Seq.empty
      case Some(answered) =>
        val dateFormatter = DateTimeFormatter.ofPattern("dd MM yyyy")
        val value: String = answered.format(dateFormatter)
        Seq(
          SummaryListRowViewModel(
            key = key,
            value = ValueViewModel(value).withCssClass("govuk-!-text-align-right"),
            actions = if (isCheckAnswers) {
              Seq(
                ActionItemViewModel("site.change", action)
                  .withAttribute(("id", actionId))
                  .withVisuallyHiddenText(messages(s"$hiddenText.change.hidden"))
              )
            } else {
              Seq.empty
            }
          )
        )
    }
  }

  def summaryList(userAnswers: UserAnswers, isCheckAnswers: Boolean)(implicit messages: Messages): SummaryList = {
    SummaryListViewModel(rows =
      row(userAnswers, isCheckAnswers)
    )
  }

  val page: QuestionPage[LocalDate] = StartDatePage
  val key: String = "startDate.checkYourAnswersKey"
  val action: String = routes.StartDateController.onPageLoad(CheckMode).url
  val actionId: String = "change-startDate"
  val hiddenText: String = "startDate"
  def headingAndSummary(userAnswers: UserAnswers, isCheckAnswers: Boolean = true)(implicit messages: Messages): Option[(String, SummaryList)] = {
    val list = summaryList(userAnswers, isCheckAnswers)
    list.rows.headOption.fold(Option.empty[(String, SummaryList)])(_ => Some("startDate.checkYourAnswersLabel" -> list))
  }

}
