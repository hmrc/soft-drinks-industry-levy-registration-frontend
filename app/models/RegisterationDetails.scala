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

import cats.implicits._
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Format, JsPath, Json, OFormat}
import pages.{HowManyContractPackingPage, HowManyOperatePackagingSitesPage}

import java.time.Instant


case class RegisterationDetails(
                       packLarge: (Long, Long),
                       packLarge2: (Long, Long),
                       packSmall: List[SmallProducer]
                     ) {
  def totalPacked: (Long, Long) = packLarge |+| packLarge2 |+| packSmall.total //|+| packLarge2

  implicit class SmallProducerDetails(smallProducers: List[SmallProducer]) {
    def total: (Long, Long) = smallProducers.map(x => x.litreage).combineAll
  }
}

object RegisterationDetails {

  implicit val smallProducerJson: OFormat[SmallProducer] = Json.format[SmallProducer]
  implicit val registerationDetailsFormat = Json.format[RegisterationDetails]



  def apply(userAnswers: UserAnswers): RegisterationDetails = {
    val lowPackLarge = userAnswers.get(HowManyOperatePackagingSitesPage).map(_.lowBand).getOrElse(0L)
    val highPackLarge = userAnswers.get(HowManyOperatePackagingSitesPage).map(_.highBand).getOrElse(0L)
    val lowPackLarge2 = userAnswers.get(HowManyContractPackingPage).map(_.lowBand).getOrElse(0L)
    val highPackLarge2 = userAnswers.get(HowManyContractPackingPage).map(_.highBand).getOrElse(0L)
    val packSmall = userAnswers.smallProducerList
    RegisterationDetails(
      packLarge = (lowPackLarge, highPackLarge),
      packLarge2 = (lowPackLarge2, highPackLarge2),
      packSmall = packSmall,
    )
  }

}