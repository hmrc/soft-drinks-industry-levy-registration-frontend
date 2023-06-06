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

package navigation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import models.HowManyLitresGlobally.{Large, Small}
import models.OrganisationType.Partnership
import pages._
import models._

@Singleton
class Navigator @Inject()() {

  private def navigationForHowManyLitresGloballyCheckMode(userAnswers: UserAnswers, previousAnswer: Option[String]): Call = {
    (previousAnswer, userAnswers.get(page = HowManyLitresGloballyPage)) match {
      case (Some(previousAnswer), Some(newGlobalLitresAnswer)) if previousAnswer == newGlobalLitresAnswer.toString =>
        routes.CheckYourAnswersController.onPageLoad()
      case (_, Some(Large)) =>
        routes.OperatePackagingSitesController.onPageLoad(CheckMode)
      case (_, Some(Small)) =>
        routes.ThirdPartyPackagersController.onPageLoad(CheckMode)
      case (_, Some(HowManyLitresGlobally.None)) =>
        routes.ContractPackingController.onPageLoad(CheckMode)
      case (_, _) =>
        routes.IndexController.onPageLoad()
    }
  }

  private def navigationForHowManyLitresGloballyNormalMode(userAnswers: UserAnswers): Call = {
    userAnswers.get(page = HowManyLitresGloballyPage) match {
      case Some(litres) if litres == Large  =>
        routes.OperatePackagingSitesController.onPageLoad(NormalMode)
      case Some(litres) if litres == Small =>
        routes.ThirdPartyPackagersController.onPageLoad(NormalMode)
      case Some(litres) if litres == HowManyLitresGlobally.None =>
        routes.ContractPackingController.onPageLoad(NormalMode)
      case _ =>
        routes.IndexController.onPageLoad()
    }
  }

  private def navigationForOrganisationType(userAnswers: UserAnswers, mode: Mode): Call = {
    (userAnswers.get(page = OrganisationTypePage),mode) match {
      case (Some(organisationType),_) if organisationType == Partnership =>
        routes.CannotRegisterPartnershipController.onPageLoad()
      case (_, CheckMode) =>
        routes.CheckYourAnswersController.onPageLoad()
      case (_,_) =>
        routes.HowManyLitresGloballyController.onPageLoad(mode)
    }
  }

  private def navigationForContractPacking(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ContractPackingPage).contains(true)) {
      routes.HowManyContractPackingController.onPageLoad(mode)
    } else if(mode == CheckMode){
        routes.CheckYourAnswersController.onPageLoad()
    } else {
        routes.IndexController.onPageLoad()
    }
  }

  private def navigationForImports(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ImportsPage).contains(true)) {
      routes.HowManyImportsController.onPageLoad(mode)
    } else if(mode == CheckMode){
        routes.CheckYourAnswersController.onPageLoad()
    } else {
        routes.IndexController.onPageLoad()
    }
  }

  private def navigationForOperatePackagingSites(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = OperatePackagingSitesPage).contains(true)) {
      routes.HowManyOperatePackagingSitesController.onPageLoad(mode)
    } else if(mode == CheckMode){
        routes.CheckYourAnswersController.onPageLoad()
    } else {
        routes.ContractPackingController.onPageLoad(mode)
    }
  }

  private val normalRoutes: Page => UserAnswers => Call = {
    case ContactDetailsPage => userAnswers => routes.IndexController.onPageLoad()
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, NormalMode)
    case HowManyContractPackingPage => userAnswers => routes.IndexController.onPageLoad()
    case ImportsPage => userAnswers => navigationForImports(userAnswers, NormalMode)
    case HowManyImportsPage => userAnswers => routes.IndexController.onPageLoad()
    case AskSecondaryWarehousesPage => userAnswers => routes.IndexController.onPageLoad()
    case OperatePackagingSitesPage => userAnswers => navigationForOperatePackagingSites(userAnswers, NormalMode)
    case HowManyOperatePackagingSitesPage => userAnswers => routes.ContractPackingController.onPageLoad(NormalMode)
    case ThirdPartyPackagersPage => userAnswers => routes.OperatePackagingSitesController.onPageLoad(NormalMode)
    case PackagingSiteDetailsPage => userAnswers => routes.IndexController.onPageLoad()
    case StartDatePage => userAnswers => routes.IndexController.onPageLoad()
    case OrganisationTypePage => userAnswers => navigationForOrganisationType(userAnswers, NormalMode)
    case HowManyLitresGloballyPage => userAnswers => navigationForHowManyLitresGloballyNormalMode(userAnswers)
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Option[String] => Call = {
    case ContractPackingPage => userAnswers => _ => navigationForContractPacking(userAnswers, CheckMode)
    case OperatePackagingSitesPage => userAnswers => _ =>  navigationForOperatePackagingSites(userAnswers, CheckMode)
    case ImportsPage => userAnswers => _ => navigationForImports(userAnswers, CheckMode)
    case OrganisationTypePage => userAnswers => _ => navigationForOrganisationType(userAnswers, CheckMode)
    case HowManyLitresGloballyPage => userAnswers => previousAnswer => navigationForHowManyLitresGloballyCheckMode(userAnswers, previousAnswer)
    case _ => _ => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, previousAnswer: Option[String] = None): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)(previousAnswer)
  }
}
