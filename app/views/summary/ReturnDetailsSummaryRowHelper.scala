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
import pages.QuestionPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

trait ReturnDetailsSummaryRowHelper {

  val page: QuestionPage[Boolean]
  //LDS ignore
  val key: String
  val action: String
  val actionId: String
  val hiddenText: String

  def row(answers: UserAnswers, isCheckAnswers: Boolean = false)(implicit messages: Messages): Seq[SummaryListRow] = {
    answers.get(page) match {
      case None => Seq.empty
      case Some(answered) =>
        val value: String = if (answered) {
          "site.yes"
        } else {
          "site.no"
        }
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
}
