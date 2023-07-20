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
import config.FrontendAppConfig
import models.HowManyLitresGlobally.Large
import models.OrganisationType.LimitedCompany
import models.Verify.YesRegister
import models._
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.summary.{BusinessDetailsSummary, ContactDetailsSummary}
import views.html.RegistrationConfirmationView
import views.summary.{ContractPackingSummary, ImportsSummary, OperatePackagingSitesSummary, StartDateSummary}

import java.time.{LocalDate, LocalDateTime, ZoneOffset}

class RegistrationConfirmationControllerSpec extends SpecBase {

  "RegistrationConfirmation Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswerDate: LocalDate = LocalDate.of(2023, 6, 1)
      val submittedDateTime = LocalDateTime.of(2023, 6, 1, 10, 30)
      val userAnswers = {
        emptyUserAnswers
          .copy(submittedOn = Some(submittedDateTime.toInstant(ZoneOffset.UTC)))
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
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationConfirmationController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegistrationConfirmationView]
        val config = application.injector.instanceOf[FrontendAppConfig]

        val businessDetails: (String, SummaryList) = BusinessDetailsSummary.headingAndSummary(userAnswers, rosmRegistration, false).get

        val operatePackagingSites: (String, SummaryList) = {
          "operatePackagingSites.checkYourAnswersLabel" -> OperatePackagingSitesSummary.summaryList(userAnswers = userAnswers, isCheckAnswers = false)
        }
        val contractPacking: (String, SummaryList) = {
          "contractPacking.checkYourAnswersLabel" -> ContractPackingSummary.summaryList(userAnswers = userAnswers, isCheckAnswers = false)
        }
        val imports: (String, SummaryList) = {
          "imports.checkYourAnswersLabel" -> ImportsSummary.summaryList(userAnswers = userAnswers, isCheckAnswers = false)
        }
        val startDate: (String, SummaryList) = {
          "startDate.checkYourAnswersLabel" -> StartDateSummary.summaryList(userAnswers = userAnswers, isCheckAnswers = false)
        }
        val contactDetails: (String, SummaryList) = ContactDetailsSummary.headingAndSummary(userAnswers = userAnswers, isCheckAnswers = false).get

        val headingAndSummaryItems = Seq(businessDetails, operatePackagingSites, contractPacking, imports, startDate, contactDetails)


        status(result) mustEqual OK
        contentAsString(result) mustEqual view(headingAndSummaryItems, submittedDateTime, rosmRegistration.rosmRegistration.organisationName, "bang")(request, messages(application), config).toString
      }
    }

    "must redirect to start of journey if registration request not submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationConfirmationController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(routes.VerifyController.onPageLoad(NormalMode).url)
      }
    }
  }
}
