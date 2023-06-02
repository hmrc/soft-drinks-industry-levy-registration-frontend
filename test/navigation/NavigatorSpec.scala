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
import pages._
import models._
import play.api.libs.json.Json

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

      "when on organisation type page" - {
        "must navigate to cannot register partnership page and partnership is selected" in {
          val result = navigator.nextPage(OrganisationTypePage, NormalMode,
            UserAnswers("id", Json.obj(OrganisationTypePage.toString -> Partnership.toString)))
          result mustBe routes.CannotRegisterPartnershipController.onPageLoad
        }

        "must navigate to how many litres globally page and limited company is selected" in {
          val result = navigator.nextPage(OrganisationTypePage, NormalMode,
            UserAnswers("id", Json.obj(OrganisationTypePage.toString -> LimitedCompany.toString)))
          result mustBe routes.HowManyLitresGloballyController.onPageLoad(NormalMode)
        }

        "must navigate to how many litres globally page and limited liability partnership is selected" in {
          val result = navigator.nextPage(OrganisationTypePage, NormalMode,
            UserAnswers("id", Json.obj(OrganisationTypePage.toString -> LimitedLiabilityPartnership.toString)))
          result mustBe routes.HowManyLitresGloballyController.onPageLoad(NormalMode)
        }

        "must navigate to how many litres globally page and unincorporated body is selected" in {
          val result = navigator.nextPage(OrganisationTypePage, NormalMode,
            UserAnswers("id", Json.obj(OrganisationTypePage.toString -> UnincorporatedBody.toString)))
          result mustBe routes.HowManyLitresGloballyController.onPageLoad(NormalMode)
        }

        "must navigate to how many litres globally page and sole trader is selected" in {
          val result = navigator.nextPage(OrganisationTypePage, NormalMode,
            UserAnswers("id", Json.obj(OrganisationTypePage.toString -> SoleTrader.toString)))
          result mustBe routes.HowManyLitresGloballyController.onPageLoad(NormalMode)
        }

      }

      "when on how many litres globally page" - {

        "must navigate to third party packagers page when less than 1 million litres is selected" in {
          val result = navigator.nextPage(HowManyLitresGloballyPage, NormalMode,
            UserAnswers("id", Json.obj(HowManyLitresGloballyPage.toString -> Large.toString)))
          result mustBe routes.OperatePackagingSitesController.onPageLoad(NormalMode)
        }

        "must navigate to third party packagers page when less than less than 1 million litres is selected" in {
          val result = navigator.nextPage(HowManyLitresGloballyPage, NormalMode,
            UserAnswers("id", Json.obj(HowManyLitresGloballyPage.toString -> Small.toString)))
          result mustBe routes.ThirdPartyPackagersController.onPageLoad(NormalMode)
        }

        "must navigate to third party packagers page when None is selected" in {
          val result = navigator.nextPage(HowManyLitresGloballyPage, NormalMode,
            UserAnswers("id", Json.obj(HowManyLitresGloballyPage.toString -> HowManyLitresGlobally.None.toString)))
          result mustBe routes.ContractPackingController.onPageLoad(NormalMode)
        }

        "must navigate to index controller page when no answer available in user answers" in {
          val result = navigator.nextPage(HowManyLitresGloballyPage, NormalMode,
            UserAnswers("id", Json.obj()))
          result mustBe routes.IndexController.onPageLoad()
        }

      }

    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad
      }

      "when on organisation type page" - {

        "must navigate to cannot register partnership page in check mode" in {
          val result = navigator.nextPage(OrganisationTypePage, CheckMode,
            UserAnswers("id", Json.obj(OrganisationTypePage.toString -> Partnership.toString)))
          result mustBe routes.CannotRegisterPartnershipController.onPageLoad
        }

        "must navigate to check your answers page in check mode" in {
          val result = navigator.nextPage(OrganisationTypePage, CheckMode,
            UserAnswers("id", Json.obj(OrganisationTypePage.toString -> LimitedCompany.toString)))
          result mustBe routes.CheckYourAnswersController.onPageLoad
        }
      }

      "when on how many litres globally page" - {

        "must navigate to third party packagers page when less than 1 million litres is selected in check mode" in {
          val result = navigator.nextPage(HowManyLitresGloballyPage, CheckMode,
            UserAnswers("id", Json.obj(HowManyLitresGloballyPage.toString -> Large.toString)))
          result mustBe routes.OperatePackagingSitesController.onPageLoad(CheckMode)
        }

        "must navigate to third party packagers page when less than less than 1 million litres is selected in check mode" in {
          val result = navigator.nextPage(HowManyLitresGloballyPage, CheckMode,
            UserAnswers("id", Json.obj(HowManyLitresGloballyPage.toString -> Small.toString)))
          result mustBe routes.ThirdPartyPackagersController.onPageLoad(CheckMode)
        }

        "must navigate to third party packagers page when None is selected in check mode" in {
          val result = navigator.nextPage(HowManyLitresGloballyPage, CheckMode,
            UserAnswers("id", Json.obj(HowManyLitresGloballyPage.toString -> HowManyLitresGlobally.None.toString)))
          result mustBe routes.ContractPackingController.onPageLoad(CheckMode)
        }

        "must navigate to index controller page when no answer available in user answers" in {
          val result = navigator.nextPage(HowManyLitresGloballyPage, CheckMode,
            UserAnswers("id", Json.obj()))
          result mustBe routes.IndexController.onPageLoad()
        }
      }
    }
  }
}
