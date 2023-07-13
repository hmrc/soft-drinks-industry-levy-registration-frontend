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

package controllers.actions

import base.SpecBase
import models.HowManyLitresGlobally.{Large, Small}
import models.OrganisationType.LimitedCompany
import models.Verify.YesRegister
import models.backend.UkAddress
import models.requests.DataRequest
import models.{ContactDetails, HowManyLitresGlobally, LitresInBands, NormalMode, OrganisationType, RosmRegistration, RosmWithUtr, Verify}
import pages._
import play.api.libs.json.Reads
import play.api.mvc.Results.Ok
import play.api.test.Helpers.{contentAsString, redirectLocation}
import play.api.test.{DefaultAwaitTimeout, FakeRequest}

import java.time.LocalDate
import scala.concurrent.Future

class RequiredUserAnswersSpec extends SpecBase with DefaultAwaitTimeout {

  val requiredUserAnswers: RequiredUserAnswers = application.injector.instanceOf[RequiredUserAnswers]

  "requireData" - {
    "should return result passed in when not a page matched in function" in {
      val dataRequest = DataRequest(
        FakeRequest(),"", false, None, emptyUserAnswers, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
      )
      val action = Future.successful(Ok("woohoo"))
      contentAsString(requiredUserAnswers.requireData(VerifyPage)(action)(dataRequest)) mustBe "woohoo"
    }
    s"should return result passed in when page is $CheckYourAnswersPage" - {
      s"when HowManyLitresGlobally is $Large and all answers are answered" in {
        val userAnswers = {
          emptyUserAnswers
            .set(VerifyPage, YesRegister).success.value
            .set(OrganisationTypePage, LimitedCompany).success.value
            .set(HowManyLitresGloballyPage, Large).success.value
            .set(OperatePackagingSitesPage, true).success.value
            .set(HowManyOperatePackagingSitesPage, LitresInBands(1,1)).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1,1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1,1)).success.value
            .set(StartDatePage, LocalDate.now()).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(PackagingSiteDetailsPage, true).success.value
            .set(AskSecondaryWarehousesPage, true).success.value
            .set(WarehouseDetailsPage, true).success.value
            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
        }
        val dataRequest = DataRequest(
          FakeRequest(),"", false, None, userAnswers, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
        )
        val action = Future.successful(Ok("woohoo"))
        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(action)(dataRequest)) mustBe "woohoo"
      }
      s"when $HowManyLitresGlobally is $Small and all answers are answered" in {
        val userAnswers = {
          emptyUserAnswers
            .set(VerifyPage, YesRegister).success.value
            .set(OrganisationTypePage, LimitedCompany).success.value
            .set(HowManyLitresGloballyPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(OperatePackagingSitesPage, true).success.value
            .set(HowManyOperatePackagingSitesPage, LitresInBands(1,1)).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1,1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1,1)).success.value
            .set(StartDatePage, LocalDate.now()).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(PackagingSiteDetailsPage, true).success.value
            .set(AskSecondaryWarehousesPage, true).success.value
            .set(WarehouseDetailsPage, true).success.value
            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
        }
        val dataRequest = DataRequest(
          FakeRequest(),"", false, None, userAnswers, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
        )
        val action = Future.successful(Ok("woohoo"))
        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(action)(dataRequest)) mustBe "woohoo"
      }
      s"when $HowManyLitresGlobally is $None and all answers are answered" in {
        val userAnswers = {
          emptyUserAnswers
            .set(VerifyPage, YesRegister).success.value
            .set(OrganisationTypePage, LimitedCompany).success.value
            .set(HowManyLitresGloballyPage, HowManyLitresGlobally.None).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1,1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1,1)).success.value
            .set(StartDatePage, LocalDate.now()).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(PackagingSiteDetailsPage, true).success.value
            .set(AskSecondaryWarehousesPage, true).success.value
            .set(WarehouseDetailsPage, true).success.value
            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
        }
        val dataRequest = DataRequest(
          FakeRequest(),"", false, None, userAnswers, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
        )
        val action = Future.successful(Ok("woohoo"))
        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(action)(dataRequest)) mustBe "woohoo"
      }
      s"when all $LitresInBands are not required" in {
        val userAnswers = {
          emptyUserAnswers
            .set(VerifyPage, YesRegister).success.value
            .set(OrganisationTypePage, LimitedCompany).success.value
            .set(HowManyLitresGloballyPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(OperatePackagingSitesPage, false).success.value
            .set(ContractPackingPage, false).success.value
            .set(ImportsPage, false).success.value
            .set(StartDatePage, LocalDate.now()).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(PackagingSiteDetailsPage, true).success.value
            .set(AskSecondaryWarehousesPage, true).success.value
            .set(WarehouseDetailsPage, true).success.value
            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
        }
        val dataRequest = DataRequest(
          FakeRequest(),"", false, None, userAnswers, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
        )
        val action = Future.successful(Ok("woohoo"))
        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(action)(dataRequest)) mustBe "woohoo"
      }
      "when warehouses are not required" in {
        val userAnswers = {
          emptyUserAnswers
            .set(VerifyPage, YesRegister).success.value
            .set(OrganisationTypePage, LimitedCompany).success.value
            .set(HowManyLitresGloballyPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(OperatePackagingSitesPage, true).success.value
            .set(HowManyOperatePackagingSitesPage, LitresInBands(1,1)).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(1,1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1,1)).success.value
            .set(StartDatePage, LocalDate.now()).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(PackagingSiteDetailsPage, true).success.value
            .set(AskSecondaryWarehousesPage, false).success.value
            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
        }
        val dataRequest = DataRequest(
          FakeRequest(),"", false, None, userAnswers, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
        )
        val action = Future.successful(Ok("woohoo"))
        contentAsString(requiredUserAnswers.requireData(CheckYourAnswersPage)(action)(dataRequest)) mustBe "woohoo"
      }
    }
    s"should redirect to verify controller when missing answers for $CheckYourAnswersPage" - {
      "with no answers" in {
        val dataRequest = DataRequest(
          FakeRequest(),"", false, None, emptyUserAnswers, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
        )
        val action = Future.successful(Ok("woohoo"))
        val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(action)(dataRequest))
        res.get mustBe controllers.routes.VerifyController.onPageLoad(NormalMode).url
      }
      "with missing selection of pages" in {
        val userAnswers = {
          emptyUserAnswers
            .set(OrganisationTypePage, LimitedCompany).success.value
            .set(HowManyLitresGloballyPage, Small).success.value
            .set(ThirdPartyPackagersPage, true).success.value
            .set(OperatePackagingSitesPage, true).success.value
            .set(HowManyOperatePackagingSitesPage, LitresInBands(1,1)).success.value
            .set(HowManyContractPackingPage, LitresInBands(1,1)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(1,1)).success.value
            .set(StartDatePage, LocalDate.now()).success.value
            .set(PackagingSiteDetailsPage, true).success.value
            .set(AskSecondaryWarehousesPage, false).success.value
            .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
        }
        val dataRequest = DataRequest(
          FakeRequest(),"", false, None, userAnswers, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
        )
        val action = Future.successful(Ok("woohoo"))
        val res = redirectLocation(requiredUserAnswers.requireData(CheckYourAnswersPage)(action)(dataRequest))
        res.get mustBe controllers.routes.VerifyController.onPageLoad(NormalMode).url
      }
    }
  }
  "returnMissingAnswers" - {
    "should return all missing answers when user answers is empty" in {
      implicit val dataRequest = DataRequest(
        FakeRequest(),"", false, None, emptyUserAnswers, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
      )
      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
      res mustBe
        List(
          RequiredPage(VerifyPage, None)(implicitly[Reads[Verify]]),
          RequiredPage(OrganisationTypePage, None)(implicitly[Reads[OrganisationType]]),
          RequiredPage(HowManyLitresGloballyPage, None)(implicitly[Reads[HowManyLitresGlobally]]),
          RequiredPage(ContractPackingPage, None)(implicitly[Reads[Boolean]]),
          RequiredPage(ImportsPage, None)(implicitly[Reads[Boolean]]),
          RequiredPage(StartDatePage, None)(implicitly[Reads[LocalDate]]),
          RequiredPage(PackAtBusinessAddressPage, None)(implicitly[Reads[Boolean]]),
          RequiredPage(PackagingSiteDetailsPage, None)(implicitly[Reads[Boolean]]),
          RequiredPage(AskSecondaryWarehousesPage, None)(implicitly[Reads[Boolean]]),
          RequiredPage(ContactDetailsPage, None)(implicitly[Reads[ContactDetails]])
        )
    }
    "should return all but 1 missing answers when user answers is fully populated apart from 1 answer" in {
      val userAnswers = {
        emptyUserAnswers
          .set(VerifyPage, YesRegister).success.value
          .set(OrganisationTypePage, LimitedCompany).success.value
          .set(HowManyLitresGloballyPage, Small).success.value
          .set(ThirdPartyPackagersPage, true).success.value
          .set(OperatePackagingSitesPage, true).success.value
          .set(HowManyOperatePackagingSitesPage, LitresInBands(1,1)).success.value
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(1,1)).success.value
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, LitresInBands(1,1)).success.value
          .set(StartDatePage, LocalDate.now()).success.value
          .set(PackAtBusinessAddressPage, true).success.value
          .set(PackagingSiteDetailsPage, true).success.value
          .set(AskSecondaryWarehousesPage, false).success.value
      }
      implicit val dataRequest = DataRequest(
        FakeRequest(),"", false, None, userAnswers, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
      )
      val res = requiredUserAnswers.returnMissingAnswers(requiredUserAnswers.journey)
      res mustBe List(RequiredPage(ContactDetailsPage, None)(implicitly[Reads[ContactDetails]]))
    }
  }
  "checkYourAnswersRequiredData" - {
    "should redirect to verify controller when missing answers" in {
      implicit val dataRequest = DataRequest(
        FakeRequest(),"", false, None, emptyUserAnswers, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
      )
      val action = Future.successful(Ok("woohoo"))
      redirectLocation(requiredUserAnswers.checkYourAnswersRequiredData(action)).get mustBe controllers.routes.VerifyController.onPageLoad(NormalMode).url
    }
    "should redirect to action when all answers answered" in {
      val userAnswers = {
        emptyUserAnswers
          .set(VerifyPage, YesRegister).success.value
          .set(OrganisationTypePage, LimitedCompany).success.value
          .set(HowManyLitresGloballyPage, Small).success.value
          .set(ThirdPartyPackagersPage, true).success.value
          .set(OperatePackagingSitesPage, true).success.value
          .set(HowManyOperatePackagingSitesPage, LitresInBands(1,1)).success.value
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(1,1)).success.value
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, LitresInBands(1,1)).success.value
          .set(StartDatePage, LocalDate.now()).success.value
          .set(PackAtBusinessAddressPage, true).success.value
          .set(PackagingSiteDetailsPage, true).success.value
          .set(AskSecondaryWarehousesPage, false).success.value
          .set(ContactDetailsPage, ContactDetails("", "", "", "")).success.value
      }
      implicit val dataRequest = DataRequest(
        FakeRequest(),"", false, None, userAnswers, RosmWithUtr("", RosmRegistration("", None, None, UkAddress(List.empty,"", None)))
      )
      val action = Future.successful(Ok("woohoo"))
      contentAsString(requiredUserAnswers.checkYourAnswersRequiredData(action)) mustBe "woohoo"
    }
  }
}
