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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait OrganisationType {
  val enumNum: String
}

object OrganisationType extends Enumerable.Implicits {

  case object LimitedCompany extends WithName("limitedcompany") with OrganisationType {
    override val enumNum: String = "7"
  }
  case object LimitedLiabilityPartnership extends WithName("limitedliabilitypartnership") with OrganisationType {
    override val enumNum: String = "2"
  }
  case object Partnership extends WithName("partnership") with OrganisationType {
    override val enumNum: String = "3"
  }
  case object SoleTrader extends WithName("soletrader") with OrganisationType {
    override val enumNum: String = "1"
  }
  case object UnincorporatedBody extends WithName("unincorporatedbody") with OrganisationType {
    override val enumNum: String = "5"
  }

  val values: Seq[OrganisationType] = Seq(
    LimitedCompany, LimitedLiabilityPartnership, Partnership, UnincorporatedBody)
  val valuesWithST: Seq[OrganisationType] = Seq(
    LimitedCompany, LimitedLiabilityPartnership, Partnership, SoleTrader, UnincorporatedBody)

  def options(withoutSoleTrader: Boolean)(implicit messages: Messages): Seq[RadioItem] = {
    val valuesList = if (withoutSoleTrader) values else valuesWithST
    valuesList.zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = Text(messages(s"organisationType.${value.toString}")),
          value = Some(value.toString),
          id = Some(s"value_$index"))
    }
  }

  implicit val enumerable: Enumerable[OrganisationType] =
    Enumerable(valuesWithST.map(v => v.toString -> v): _*)
}
