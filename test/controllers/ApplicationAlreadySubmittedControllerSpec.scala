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
import connectors.SoftDrinksIndustryLevyConnector
import models.RegisterState
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.AddressFormattingHelper
import views.html.ApplicationAlreadySubmittedView

class ApplicationAlreadySubmittedControllerSpec extends SpecBase {

  "ApplicationAlreadySubmitted Controller" - {

    "must return OK and the correct view for a GET" in {
      val mockSdilConnector = mock[SoftDrinksIndustryLevyConnector]

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers.copy(registerState = RegisterState.RegisterApplicationAccepted)),
        utr = Some(utr))
        .overrides(
          bind[SoftDrinksIndustryLevyConnector].toInstance(mockSdilConnector)).build()

      running(application) {
        when(mockSdilConnector.retreiveRosmSubscription(any(), any())(any())).thenReturn(createSuccessRegistrationResult(rosmRegistration))
        val request = FakeRequest(GET, routes.ApplicationAlreadySubmittedController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ApplicationAlreadySubmittedView]

        val formattedAddress = AddressFormattingHelper.formatBusinessAddress(
          rosmRegistration.rosmRegistration.address,
          Some(rosmRegistration.rosmRegistration.organisationName))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formattedAddress)(request, messages(application)).toString
      }
    }
  }
}
