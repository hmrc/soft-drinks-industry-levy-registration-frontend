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

package services

import base.SpecBase
import connectors.AddressLookupConnector
import controllers.routes
import models.alf.init._
import models.alf.{AlfAddress, AlfResponse}
import models.core.ErrorModel
import models.{CheckMode, NormalMode}
import org.mockito.ArgumentMatchers
import org.mockito.MockitoSugar.{mock, when}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import services.AddressLookupState._

import scala.concurrent.Future

class AddressLookupServiceSpec extends SpecBase with FutureAwaits with DefaultAwaitTimeout {

  val mockALFConnector = mock[AddressLookupConnector]
  val service = new AddressLookupService(mockALFConnector, frontendAppConfig)
  val organisation = "soft drinks ltd"
  val addressLine1 = "line 1"
  val addressLine2 = "line 2"
  val addressLine3 = "line 3"
  val addressLine4 = "line 4"
  val postcode = "aa1 1aa"
  val countryCode = "UK"
  val customerAddressMax: AlfResponse = AlfResponse(
    AlfAddress(
      Some(organisation),
      List(addressLine1, addressLine2, addressLine3, addressLine4),
      Some(postcode),
      Some(countryCode)
    ))

  val offRampBaseUrl ="http://localhost:8706/soft-drinks-industry-levy-registration/off-ramp"

  "getAddress" - {
    "return an address when Connector returns success" in {
      when(mockALFConnector.getAddress("123456789")(hc, implicitly)).thenReturn(Future.successful(Right(customerAddressMax)))

      val res = service.getAddress("123456789")

      whenReady(res) { result =>
        result mustBe customerAddressMax
      }
    }
    "return an exception when Connector returns error" in {
      when(mockALFConnector.getAddress("123456789")(hc, implicitly)).thenReturn(Future.successful(Left(ErrorModel(1, "foo"))))

      val res = intercept[Exception](await(service.getAddress("123456789")))
      res.getMessage mustBe "Error returned from ALF for 123456789 1 foo for None"
    }
  }

  "initJourney" - {
    "should return response from connector" in {
      val journeyConfig = JourneyConfig(1, JourneyOptions(""), None, None)
      when(mockALFConnector.initJourney(ArgumentMatchers.eq(journeyConfig))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Right("foo")))

      whenReady(service.initJourney(journeyConfig)) {
        res => res mustBe Right("foo")
      }
    }
  }

  List(NormalMode, CheckMode).foreach { mode =>
    s"when in ${mode.toString}" - {
      "initJourneyAndReturnOnRampUrl" - {
        s"should return Successful future when connector returns success for $PackingDetails" in {
          val sdilId = "Foobar"
          val expectedJourneyConfigToBePassedToConnector = JourneyConfig(
            version = frontendAppConfig.AddressLookupConfig.version,
            options = JourneyOptions(
              continueUrl = s"$offRampBaseUrl/${if(mode == CheckMode){"change-"}else{""}}packing-site-details/$sdilId",
              homeNavHref = None,
              signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
<<<<<<< HEAD
              accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-registration-frontend"),
=======
              accessibilityFooterUrl = Some("http://localhost:12346"),
>>>>>>> origin/main
              phaseFeedbackLink = None,
              deskProServiceName = None,
              showPhaseBanner = Some(false),
              alphaPhase = None,
              includeHMRCBranding = Some(true),
              ukMode = Some(true),
              selectPageConfig = Some(SelectPageConfig(
                proposalListLimit = Some(10),
                showSearchAgainLink = Some(true)
              )),
              showBackButtons = Some(true),
              disableTranslations = Some(true),
              allowedCountryCodes = None,
              confirmPageConfig = Some(ConfirmPageConfig(
                showSearchAgainLink = Some(true),
                showSubHeadingAndInfo = Some(true),
                showChangeLink = Some(true),
                showConfirmChangeText = Some(true)
              )),
              timeoutConfig = Some(TimeoutConfig(
                timeoutAmount = frontendAppConfig.timeout,
                timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
                timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
              )),
              serviceHref = Some(frontendAppConfig.sdilHomeUrl),
              pageHeadingStyle = Some("govuk-heading-l")
            ),
            labels = Some(
              JourneyLabels(
                en = Some(LanguageLabels(
                  appLevelLabels = Some(AppLevelLabels(
                    navTitle = Some("Soft Drinks Industry Levy"),
                    phaseBannerHtml = None
                  )),
                  selectPageLabels = None,
                  lookupPageLabels = Some(
                    LookupPageLabels(
                      title = Some("Find UK packaging site address"),
                      heading = Some("Find UK packaging site address"),
                      postcodeLabel = Some("Postcode"))),
                  editPageLabels = Some(
                    EditPageLabels(
                      title = Some("Enter the UK packaging site address"),
                      heading = Some("Enter the UK packaging site address"),
                      line1Label = Some("Address line 1"),
                      line2Label = Some("Address line 2"),
                      line3Label = Some("Address line 3 (optional)"),
                      townLabel = Some("Address line 4 (optional)"),
                      postcodeLabel = Some("Postcode"),
                      organisationLabel = Some("Packaging site name"))
                  ),
                  confirmPageLabels = None,
                  countryPickerLabels = None
                ))
              )),
            requestedVersion = None
          )

          when(mockALFConnector.initJourney(ArgumentMatchers.eq(expectedJourneyConfigToBePassedToConnector))(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Right("foo")))
          whenReady(service.initJourneyAndReturnOnRampUrl(PackingDetails, sdilId, mode)(implicitly, implicitly, implicitly)) {
            res => res mustBe "foo"
          }
        }
        s"should return Successful future when connector returns success for $WarehouseDetails" in {
          val sdilId = "Foobar"
          val expectedJourneyConfigToBePassedToConnector = JourneyConfig(
            version = 2,
            options = JourneyOptions(
              continueUrl = s"$offRampBaseUrl/${if(mode == CheckMode){"change-"}else{""}}warehouses/$sdilId",
              homeNavHref = None,
              signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
<<<<<<< HEAD
              accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-registration-frontend"),
=======
              accessibilityFooterUrl = Some("http://localhost:12346"),
>>>>>>> origin/main
              phaseFeedbackLink = None,deskProServiceName = None,
              showPhaseBanner = Some(false),
              alphaPhase = None,
              includeHMRCBranding = Some(true),
              ukMode = Some(true),
              selectPageConfig = Some(SelectPageConfig(
                proposalListLimit = Some(10),
                showSearchAgainLink = Some(true)
              )),
              showBackButtons = Some(true),
              disableTranslations = Some(true),
              allowedCountryCodes = None,
              confirmPageConfig = Some(ConfirmPageConfig(
                showSearchAgainLink = Some(true),
                showSubHeadingAndInfo = Some(true),
                showChangeLink = Some(true),
                showConfirmChangeText = Some(true)
              )),
              timeoutConfig = Some(TimeoutConfig(
                timeoutAmount = frontendAppConfig.timeout,
                timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
                timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
              )),
              serviceHref = Some(frontendAppConfig.sdilHomeUrl),
              pageHeadingStyle = Some("govuk-heading-l")
            ),
            labels = Some(
              JourneyLabels(
                en = Some(LanguageLabels(
                  appLevelLabels = Some(AppLevelLabels(
                    navTitle = Some("Soft Drinks Industry Levy"),
                    phaseBannerHtml = None
                  )),
                  selectPageLabels = None,
                  lookupPageLabels = Some(
                    LookupPageLabels(
                      title = Some("Find UK warehouse address"),
                      heading = Some("Find UK warehouse address"),
                      postcodeLabel = Some("Postcode"))),
                  editPageLabels = Some(
                    EditPageLabels(
                      title = Some("Enter the UK warehouse address"),
                      heading = Some("Enter the UK warehouse address"),
                      line1Label = Some("Address line 1"),
                      line2Label = Some("Address line 2"),
                      line3Label = Some("Address line 3 (optional)"),
                      townLabel = Some("Address line 4 (optional)"),
                      postcodeLabel = Some("Postcode"),
                      organisationLabel = Some("Trading name"))
                  ),
                  confirmPageLabels = None,
                  countryPickerLabels = None
                ))
              )),
            requestedVersion = None
          )

          when(mockALFConnector.initJourney(ArgumentMatchers.eq(expectedJourneyConfigToBePassedToConnector))(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Right("foo")))
          whenReady(service.initJourneyAndReturnOnRampUrl(WarehouseDetails, sdilId, mode)(implicitly, implicitly, implicitly)) {
            res => res mustBe "foo"
          }
        }

        s"should return Successful future when connector returns success for $BusinessAddress" in {
          val sdilId = "Foobar"
          val expectedJourneyConfigToBePassedToConnector = JourneyConfig(
            version = 2,
            options = JourneyOptions(
              continueUrl = s"$offRampBaseUrl/${if(mode == CheckMode){"change-"}else{""}}new-contact-address/$sdilId",
              homeNavHref = None,
              signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
<<<<<<< HEAD
              accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-registration-frontend"),
=======
              accessibilityFooterUrl = Some("http://localhost:12346"),
>>>>>>> origin/main
              phaseFeedbackLink = None,
              deskProServiceName = None,
              showPhaseBanner = Some(false),
              alphaPhase = None,
              includeHMRCBranding = Some(true),
              ukMode = Some(true),
              selectPageConfig = Some(SelectPageConfig(
                proposalListLimit = Some(10),
                showSearchAgainLink = Some(true)
              )),
              showBackButtons = Some(true),
              disableTranslations = Some(true),
              allowedCountryCodes = None,
              confirmPageConfig = Some(ConfirmPageConfig(
                showSearchAgainLink = Some(true),
                showSubHeadingAndInfo = Some(true),
                showChangeLink = Some(true),
                showConfirmChangeText = Some(true)
              )),
              timeoutConfig = Some(TimeoutConfig(
                timeoutAmount = frontendAppConfig.timeout,
                timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
                timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
              )),
              serviceHref = Some(frontendAppConfig.sdilHomeUrl),
              pageHeadingStyle = Some("govuk-heading-l")
            ),
            labels = Some(
              JourneyLabels(
                en = Some(LanguageLabels(
                  appLevelLabels = Some(AppLevelLabels(
                    navTitle = Some("Soft Drinks Industry Levy"),
                    phaseBannerHtml = None
                  )),
                  selectPageLabels = None,
                  lookupPageLabels = Some(
                    LookupPageLabels(
                      title = Some("Find UK contact address"),
                      heading = Some("Find UK contact address"),
                      postcodeLabel = Some("Postcode"))),
                  editPageLabels = Some(
                    EditPageLabels(
                      title = Some("Update your registered business address for the Soft Drinks Industry Levy"),
                      heading = Some("Update your registered business address for the Soft Drinks Industry Levy"),
                      line1Label = Some("Address line 1"),
                      line2Label = Some("Address line 2"),
                      line3Label = Some("Address line 3 (optional)"),
                      townLabel = Some("Address line 4 (optional)"),
                      postcodeLabel = Some("Postcode")
                    )
                  ),
                  confirmPageLabels = None,
                  countryPickerLabels = None
                ))
              )),
            requestedVersion = None
          )

          when(mockALFConnector.initJourney(ArgumentMatchers.eq(expectedJourneyConfigToBePassedToConnector))(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Right("foo")))
          whenReady(service.initJourneyAndReturnOnRampUrl(BusinessAddress, sdilId, mode)(implicitly, implicitly, implicitly)) {
            res => res mustBe "foo"
          }
        }

        "should return Exception if connector returns left" in {
          when(mockALFConnector.initJourney(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(Left(ErrorModel(1, "foo"))))
          val res = intercept[Exception](await(service.initJourneyAndReturnOnRampUrl(PackingDetails, mode = mode)(implicitly, implicitly, implicitly)))
          res.getMessage mustBe "Failed to init ALF foo with status 1 for None"
        }
      }

      "createJourneyConfig" - {
        s"should return a journey config for $WarehouseDetails" in {
          val exampleSdilIdWeGenerate: String = "wizz"
          val res = service.createJourneyConfig(WarehouseDetails, exampleSdilIdWeGenerate, mode)(implicitly)
          val expected = JourneyConfig(
            version = 2,
            options = JourneyOptions(
              continueUrl = s"$offRampBaseUrl/${if(mode == CheckMode){"change-"}else{""}}warehouses/$exampleSdilIdWeGenerate",
              homeNavHref = None,
              signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
<<<<<<< HEAD
              accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-registration-frontend"),
=======
              accessibilityFooterUrl = Some("http://localhost:12346"),
>>>>>>> origin/main
              phaseFeedbackLink = None,
              deskProServiceName = None,
              showPhaseBanner = Some(false),
              alphaPhase = None,
              includeHMRCBranding = Some(true),
              ukMode = Some(true),
              selectPageConfig = Some(SelectPageConfig(
                proposalListLimit = Some(10),
                showSearchAgainLink = Some(true)
              )),
              showBackButtons = Some(true),
              disableTranslations = Some(true),
              allowedCountryCodes = None,
              confirmPageConfig = Some(ConfirmPageConfig(
                showSearchAgainLink = Some(true),
                showSubHeadingAndInfo = Some(true),
                showChangeLink = Some(true),
                showConfirmChangeText = Some(true)
              )),
              timeoutConfig = Some(TimeoutConfig(
                timeoutAmount = frontendAppConfig.timeout,
                timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
                timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
              )),
              serviceHref = Some(frontendAppConfig.sdilHomeUrl),
              pageHeadingStyle = Some("govuk-heading-l")
            ),
            labels = Some(
              JourneyLabels(
                en = Some(LanguageLabels(
                  appLevelLabels = Some(AppLevelLabels(
                    navTitle = Some("Soft Drinks Industry Levy"),
                    phaseBannerHtml = None
                  )),
                  selectPageLabels = None,
                  lookupPageLabels = Some(
                    LookupPageLabels(
                      title = Some("Find UK warehouse address"),
                      heading = Some("Find UK warehouse address"),
                      postcodeLabel = Some("Postcode"))),
                  editPageLabels = Some(
                    EditPageLabels(
                      title = Some("Enter the UK warehouse address"),
                      heading = Some("Enter the UK warehouse address"),
                      line1Label = Some("Address line 1"),
                      line2Label = Some("Address line 2"),
                      line3Label = Some("Address line 3 (optional)"),
                      townLabel = Some("Address line 4 (optional)"),
                      postcodeLabel = Some("Postcode"),
                      organisationLabel = Some("Trading name"))
                  ),
                  confirmPageLabels = None,
                  countryPickerLabels = None
                ))
              )),
            requestedVersion = None
          )

          res mustBe expected
        }

        s"should return a journey config for $PackingDetails" in {
          val exampleSdilIdWeGenerate: String = "wizz"
          val res = service.createJourneyConfig(PackingDetails, exampleSdilIdWeGenerate, mode)(implicitly)
          val expected = JourneyConfig(
            version = 2,
            options = JourneyOptions(
              continueUrl = s"$offRampBaseUrl/${if(mode == CheckMode){"change-"}else{""}}packing-site-details/$exampleSdilIdWeGenerate",
              homeNavHref = None,
              signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
              accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-registration-frontend"),
              phaseFeedbackLink = None,
              deskProServiceName = None,
              showPhaseBanner = Some(false),
              alphaPhase = None,
              includeHMRCBranding = Some(true),
              ukMode = Some(true),
              selectPageConfig = Some(SelectPageConfig(
                proposalListLimit = Some(10),
                showSearchAgainLink = Some(true)
              )),
              showBackButtons = Some(true),
              disableTranslations = Some(true),
              allowedCountryCodes = None,
              confirmPageConfig = Some(ConfirmPageConfig(
                showSearchAgainLink = Some(true),
                showSubHeadingAndInfo = Some(true),
                showChangeLink = Some(true),
                showConfirmChangeText = Some(true)
              )),
              timeoutConfig = Some(TimeoutConfig(
                timeoutAmount = frontendAppConfig.timeout,
                timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
                timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
              )),
              serviceHref = Some(frontendAppConfig.sdilHomeUrl),
              pageHeadingStyle = Some("govuk-heading-l")
            ),
            labels = Some(
              JourneyLabels(
                en = Some(LanguageLabels(
                  appLevelLabels = Some(AppLevelLabels(
                    navTitle = Some("Soft Drinks Industry Levy"),
                    phaseBannerHtml = None
                  )),
                  selectPageLabels = None,
                  lookupPageLabels = Some(
                    LookupPageLabels(
                      title = Some("Find UK packaging site address"),
                      heading = Some("Find UK packaging site address"),
                      postcodeLabel = Some("Postcode"))),
                  editPageLabels = Some(
                    EditPageLabels(
                      title = Some("Enter the UK packaging site address"),
                      heading = Some("Enter the UK packaging site address"),
                      line1Label = Some("Address line 1"),
                      line2Label = Some("Address line 2"),
                      line3Label = Some("Address line 3 (optional)"),
                      townLabel = Some("Address line 4 (optional)"),
                      postcodeLabel = Some("Postcode"),
                      organisationLabel = Some("Packaging site name"))
                  ),
                  confirmPageLabels = None,
                  countryPickerLabels = None
                ))
              )),
            requestedVersion = None
          )

          res mustBe expected
        }
      }
    }
  }
}