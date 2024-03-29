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

import base.SpecBase
import controllers.routes
import models.HowManyLitresGlobally.{Large, Small}
import models.OrganisationType.{LimitedCompany, LimitedLiabilityPartnership, Partnership, SoleTrader, UnincorporatedBody}
import models._
import pages._
import play.api.libs.json.Json

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR)) mustBe routes.RegistrationController.start
      }

      "when on organisation type page" - {
        "must navigate to cannot register partnership page and partnership is selected" in {
          val result = navigator.nextPage(OrganisationTypePage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(OrganisationTypePage.toString -> Partnership.toString)))
          result mustBe routes.CannotRegisterPartnershipController.onPageLoad
        }

        "must navigate to how many litres globally page and limited company is selected" in {
          val result = navigator.nextPage(OrganisationTypePage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(OrganisationTypePage.toString -> LimitedCompany.toString)))
          result mustBe routes.HowManyLitresGloballyController.onPageLoad(NormalMode)
        }

        "must navigate to how many litres globally page and limited liability partnership is selected" in {
          val result = navigator.nextPage(OrganisationTypePage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(OrganisationTypePage.toString -> LimitedLiabilityPartnership.toString)))
          result mustBe routes.HowManyLitresGloballyController.onPageLoad(NormalMode)
        }

        "must navigate to how many litres globally page and unincorporated body is selected" in {
          val result = navigator.nextPage(OrganisationTypePage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(OrganisationTypePage.toString -> UnincorporatedBody.toString)))
          result mustBe routes.HowManyLitresGloballyController.onPageLoad(NormalMode)
        }

        "must navigate to how many litres globally page and sole trader is selected" in {
          val result = navigator.nextPage(OrganisationTypePage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(OrganisationTypePage.toString -> SoleTrader.toString)))
          result mustBe routes.HowManyLitresGloballyController.onPageLoad(NormalMode)
        }

      }

      "when on how many litres globally page" - {
        "must navigate to third party packagers page when less than 1 million litres is selected" in {
          val result = navigator.nextPage(HowManyLitresGloballyPage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(HowManyLitresGloballyPage.toString -> Large.toString)))
          result mustBe routes.OperatePackagingSitesController.onPageLoad(NormalMode)
        }

        "must navigate to third party packagers page when less than less than 1 million litres is selected" in {
          val result = navigator.nextPage(HowManyLitresGloballyPage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(HowManyLitresGloballyPage.toString -> Small.toString)))
          result mustBe routes.ThirdPartyPackagersController.onPageLoad(NormalMode)
        }

        "must navigate to third party packagers page when None is selected" in {
          val result = navigator.nextPage(HowManyLitresGloballyPage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(HowManyLitresGloballyPage.toString -> HowManyLitresGlobally.None.toString)))
          result mustBe routes.ContractPackingController.onPageLoad(NormalMode)
        }

        "must navigate to start page when no answer available in user answers" in {
          val result = navigator.nextPage(HowManyLitresGloballyPage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj()))
          result mustBe routes.RegistrationController.start
        }

      }

      "when on how many third party packagers page" - {
        "must navigate to operate packaging sites page when yes is selected" in {
          val result = navigator.nextPage(ThirdPartyPackagersPage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(ThirdPartyPackagersPage.toString -> true)))
          result mustBe routes.OperatePackagingSitesController.onPageLoad(NormalMode)
        }

        "must navigate to operate packaging sites page when no is selected" in {
          val result = navigator.nextPage(ThirdPartyPackagersPage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(ThirdPartyPackagersPage.toString -> false)))
          result mustBe routes.OperatePackagingSitesController.onPageLoad(NormalMode)
        }
      }

      "when on operate packaging sites page" - {
        "must navigate to how many litres will be packaged in next 12 months when yes is selected" in {
          val result = navigator.nextPage(OperatePackagingSitesPage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(OperatePackagingSitesPage.toString -> true)))
          result mustBe routes.HowManyOperatePackagingSitesController.onPageLoad(NormalMode)
        }

        "must navigate to third party or co packing when no is selected" in {
          val result = navigator.nextPage(OperatePackagingSitesPage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(OperatePackagingSitesPage.toString -> false)))
          result mustBe routes.ContractPackingController.onPageLoad(NormalMode)
        }
      }

      "when on operate how many own brands in the next 12 months page" - {

        "must navigate to third party of co-packing in case of valid litreage" in {
          val result = navigator.nextPage(HowManyOperatePackagingSitesPage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(HowManyOperatePackagingSitesPage.toString -> Json.obj("lowBand" -> "123", "highBand" -> "123"))))
          result mustBe routes.ContractPackingController.onPageLoad(NormalMode)
        }
      }

      "when on contract packing page" - {
        "must navigate to how many litres contract packing in next 12 months when yes is selected" in {
          val result = navigator.nextPage(ContractPackingPage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(ContractPackingPage.toString -> true)))
          result mustBe routes.HowManyContractPackingController.onPageLoad(NormalMode)
        }

        "must navigate to imports when no is selected" in {
          val result = navigator.nextPage(ContractPackingPage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(ContractPackingPage.toString -> false)))
          result mustBe routes.ImportsController.onPageLoad(NormalMode)
        }
      }

      "when on contract packer in the next 12 months page" - {
        "must navigate to imports page in case of valid litreage" in {
          val result = navigator.nextPage(HowManyContractPackingPage, NormalMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(HowManyContractPackingPage.toString -> Json.obj("lowBand" -> "123", "highBand" -> "123"))))
          result mustBe routes.ImportsController.onPageLoad(NormalMode)
        }
      }

      "when on start date page as a large producer" - {

        "must navigate to Ask secondary warehouses page if user has answered " +
          "no on the operate packaging sites page, no on the contract-packing page, and no on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "large",
            "operatePackagingSites" -> false, "contractPacking" -> false, "imports" -> false)))
          result mustBe routes.AskSecondaryWarehousesController.onPageLoad(NormalMode)
        }

        "must navigate to Ask secondary warehouses page if user has answered " +
          "no on the operate packaging sites page, no on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "large",
            "operatePackagingSites" -> false, "contractPacking" -> false, "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.AskSecondaryWarehousesController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "no on the operate packaging sites page, yes on the contract-packing page, and no on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "large",
            "operatePackagingSites" -> false, "contractPacking" -> true, "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "imports" -> false)))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "no on the operate packaging sites page, yes on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "large",
            "operatePackagingSites" -> false, "contractPacking" -> true, "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "yes on the operate packaging sites page, no on the contract-packing page, and no on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "large",
            "operatePackagingSites" -> true, "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "contractPacking" -> false, "imports" -> false)))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "yes on the operate packaging sites page, no on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "large",
            "operatePackagingSites" -> true, "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "contractPacking" -> false, "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "yes on the operate packaging sites page, yes on the contract-packing page, and no on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "large",
            "operatePackagingSites" -> true, "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "contractPacking" -> true, "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "imports" -> false)))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "yes on the operate packaging sites page, yes on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "large",
            "operatePackagingSites" -> true, "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "contractPacking" -> true, "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }
      }

      "when on start date page as a small producer" - {

        "must navigate to Ask secondary warehouses page if user has answered " +
          "no on the third party packaging page, no on the operate packaging sites page, no on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> false, "contractPacking" -> false, "imports" -> true,
            "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.AskSecondaryWarehousesController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "no on the third party packaging page, no on the operate packaging sites page, yes on the contract-packing page, and no on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> false, "contractPacking" -> true,
            "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1), "imports" -> false)))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "no on the third party packaging page, no on the operate packaging sites page, yes on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> false, "contractPacking" -> true,
            "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1), "imports" -> true,
            "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }

        "must navigate to Ask secondary warehouses page if user has answered " +
          "no on the third party packaging page, yes on the operate packaging sites page, no on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> true, "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "contractPacking" -> false, "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.AskSecondaryWarehousesController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "no on the third party packaging page, yes on the operate packaging sites page, yes on the contract-packing page, and no on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> true, "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "contractPacking" -> true, "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1), "imports" -> false)))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "no on the third party packaging page, yes on the operate packaging sites page, yes on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> true, "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "contractPacking" -> true, "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }

        "must navigate to Ask secondary warehouses page if user has answered " +
          "yes on the third party packaging page, no on the operate packaging sites page, no on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> true, "operatePackagingSites" -> false, "contractPacking" -> false,
            "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.AskSecondaryWarehousesController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "yes on the third party packaging page, no on the operate packaging sites page, yes on the contract-packing page, and no on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> true, "operatePackagingSites" -> false, "contractPacking" -> true,
            "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1), "imports" -> false)))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "yes on the third party packaging page, no on the operate packaging sites page, yes on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> true, "operatePackagingSites" -> false, "contractPacking" -> true,
            "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }

        "must navigate to Ask secondary warehouses page if user has answered " +
          "yes on the third party packaging page, yes on the operate packaging sites page, no on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> true, "operatePackagingSites" -> true, "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "contractPacking" -> false, "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.AskSecondaryWarehousesController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "yes on the third party packaging page, yes on the operate packaging sites page, yes on the contract-packing page, and no on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> true, "operatePackagingSites" -> true, "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "contractPacking" -> true, "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1), "imports" -> false)))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "yes on the third party packaging page, yes on the operate packaging sites page, yes on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> true, "operatePackagingSites" -> true, "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "contractPacking" -> true, "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }
      }

      "when on start date page as a non producer" - {

        "must navigate to Ask secondary warehouses page if user has answered " +
          "no on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "contractPacking" -> false, "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.AskSecondaryWarehousesController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "yes on the contract-packing page, and no on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "small",
            "contractPacking" -> true, "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1), "imports" -> false)))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }

        "must navigate to Pack at business address page if user has answered " +
          "yes on the contract-packing page, and yes on the imports page" in {
          val result = navigator.nextPage(StartDatePage, NormalMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "small",
            "contractPacking" -> true, "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.PackAtBusinessAddressController.onPageLoad(NormalMode)
        }
      }

      s"when on $ImportsPage when user has answered yes " - {
        s" must navigate to the $HowManyImportsPage page " in {
          val result = navigator.nextPage(ImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "contractPacking" -> false, "imports" -> true)))
          result mustBe routes.HowManyImportsController.onPageLoad(NormalMode)
        }
      }

      s"when on $ImportsPage when user has answered no " - {
        s"as a large producer, must navigate to the $StartDatePage" in {
          val result = navigator.nextPage(ImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "large",
            "contractPacking" -> false, "imports" -> false)))
          result mustBe routes.StartDateController.onPageLoad(NormalMode)
        }

        s"as a non-producer, if user selected yes on $ContractPackingPage, must navigate to the $StartDatePage" in {
          val result = navigator.nextPage(ImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "xnot",
            "contractPacking" -> true, "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1), "imports" -> false)))
          result mustBe routes.StartDateController.onPageLoad(NormalMode)
        }

        s"as a non-producer, if user selected no on $ContractPackingPage, must navigate to the DoNotRegister controller" in {
          val result = navigator.nextPage(ImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "xnot",
            "contractPacking" -> false, "imports" -> false)))
          result mustBe routes.DoNotRegisterController.onPageLoad
        }

        s"as a small producer, if user selected yes on $ContractPackingPage, must navigate to the $StartDatePage" in {
          val result = navigator.nextPage(ImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> false, "contractPacking" -> true, "howManyContractPacking" ->
              Json.obj("lowBand" -> 1, "highBand" -> 1), "imports" -> false)))
          result mustBe routes.StartDateController.onPageLoad(NormalMode)
        }

        s"as a small producer, if user selected no on the $ThirdPartyPackagersPage, no on the $OperatePackagingSitesPage, " +
          s"and no on $ContractPackingPage must navigate to the DoNotRegisterController" in {
          val result = navigator.nextPage(ImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> false, "contractPacking" -> false, "imports" -> false)))
          result mustBe routes.DoNotRegisterController.onPageLoad
        }

        s"as a small producer, if user selected no on the $ThirdPartyPackagersPage, yes on the $OperatePackagingSitesPage, " +
          s"and no on $ContractPackingPage must navigate to the DoNotRegisterController" in {
          val result = navigator.nextPage(ImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> true, "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "contractPacking" -> false, "imports" -> false)))
          result mustBe routes.DoNotRegisterController.onPageLoad
        }

        s"as a small producer, if user selected yes on the $ThirdPartyPackagersPage, no on the $OperatePackagingSitesPage, " +
          s"and no on $ContractPackingPage must navigate to the $ContactDetailsPage" in {
          val result = navigator.nextPage(ImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> true, "operatePackagingSites" -> false, "contractPacking" -> false, "imports" -> false)))
          result mustBe routes.ContactDetailsController.onPageLoad(NormalMode)
        }
      }

      s"when on $HowManyImportsPage when the user enters valid litres and clicks Save and continue " - {
        s"as a large producer, must navigate to the $StartDatePage" in {
          val result = navigator.nextPage(HowManyImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "large",
            "contractPacking" -> false, "imports" -> false, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.StartDateController.onPageLoad(NormalMode)
        }

        s"as a non-producer, if user selected yes on $ContractPackingPage, must navigate to the $StartDatePage" in {
          val result = navigator.nextPage(HowManyImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "xnot",
            "contractPacking" -> true, "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.StartDateController.onPageLoad(NormalMode)
        }

        s"as a small producer, if user selected yes on $ContractPackingPage, must navigate to the $StartDatePage" in {
          val result = navigator.nextPage(HowManyImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> false, "contractPacking" -> true, "howManyContractPacking" ->
              Json.obj("lowBand" -> 1, "highBand" -> 1), "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.StartDateController.onPageLoad(NormalMode)
        }

        s"as a small producer, if user selected no on the $ThirdPartyPackagersPage, no on the $OperatePackagingSitesPage, " +
          s"and no on $ContractPackingPage must navigate to the $StartDatePage" in {
          val result = navigator.nextPage(HowManyImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> false, "contractPacking" -> false, "imports" -> true,
            "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.StartDateController.onPageLoad(NormalMode)
        }

        s"as a small producer, if user selected no on the $ThirdPartyPackagersPage, yes on the $OperatePackagingSitesPage, " +
          s"and no on $ContractPackingPage must navigate to the $StartDatePage" in {
          val result = navigator.nextPage(HowManyImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> true, "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1, "highBand" -> 1),
            "contractPacking" -> false, "imports" -> true, "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.StartDateController.onPageLoad(NormalMode)
        }

        s"as a small producer, if user selected yes on the $ThirdPartyPackagersPage, no on the $OperatePackagingSitesPage, " +
          s"and no on $ContractPackingPage must navigate to the $StartDatePage" in {
          val result = navigator.nextPage(HowManyImportsPage, NormalMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> true, "operatePackagingSites" -> false, "contractPacking" -> false, "imports" -> true,
            "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.StartDateController.onPageLoad(NormalMode)
        }
      }

    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id", RegisterState.RegisterWithAuthUTR)) mustBe routes.CheckYourAnswersController.onPageLoad
      }

      "when on organisation type page" - {
        "must navigate to cannot register partnership page in check mode" in {
          val result = navigator.nextPage(OrganisationTypePage, CheckMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(OrganisationTypePage.toString -> Partnership.toString)))
          result mustBe routes.CannotRegisterPartnershipController.onPageLoad
        }

        "must navigate to check your answers page in check mode" in {
          val result = navigator.nextPage(OrganisationTypePage, CheckMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(OrganisationTypePage.toString -> LimitedCompany.toString)))
          result mustBe routes.CheckYourAnswersController.onPageLoad
        }
      }

      "when on how many litres globally page" - {
        "in checkmode" - {
          HowManyLitresGlobally.values.foreach { previousValue =>
            HowManyLitresGlobally.values.foreach { newValue =>
              val userAnswers = UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(HowManyLitresGloballyPage.toString -> newValue.toString))
              if (newValue != previousValue) {
                val expectedUrl = newValue match {
                  case Large => routes.OperatePackagingSitesController.onPageLoad(NormalMode)
                  case Small => routes.ThirdPartyPackagersController.onPageLoad(NormalMode)
                  case _ => routes.ContractPackingController.onPageLoad(NormalMode)
                }
                s"must redirect to the ${expectedUrl.url} when the useranswers amount produced is $newValue" - {
                  s"and the previous value is $previousValue" in {
                    val result = navigator.nextPage(HowManyLitresGloballyPage, CheckMode, userAnswers, Some(previousValue.toString))

                    result mustBe expectedUrl
                  }
                }
              } else {
                "must redirect to check your answers" - {
                  s"if the updated answers and the previous answer is $newValue" in {
                    val result = navigator.nextPage(HowManyLitresGloballyPage, CheckMode, userAnswers, Some(previousValue.toString))
                    result mustBe routes.CheckYourAnswersController.onPageLoad
                  }
                }
              }
            }
          }
        }
      }

      "when on how many third party packagers page" - {
        "must navigate to operate packaging sites page when yes is selected" in {
          val result = navigator.nextPage(ThirdPartyPackagersPage, CheckMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(ThirdPartyPackagersPage.toString -> true)))
          result mustBe routes.CheckYourAnswersController.onPageLoad
        }

        "must navigate to operate packaging sites page when no is selected" in {
          val result = navigator.nextPage(ThirdPartyPackagersPage, CheckMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(ThirdPartyPackagersPage.toString -> false)))
          result mustBe routes.CheckYourAnswersController.onPageLoad
        }
      }

      "when on operate packaging sites page" - {
        "must navigate to how many litres will be packaged in next 12 months when yes is selected" in {
          val result = navigator.nextPage(OperatePackagingSitesPage, CheckMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(OperatePackagingSitesPage.toString -> true)))
          result mustBe routes.HowManyOperatePackagingSitesController.onPageLoad(CheckMode)
        }

        "must navigate to check your answers when no is selected" in {
          val result = navigator.nextPage(OperatePackagingSitesPage, CheckMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(OperatePackagingSitesPage.toString -> false)))
          result mustBe routes.CheckYourAnswersController.onPageLoad
        }
      }

      "when on operate how many own brands in the next 12 months page" - {
        "must navigate to check your answers controller in case of valid litreage" in {
          val result = navigator.nextPage(HowManyOperatePackagingSitesPage, CheckMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(HowManyOperatePackagingSitesPage.toString -> Json.obj("lowBand" -> "123", "highBand" -> "123"))))
          result mustBe routes.CheckYourAnswersController.onPageLoad
        }
      }

      "when on contract packing sites page" - {
        "must navigate to how many litres will be packaged in next 12 months when yes is selected" in {
          val result = navigator.nextPage(ContractPackingPage, CheckMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(ContractPackingPage.toString -> true)))
          result mustBe routes.HowManyContractPackingController.onPageLoad(CheckMode)
        }

        "must navigate to check your answers when no is selected" in {
          val result = navigator.nextPage(ContractPackingPage, CheckMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(ContractPackingPage.toString -> false)))
          result mustBe routes.CheckYourAnswersController.onPageLoad
        }
      }

      "when on contract packing how many litres in the next 12 months page" - {
        "must navigate to check your answers controller in case of valid litreage" in {
          val result = navigator.nextPage(HowManyContractPackingPage, CheckMode,
            UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj(HowManyContractPackingPage.toString -> Json.obj("lowBand" -> "123", "highBand" -> "123"))))
          result mustBe routes.CheckYourAnswersController.onPageLoad
        }
      }

      "when on start date page in check mode" - {
        "must navigate to check your answers" in {
          val result = navigator.nextPage(StartDatePage, CheckMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> false, "contractPacking" -> false, "imports" -> true,
            "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1))))
          result mustBe routes.CheckYourAnswersController.onPageLoad
        }
      }

      s"when on $ImportsPage in check mode" - {
        "must navigate to the correct page as it does in normal mode" in {
          val result = navigator.nextPage(ImportsPage, CheckMode, emptyUserAnswers.copy(data = Json.obj("howManyLitresGlobally" -> "small",
            "thirdPartyPackagers" -> false, "operatePackagingSites" -> false, "contractPacking" -> false, "imports" -> true)))
          result mustBe routes.HowManyImportsController.onPageLoad(CheckMode)
        }
      }
    }
  }
}
