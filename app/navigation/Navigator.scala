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
    case RemoveWarehouseDetailsPage => _ => routes.WarehouseDetailsController.onPageLoad(NormalMode)
    case ContactDetailsPage => _ => routes.CheckYourAnswersController.onPageLoad
    case ContractPackingPage => userAnswers => navigationForContractPacking(userAnswers, NormalMode)
    case HowManyContractPackingPage => _ => routes.ImportsController.onPageLoad(NormalMode)
    case ImportsPage => userAnswers => navigationForImports(userAnswers, NormalMode)
    case HowManyImportsPage => userAnswers => navigationForHowManyImports(userAnswers, NormalMode)
    case OperatePackagingSitesPage => userAnswers => navigationForOperatePackagingSites(userAnswers, NormalMode)
    case HowManyOperatePackagingSitesPage => _ => routes.ContractPackingController.onPageLoad(NormalMode)
    case ThirdPartyPackagersPage => _ => routes.OperatePackagingSitesController.onPageLoad(NormalMode)
    case StartDatePage => userAnswers => navigationForStartDate(userAnswers, NormalMode)
    case OrganisationTypePage => userAnswers => navigationForOrganisationType(userAnswers, NormalMode)
    case HowManyLitresGloballyPage => userAnswers => navigationForHowManyLitresGloballyNormalMode(userAnswers)
    case _ => _ => routes.VerifyController.onPageLoad(NormalMode)
  }

  private val checkRouteMap: Page => UserAnswers => Option[String] => Call = {
    case EnterBusinessDetailsPage => _ =>_ => routes.VerifyController.onPageLoad(NormalMode)
    case RemoveWarehouseDetailsPage => _ => _ => routes.WarehouseDetailsController.onPageLoad(CheckMode)
    case StartDatePage => userAnswers => _ => navigationForStartDate(userAnswers, CheckMode)
    case ContractPackingPage => userAnswers => _ => navigationForContractPacking(userAnswers, CheckMode)
    case OperatePackagingSitesPage => userAnswers => _ =>  navigationForOperatePackagingSites(userAnswers, CheckMode)
    case ImportsPage => userAnswers => _ => navigationForImports(userAnswers, CheckMode)
    case HowManyImportsPage =>userAnswers => _ => navigationForHowManyImports(userAnswers, CheckMode)
    case OrganisationTypePage => userAnswers => _ => navigationForOrganisationType(userAnswers, CheckMode)
    case HowManyLitresGloballyPage => userAnswers => previousAnswer => navigationForHowManyLitresGloballyCheckMode(userAnswers, previousAnswer)
    case _ => _ => _ => routes.CheckYourAnswersController.onPageLoad
  }

  private def navigationForHowManyLitresGloballyCheckMode(userAnswers: UserAnswers, previousAnswer: Option[String]): Call = {
    (previousAnswer, userAnswers.get(page = HowManyLitresGloballyPage)) match {
      case (Some(previousAnswer), Some(newGlobalLitresAnswer)) if previousAnswer == newGlobalLitresAnswer.toString =>
        routes.CheckYourAnswersController.onPageLoad
      case (_, Some(Large)) =>
        routes.OperatePackagingSitesController.onPageLoad(CheckMode)
      case (_, Some(Small)) =>
        routes.ThirdPartyPackagersController.onPageLoad(CheckMode)
      case (_, Some(HowManyLitresGlobally.None)) =>
        routes.ContractPackingController.onPageLoad(CheckMode)
      case (_, _) =>
        routes.HowManyLitresGloballyController.onPageLoad(CheckMode)
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
        routes.HowManyLitresGloballyController.onPageLoad(NormalMode)
    }
  }

  private def navigationForOrganisationType(userAnswers: UserAnswers, mode: Mode): Call = {
    (userAnswers.get(page = OrganisationTypePage), mode) match {
      case (Some(organisationType), _) if organisationType == Partnership =>
        routes.CannotRegisterPartnershipController.onPageLoad
      case (_, CheckMode) =>
        routes.CheckYourAnswersController.onPageLoad
      case (_, _) =>
        routes.HowManyLitresGloballyController.onPageLoad(mode)
    }
  }

  private def navigationForContractPacking(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = ContractPackingPage).contains(true)) {
      routes.HowManyContractPackingController.onPageLoad(mode)
    } else if (mode == CheckMode) {
      routes.CheckYourAnswersController.onPageLoad
    } else {
      routes.ImportsController.onPageLoad(mode)
    }
  }

  private def navigationForImports(userAnswers: UserAnswers, mode: Mode): Call = {
    userAnswers.get(page = ImportsPage).contains(true) match {
      case true => routes.HowManyImportsController.onPageLoad(mode)
      case false if navigateToStartDate(userAnswers) => routes.StartDateController.onPageLoad(mode)
      case false => contactDetailsOrDoNotRegister(userAnswers, mode)
    }
  }

  private def navigationForHowManyImports(userAnswers: UserAnswers, mode: Mode): Call = {
    if (navigateToStartDate(userAnswers)) {
      routes.StartDateController.onPageLoad(mode)
    } else {
      contactDetailsOrDoNotRegister(userAnswers, mode)
    }
  }

  private def navigationForOperatePackagingSites(userAnswers: UserAnswers, mode: Mode): Call = {
    if (userAnswers.get(page = OperatePackagingSitesPage).contains(true)) {
      routes.HowManyOperatePackagingSitesController.onPageLoad(mode)
    } else if(mode == CheckMode){
      routes.CheckYourAnswersController.onPageLoad
    } else {
      routes.ContractPackingController.onPageLoad(mode)
    }
  }

  private def navigationForStartDate(userAnswers: UserAnswers, mode: Mode): Call = {
    if (isLarge(userAnswers)) {
      largeProducerToPackagingSiteOrWarehouse(userAnswers, mode)
    } else {
      notALargeProducerToPackagingSiteOrWarehouse(userAnswers, mode)
    }
  }

  private def isSmall(userAnswers: UserAnswers): Boolean = UserTypeCheck.isSmall(userAnswers)
  private def isLarge(userAnswers: UserAnswers): Boolean = UserTypeCheck.isLarge(userAnswers)
  private def notAProducer(userAnswers: UserAnswers): Boolean = UserTypeCheck.notAProducer(userAnswers)
  private def copackerAll(userAnswers: UserAnswers): Boolean = UserTypeCheck.copackerAll(userAnswers)
  private def copackee(userAnswers: UserAnswers): Boolean = UserTypeCheck.copackee(userAnswers)
  private def importer(userAnswers: UserAnswers): Boolean = UserTypeCheck.importer(userAnswers)
  private def operatesPackagingSite(userAnswers: UserAnswers): Boolean = UserTypeCheck.operatesPackagingSite(userAnswers)

  private def largeProducerToPackagingSiteOrWarehouse(userAnswers: UserAnswers, mode: Mode): Call = {
    if (!operatesPackagingSite(userAnswers) && (!copackerAll(userAnswers))) {
      routes.AskSecondaryWarehousesController.onPageLoad(mode)
    } else {
      routes.PackAtBusinessAddressController.onPageLoad(mode)
    }
  }

  private def notALargeProducerToPackagingSiteOrWarehouse(userAnswers: UserAnswers, mode: Mode): Call = {
    if ((isSmall(userAnswers) || notAProducer(userAnswers)) && copackerAll(userAnswers)) {
      routes.PackAtBusinessAddressController.onPageLoad(mode)
    } else {
      routes.AskSecondaryWarehousesController.onPageLoad(mode)
    }
  }

  private def navigateToStartDate(userAnswers: UserAnswers): Boolean = {
    if (isLarge(userAnswers)) {
      true
    } else if (notAProducer(userAnswers) && (!notACopackerOrImporter(userAnswers))) {
      true
    } else if (isSmall(userAnswers)) {
      if (importer(userAnswers) || copackerAll(userAnswers)) {
        true
      } else {
        false
      }
    } else {
      false
    }
  }

  private def notACopackerOrImporter(userAnswers: UserAnswers): Boolean = {
    if (!copackerAll(userAnswers) && !importer(userAnswers)) true else false
  }

  private def contactDetailsOrDoNotRegister(userAnswers: UserAnswers, mode: Mode): Call = {
    if (notAProducer(userAnswers)) {
      routes.DoNotRegisterController.onPageLoad
    } else if (copackee(userAnswers)) {
      routes.ContactDetailsController.onPageLoad(mode)
    } else {
      routes.DoNotRegisterController.onPageLoad
    }
  }

}
