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

import pages.{ ContractPackingPage, HowManyContractPackingPage, HowManyImportsPage, HowManyLitresGloballyPage, HowManyOperatePackagingSitesPage, ImportsPage, ThirdPartyPackagersPage }

object UserTypeCheck {
  def isLarge(userAnswers: UserAnswers): Boolean = userAnswers.get(HowManyLitresGloballyPage).contains(HowManyLitresGlobally.Large)
  def isSmall(userAnswers: UserAnswers): Boolean = userAnswers.get(HowManyLitresGloballyPage).contains(HowManyLitresGlobally.Small)
  def notAProducer(userAnswers: UserAnswers): Boolean = userAnswers.get(HowManyLitresGloballyPage).contains(HowManyLitresGlobally.None)
  def operatesPackagingSite(userAnswers: UserAnswers): Boolean = userAnswers.get(HowManyOperatePackagingSitesPage).map(Litreage.fromLitresInBands).nonEmpty
  def copackerAll(userAnswers: UserAnswers): Boolean = userAnswers.get(HowManyContractPackingPage).map(Litreage.fromLitresInBands).nonEmpty
  def copackee(userAnswers: UserAnswers): Boolean = isSmall(userAnswers) && userAnswers.get(ThirdPartyPackagersPage).contains(true)
  def importer(userAnswers: UserAnswers): Boolean = userAnswers.get(HowManyImportsPage).map(Litreage.fromLitresInBands).nonEmpty

  def doesNotNeedToRegister(userAnswers: UserAnswers): Boolean = {
    lazy val hasNoUkActivity = userAnswers.get(ContractPackingPage).contains(false) && userAnswers.get(ImportsPage).contains(false)
    val doesNotNeedToRegisterSmall = isSmall(userAnswers) && userAnswers.get(ThirdPartyPackagersPage).contains(false) && hasNoUkActivity
    val doesNotNeedToRegisterNone = notAProducer(userAnswers) && hasNoUkActivity

    doesNotNeedToRegisterSmall || doesNotNeedToRegisterNone
  }

}
