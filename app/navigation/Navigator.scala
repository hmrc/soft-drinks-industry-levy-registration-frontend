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

import controllers.routes
import models.HowManyLitresGlobally.{Large, Small}
import models.OrganisationType.Partnership
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject()() {

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, previousAnswer: Option[String] = None): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)(previousAnswer)
  }

  private val normalRoutes: Page => UserAnswers => Call = {
    case VerifyPage => _ => routes.OrganisationTypeController.onPageLoad(NormalMode)
    case RemovePackagingSiteDetailsPage => _ => routes.PackagingSiteDetailsController.onPageLoad(NormalMode)
    case EnterBusinessDetailsPage => _ => routes.VerifyController.onPageLoad(NormalMode)
    case WarehouseDetailsPage => _ => routes.IndexController.onPageLoad()
    case RemoveWarehouseDetailsPage => _ => routes.WarehouseDetailsController.onPageLoad(NormalMode)
    case ContactDetailsPage => _ => routes.CheckYourAnswersController.onPageLoad()
    case PackAtBusinessAddressPage => _ => routes.IndexController.onPageLoad()
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, NormalMode)
    case HowManyContractPackingPage => _ => routes.ImportsController.onPageLoad(NormalMode)
    case ImportsPage => userAnswers => navigationForImports(userAnswers, NormalMode)
    case HowManyImportsPage => _ => routes.StartDateController.onPageLoad(NormalMode)
    case OperatePackagingSitesPage => userAnswers => navigationForOperatePackagingSites(userAnswers, NormalMode)
    case HowManyOperatePackagingSitesPage => _ => routes.ContractPackingController.onPageLoad(NormalMode)
    case ThirdPartyPackagersPage => _ => routes.OperatePackagingSitesController.onPageLoad(NormalMode)
    case PackagingSiteDetailsPage => _ => routes.IndexController.onPageLoad()
    case StartDatePage => userAnswers => navigationForStartDate(userAnswers, NormalMode)
    case OrganisationTypePage => userAnswers => navigationForOrganisationType(userAnswers, NormalMode)
    case HowManyLitresGloballyPage => userAnswers => navigationForHowManyLitresGloballyNormalMode(userAnswers)
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Option[String] => Call = {
    case EnterBusinessDetailsPage => _ =>_ => routes.VerifyController.onPageLoad(NormalMode)
    case RemoveWarehouseDetailsPage => _ => _ => routes.WarehouseDetailsController.onPageLoad(CheckMode)
    case StartDatePage => userAnswers => _ => navigationForStartDate(userAnswers, CheckMode)
    case ContractPackingPage => userAnswers => _ => navigationForContractPacking(userAnswers, CheckMode)
    case OperatePackagingSitesPage => userAnswers => _ =>  navigationForOperatePackagingSites(userAnswers, CheckMode)
    case ImportsPage => userAnswers => _ => navigationForImports(userAnswers, CheckMode)
    case HowManyImportsPage => _ => _ => routes.CheckYourAnswersController.onPageLoad()
    case OrganisationTypePage => userAnswers => _ => navigationForOrganisationType(userAnswers, CheckMode)
    case HowManyLitresGloballyPage => userAnswers => previousAnswer => navigationForHowManyLitresGloballyCheckMode(userAnswers, previousAnswer)
    case _ => _ => _ => routes.CheckYourAnswersController.onPageLoad()
  }

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
      case Some(litres) if litres == Large =>
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
    (userAnswers.get(page = OrganisationTypePage), mode) match {
      case (Some(organisationType), _) if organisationType == Partnership =>
        routes.CannotRegisterPartnershipController.onPageLoad()
      case (_, CheckMode) =>
        routes.CheckYourAnswersController.onPageLoad()
      case (_, _) =>
        routes.HowManyLitresGloballyController.onPageLoad(mode)
    }
  }

  private def navigationForContractPacking(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ContractPackingPage).contains(true)) {
      routes.HowManyContractPackingController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CheckYourAnswersController.onPageLoad()
    } else {
      routes.ImportsController.onPageLoad(mode)
    }
  }

  private def navigationForImports(userAnswers: UserAnswers, mode: Mode): Call = {
    userAnswers.get(page = ImportsPage).contains(true) match {
      case true => routes.HowManyImportsController.onPageLoad(mode)
      case false if mode == NormalMode => routes.StartDateController.onPageLoad(mode)
      case _ => routes.CheckYourAnswersController.onPageLoad()
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

  private def navigationForStartDate(userAnswers: UserAnswers, mode: Mode): Call = {
    if(userAnswers.get(page = StartDatePage).isDefined && mode == NormalMode) {
      routes.PackAtBusinessAddressController.onPageLoad(mode)
    } else {
      routes.CheckYourAnswersController.onPageLoad()
    }
  }

}
