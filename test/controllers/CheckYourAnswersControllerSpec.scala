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

package controllers

import base.SpecBase
import controllers.routes._
import errors.{MissingRequiredUserAnswers, UnexpectedResponseFromSDIL}
import models.HowManyLitresGlobally.Large
import models.OrganisationType.LimitedCompany
import models.Verify.YesRegister
import models.backend.Subscription
import models.{CheckMode, ContactDetails, CreatedSubscriptionAndAmountProducedGlobally, LitresInBands, NormalMode}
import orchestrators.RegistrationOrchestrator
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView
import views.summary._

import java.time.LocalDate

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  val mockRegistrationOrchestrator = mock[RegistrationOrchestrator]

  "Check Your Answers Controller" - {

    "must redirect to verify controller for a GET with empty user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), rosmRegistration = rosmRegistration).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        redirectLocation(result).get mustEqual VerifyController.onPageLoad(CheckMode).url
      }
    }
    "must redirect to missing page for a GET with user answers full apart from 1 missing answer" in {
      val userAnswerDate: LocalDate = LocalDate.of(2023, 6, 1)
      val userAnswers = {
        emptyUserAnswers
          .set(VerifyPage, YesRegister).success.value
          .set(OrganisationTypePage, LimitedCompany).success.value
          .set(HowManyLitresGloballyPage, Large).success.value
          .set(OperatePackagingSitesPage, true).success.value
          .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 2)).success.value
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(3, 4)).success.value
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, LitresInBands(3, 4)).success.value
          .set(StartDatePage, userAnswerDate).success.value
          .set(PackAtBusinessAddressPage, true).success.value
          .set(PackagingSiteDetailsPage, true).success.value
          .set(AskSecondaryWarehousesPage, true).success.value
          .set(WarehouseDetailsPage, true).success.value
      }
      val application = applicationBuilder(userAnswers = Some(userAnswers), rosmRegistration = rosmRegistration).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        redirectLocation(result).get mustEqual ContactDetailsPage.url(CheckMode)
      }
    }
    "must return OK and the correct view for a GET with full user answers with litres pages yes" in {
      val userAnswerDate: LocalDate = LocalDate.of(2023, 6, 1)
      val userAnswers = {
        emptyUserAnswers
          .copy(packagingSiteList = packagingSiteListWith3)
          .copy(warehouseList = warehouseListWith1)
          .set(VerifyPage, YesRegister).success.value
          .set(OrganisationTypePage, LimitedCompany).success.value
          .set(HowManyLitresGloballyPage, Large).success.value
          .set(OperatePackagingSitesPage, true).success.value
          .set(HowManyOperatePackagingSitesPage, LitresInBands(1,2)).success.value
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(3,4)).success.value
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, LitresInBands(3,4)).success.value
          .set(StartDatePage, userAnswerDate).success.value
          .set(PackAtBusinessAddressPage, true).success.value
          .set(PackagingSiteDetailsPage, true).success.value
          .set(AskSecondaryWarehousesPage, true).success.value
          .set(WarehouseDetailsPage, true).success.value
          .set(ContactDetailsPage, ContactDetails("foo", "bar", "wizz", "bang")).success.value
      }
      val subscription = Subscription.generate(userAnswers, rosmRegistration)

      val application = applicationBuilder(userAnswers = Some(userAnswers), utr = Some("0000000022"), rosmRegistration = rosmRegistration)
        .overrides(bind[RegistrationOrchestrator].to(mockRegistrationOrchestrator)).build()


      running(application) {
        when(mockRegistrationOrchestrator.getSubscriptionAndHowManyLitresGlobally(any(), any())).thenReturn(Right(
          CreatedSubscriptionAndAmountProducedGlobally(subscription, Large)
        ))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val headingAndSummaryItems = RegistrationSummary.summaryList(CreatedSubscriptionAndAmountProducedGlobally(subscription, Large))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(headingAndSummaryItems,
          routes.CheckYourAnswersController.onSubmit)(request, messages(application)).toString
      }
    }
    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, rosmRegistration = rosmRegistration).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "must Redirect to confirmation page when full user answers on POST" in {
      val userAnswerDate: LocalDate = LocalDate.of(2023, 6, 1)
      val userAnswers = {
        emptyUserAnswers
          .copy(packagingSiteList = packagingSiteListWith3)
          .copy(warehouseList = warehouseListWith1)
          .set(VerifyPage, YesRegister).success.value
          .set(OrganisationTypePage, LimitedCompany).success.value
          .set(HowManyLitresGloballyPage, Large).success.value
          .set(OperatePackagingSitesPage, true).success.value
          .set(HowManyOperatePackagingSitesPage, LitresInBands(1,2)).success.value
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(3,4)).success.value
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, LitresInBands(3,4)).success.value
          .set(StartDatePage, userAnswerDate).success.value
          .set(PackAtBusinessAddressPage, true).success.value
          .set(PackagingSiteDetailsPage, true).success.value
          .set(AskSecondaryWarehousesPage, true).success.value
          .set(WarehouseDetailsPage, true).success.value
          .set(ContactDetailsPage, ContactDetails("foo", "bar", "wizz", "bang")).success.value
      }
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[RegistrationOrchestrator].to(mockRegistrationOrchestrator)).build()

      running(application) {
        when(mockRegistrationOrchestrator.createSubscriptionAndUpdateUserAnswers(any(), any(), any())) thenReturn(createSuccessRegistrationResult((): Unit))

        val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url).withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual RegistrationConfirmationController.onPageLoad.url
      }
    }

    "must Redirect to verify page when creating of subscription model fails" in {
      val userAnswerDate: LocalDate = LocalDate.of(2023, 6, 1)
      val userAnswers = {
        emptyUserAnswers
          .set(VerifyPage, YesRegister).success.value
          .set(OrganisationTypePage, LimitedCompany).success.value
          .set(HowManyLitresGloballyPage, Large).success.value
          .set(OperatePackagingSitesPage, true).success.value
          .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 2)).success.value
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(3, 4)).success.value
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, LitresInBands(3, 4)).success.value
          .set(StartDatePage, userAnswerDate).success.value
          .set(PackAtBusinessAddressPage, true).success.value
          .set(PackagingSiteDetailsPage, true).success.value
          .set(AskSecondaryWarehousesPage, true).success.value
          .set(WarehouseDetailsPage, true).success.value
          .set(ContactDetailsPage, ContactDetails("foo", "bar", "wizz", "bang")).success.value
      }
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[RegistrationOrchestrator].to(mockRegistrationOrchestrator)).build()

      running(application) {
        when(mockRegistrationOrchestrator.createSubscriptionAndUpdateUserAnswers(any(), any(), any())) thenReturn (createFailureRegistrationResult(MissingRequiredUserAnswers))

        val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url).withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual VerifyController.onPageLoad(NormalMode).url
      }
    }
    "must Redirect to verify controller when empty user answers on POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url).withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual VerifyController.onPageLoad(CheckMode).url
      }
    }
      "must Redirect to missing page when full user answers 1 missing on POST" in {
        val userAnswerDate: LocalDate = LocalDate.of(2023, 6, 1)
        val userAnswers = {
          emptyUserAnswers
            .copy(packagingSiteList = packagingSiteListWith3)
            .set(VerifyPage, YesRegister).success.value
            .set(OrganisationTypePage, LimitedCompany).success.value
            .set(HowManyLitresGloballyPage, Large).success.value
            .set(OperatePackagingSitesPage, true).success.value
            .set(HowManyOperatePackagingSitesPage, LitresInBands(1,2)).success.value
            .set(ContractPackingPage, true).success.value
            .set(HowManyContractPackingPage, LitresInBands(3,4)).success.value
            .set(ImportsPage, true).success.value
            .set(HowManyImportsPage, LitresInBands(3,4)).success.value
            .set(StartDatePage, userAnswerDate).success.value
            .set(PackAtBusinessAddressPage, true).success.value
            .set(PackagingSiteDetailsPage, true).success.value
            .set(AskSecondaryWarehousesPage, true).success.value
            .set(WarehouseDetailsPage, true).success.value
        }
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .build()

        running(application) {
          val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url).withFormUrlEncodedBody()

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual ContactDetailsPage.url(CheckMode)
        }
      }
    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url).withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }

    "must fail if the backend call fails" in {
      val userAnswerDate: LocalDate = LocalDate.of(2023, 6, 1)
      val userAnswers = {
        emptyUserAnswers
          .set(VerifyPage, YesRegister).success.value
          .set(OrganisationTypePage, LimitedCompany).success.value
          .set(HowManyLitresGloballyPage, Large).success.value
          .set(OperatePackagingSitesPage, true).success.value
          .set(HowManyOperatePackagingSitesPage, LitresInBands(1, 2)).success.value
          .set(ContractPackingPage, true).success.value
          .set(HowManyContractPackingPage, LitresInBands(3, 4)).success.value
          .set(ImportsPage, true).success.value
          .set(HowManyImportsPage, LitresInBands(3, 4)).success.value
          .set(StartDatePage, userAnswerDate).success.value
          .set(PackAtBusinessAddressPage, true).success.value
          .set(PackagingSiteDetailsPage, true).success.value
          .set(AskSecondaryWarehousesPage, true).success.value
          .set(WarehouseDetailsPage, true).success.value
          .set(ContactDetailsPage, ContactDetails("foo", "bar", "wizz", "bang")).success.value
      }
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[RegistrationOrchestrator].to(mockRegistrationOrchestrator)).build()

      running(application) {
        when(mockRegistrationOrchestrator.createSubscriptionAndUpdateUserAnswers(any(), any(), any())) thenReturn (createFailureRegistrationResult(UnexpectedResponseFromSDIL))

        val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url).withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title() mustBe "Sorry, there is a problem with the service - 500 - Soft Drinks Industry Levy - GOV.UK"
      }
    }
  }
}
