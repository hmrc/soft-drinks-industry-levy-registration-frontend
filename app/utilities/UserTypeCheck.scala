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

package utilities

import models.HowManyLitresGlobally.{Large, Small}
import models.{HowManyLitresGlobally, RegisterationDetails, RetrievedSubscription, UserAnswers}
import pages.{HowManyContractPackingPage, HowManyLitresGloballyPage, HowManyOperatePackagingSitesPage}


object UserTypeCheck {

  def isNewPacker(userAnswers: UserAnswers): Boolean = {

    val howManyOperatePackagingSitesPage: (Long, Long)= userAnswers.get(HowManyOperatePackagingSitesPage) match {
      case Some(litres) => (litres.lowBand, litres.highBand)
      case _ => (0L, 0L)
    }

    val howManyContractPackingPage: (Long, Long)= userAnswers.get(HowManyContractPackingPage) match {
      case Some(litres) => (litres.lowBand, litres.highBand)
      case _ => (0L, 0L)
    }

    val registerationDetails =  RegisterationDetails(howManyOperatePackagingSitesPage,
                                howManyContractPackingPage,
                                userAnswers.smallProducerList)

    registerationDetails.totalPacked._1 > 0L && registerationDetails.totalPacked._2 > 0L
  }

  def producerSize(userAnswers: UserAnswers) = {
    userAnswers.get(page = HowManyLitresGloballyPage) match {
      case Some(litres) if litres == Large => Large
      case Some(litres) if litres == Small => Small
      case Some(litres) if litres == HowManyLitresGlobally.None => HowManyLitresGlobally.None
      case _ => None
    }
  }

  def hasPackingSites(userAnswers: UserAnswers):Boolean = {
    userAnswers.packagingSiteList.nonEmpty
  }

  def hasWarehouses(userAnswers: UserAnswers):Boolean = {
    userAnswers.warehouseList.nonEmpty
  }
}
