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

import models.Litreage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._

trait ReturnDetailsSummaryListWithLitres extends ReturnDetailsSummaryRowHelper {

  val summaryLitres: SummaryListRowLitresHelper
  //LDS ignore
  val key: String
  val action: String
  val actionId: String
  val hiddenText: String

  def getHeadingAndSummary(literage: Option[Litreage], isCheckAnswers: Boolean = true)(implicit messages: Messages): (String, SummaryList) = {
    val list = summaryList(literage, isCheckAnswers)
    s"$key.checkYourAnswersLabel" -> list
  }

  def summaryList(optLiterage: Option[Litreage], isCheckAnswers: Boolean)(implicit messages: Messages): SummaryList = {
    val litresDetails: Seq[SummaryListRow] = optLiterage.fold(Seq.empty[SummaryListRow])(getLitresDetails(_, isCheckAnswers))

    SummaryListViewModel(rows =
      row(optLiterage, isCheckAnswers) ++ litresDetails
    )
  }

  private def getLitresDetails(literage: Litreage, isCheckAnswers: Boolean)
                              (implicit messages: Messages): Seq[SummaryListRow] = {
    summaryLitres.rows(literage, isCheckAnswers)
  }

//  private def getLitresForSmallProducer(userAnswers: UserAnswers, isCheckAnswers: Boolean)
//                                       (implicit messages: Messages): Seq[SummaryListRow] = {
//    val smallProducerList = userAnswers.smallProducerList
//    if(userAnswers.get(page).getOrElse(false) && smallProducerList.nonEmpty) {
//      val lowBandLitres = smallProducerList.map(_.litreage._1).sum
//      val highBandLitres = smallProducerList.map(_.litreage._2).sum
//      val litresInBands = LitresInBands(lowBandLitres, highBandLitres)
//      summaryLitres.rows(litresInBands, isCheckAnswers)
//    } else {
//      Seq.empty
//    }
//  }

}
