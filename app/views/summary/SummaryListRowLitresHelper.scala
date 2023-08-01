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

import models.LitresInBands
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

trait SummaryListRowLitresHelper {

  val actionUrl: String
  val bandActionIdKey: String
  val bandHiddenKey: String

  def rows(litresInBands: LitresInBands, isCheckAnswers: Boolean)(implicit messages: Messages): Seq[SummaryListRow] = {
    Seq(
      bandRow(litresInBands.lowBand, "litresInLowBand", isCheckAnswers),
      bandRow(litresInBands.highBand, "litresInHighBand", isCheckAnswers)
    )
  }

  private def bandRow(litres: Long, band: String, isCheckAnswers: Boolean)(implicit messages: Messages): SummaryListRow = {

    val value = HtmlFormat.escape(java.text.NumberFormat.getInstance.format(litres)).toString
    SummaryListRow(
      key = band,
      value = ValueViewModel(HtmlContent(value)).withCssClass("sdil-right-align--desktop"),
      classes = "govuk-summary-list__row",
      actions = action(isCheckAnswers, band)
    )
}

  def action(isCheckAnswers: Boolean, band: String)(implicit messages: Messages): Option[Actions] = if (isCheckAnswers) {
    Some(Actions("",
      items =
        Seq(
          ActionItemViewModel("site.change", actionUrl)
            .withAttribute(("id", s"change-$band-litreage-$bandActionIdKey"))
            .withVisuallyHiddenText(messages(s"$bandHiddenKey.$band.litres.hidden")))))
  } else {
    None
  }

}
