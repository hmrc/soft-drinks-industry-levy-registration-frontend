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
import models.LitresInBands
import pages.{ContractPackingPage, HowManyContractPackingPage, HowManyImportsPage, HowManyOperatePackagingSitesPage, ImportsPage, OperatePackagingSitesPage, StartDatePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView
import views.summary._

import java.time.LocalDate

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET with empty user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), rosmRegistration = rosmRegistration).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = Seq.empty

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, routes.CheckYourAnswersController.onSubmit())(request, messages(application)).toString
      }
    }
    "must return OK and the correct view for a GET with full user answers with litres pages yes" in {
      val userAnswerDate: LocalDate = LocalDate.of(2023, 6, 1)
      val userAnswers = emptyUserAnswers
        .set(OperatePackagingSitesPage, true).success.value
        .set(HowManyOperatePackagingSitesPage, LitresInBands(1,2)).success.value
        .set(ContractPackingPage, true).success.value
        .set(HowManyContractPackingPage, LitresInBands(3,4)).success.value
        .set(ImportsPage, true).success.value
        .set(HowManyImportsPage, LitresInBands(3,4)).success.value
        .set(StartDatePage, userAnswerDate).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), rosmRegistration = rosmRegistration).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val operatePackagingSites: (String, SummaryList) = {
          "operatePackagingSites.checkYourAnswersLabel" -> OperatePackagingSitesSummary.summaryList(userAnswers = userAnswers, isCheckAnswers = true)
        }
        val contractPacking: (String, SummaryList) = {
          "contractPacking.checkYourAnswersLabel" -> ContractPackingSummary.summaryList(userAnswers = userAnswers, isCheckAnswers = true)
        }
        val imports: (String, SummaryList) = {
          "imports.checkYourAnswersLabel" -> ImportsSummary.summaryList(userAnswers = userAnswers, isCheckAnswers = true)
        }
        val startDate: (String, SummaryList) = {
          "startDate.checkYourAnswersLabel" -> StartDateSummary.summaryList(userAnswers = userAnswers, isCheckAnswers = true)
        }

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq(operatePackagingSites, contractPacking, imports, startDate),
          routes.CheckYourAnswersController.onSubmit())(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, rosmRegistration = rosmRegistration).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "must Redirect to next page when data is correct for POST" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, CheckYourAnswersController.onSubmit().url).withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual IndexController.onPageLoad().url
      }
    }
    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, CheckYourAnswersController.onSubmit().url).withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
