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

package controllers.addressLookupFrontend

import base.SpecBase
import models.alf.{AddressResponseForLookupState, AlfAddress, AlfResponse}
import models.backend.{Site, UkAddress}
import models.{CheckMode, NormalMode, UserAnswers, Warehouse}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.AddressLookupService
import services.AddressLookupState._

import scala.collection.immutable.List
import scala.concurrent.Future

class RampOffControllerSpec extends SpecBase with MockitoSugar {
  class Setup {
    val mockAddressLookupService: AddressLookupService = mock[AddressLookupService]
    val mockSessionRepository: SessionRepository = mock[SessionRepository]
    val alfId: String = "bar"
    val addressLines = List("line 1", "line 2", "line 3", "line 4")
    val postcode = "aa1 1aa"
    val ukAddress = UkAddress(addressLines, postcode, alfId = Some(alfId))
    val diffAddress = UkAddress(List("29 Station Pl.", "The Railyard", "Cambridge"), "CB1 2FP")
    val sdilId = "123456"
    val tradingName = "Sugary Lemonade"
    val alfResponseWithNoTradingName: AlfResponse = AlfResponse(AlfAddress(None, addressLines, Some(postcode), None))
    val alfResponseWithTradingName: AlfResponse = AlfResponse(AlfAddress(Some(tradingName), addressLines, Some(postcode), None))
    val alfResponseEmpty: AlfResponse = AlfResponse(AlfAddress(None, List.empty, None, None))

  }

  val modes = List(CheckMode, NormalMode)

  modes.foreach { mode =>
    s"when in ${mode.toString}" - {
      s"$BusinessAddress off ramp" - {
        val expectedLocation = if (mode == CheckMode) {
          controllers.routes.CheckYourAnswersController.onPageLoad.url
        } else {
          controllers.routes.OrganisationTypeController.onPageLoad(NormalMode).url
        }
        s"should add the address to userAnswers and redirect to $expectedLocation when ALF returns address successfully" - {
          "when the alfResponse doesn't contain an organisation name" in new Setup {
            val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[AddressLookupService].toInstance(mockAddressLookupService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

            val updatedUserAnswers = emptyUserAnswers.copy(address = Some(ukAddress))

            when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
              .thenReturn(Future.successful(alfResponseWithNoTradingName))
            when(mockAddressLookupService.addressChecker(
              ArgumentMatchers.eq(alfResponseWithNoTradingName.address),
              ArgumentMatchers.eq(alfId)))
              .thenReturn(ukAddress)
            when(mockSessionRepository.set(ArgumentMatchers.eq(updatedUserAnswers)))
              .thenReturn(Future.successful(true))

            running(app) {
              val request = FakeRequest(GET, routes.RampOffController.businessAddressOffRamp(sdilId, alfId, mode).url)

              val result = route(app, request).value
              status(result) mustBe SEE_OTHER
              val expectedLocation = if (mode == CheckMode) {
                controllers.routes.CheckYourAnswersController.onPageLoad.url
              } else {
                controllers.routes.OrganisationTypeController.onPageLoad(NormalMode).url
              }
              redirectLocation(result).get mustBe expectedLocation
            }
          }
        }

        s"should add the address to userAnswers, ignore the organisation name and redirect to $expectedLocation when ALF returns address successfully" - {
          "when the alfResponse contains an organisation name" in new Setup {
            val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[AddressLookupService].toInstance(mockAddressLookupService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

            val updatedUserAnswers = emptyUserAnswers.copy(address = Some(ukAddress))
            when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
              .thenReturn(Future.successful(alfResponseWithTradingName))
            when(mockAddressLookupService.addressChecker(
              ArgumentMatchers.eq(alfResponseWithTradingName.address),
              ArgumentMatchers.eq(alfId)))
              .thenReturn(ukAddress)
            when(mockSessionRepository.set(ArgumentMatchers.eq(updatedUserAnswers)))
              .thenReturn(Future.successful(true))

            running(app) {
              val request = FakeRequest(GET, routes.RampOffController.businessAddressOffRamp(sdilId, alfId, mode).url)

              val result = route(app, request).value
              status(result) mustBe SEE_OTHER
              val expectedLocation = if (mode == CheckMode) {
                controllers.routes.CheckYourAnswersController.onPageLoad.url
              } else {
                controllers.routes.OrganisationTypeController.onPageLoad(NormalMode).url
              }
              redirectLocation(result).get mustBe expectedLocation
            }
          }
        }
        s"should return exception to the next page when ALF doesnt return Address successfully" in new Setup {
          val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AddressLookupService].toInstance(mockAddressLookupService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.failed(new Exception("woopsie")))

          running(application) {
            val request = FakeRequest(GET, routes.RampOffController.businessAddressOffRamp(sdilId, alfId, mode).url)

            intercept[Exception](await(route(application, request).value))
          }
        }
        s"should return exception to the next page when addressChecker fails" in new Setup {
          val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AddressLookupService].toInstance(mockAddressLookupService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(alfResponseEmpty))
          when(mockAddressLookupService.addressChecker(
            ArgumentMatchers.eq(alfResponseEmpty.address),
            ArgumentMatchers.eq(alfId)))
            .thenThrow(new RuntimeException("foo"))

          running(application) {
            val request = FakeRequest(GET, routes.RampOffController.businessAddressOffRamp(sdilId, alfId, mode).url)

            intercept[Exception](await(route(application, request).value))
          }
        }
        s"should return exception when database update fails" in new Setup {
          val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AddressLookupService].toInstance(mockAddressLookupService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()
          val updatedUserAnswers = emptyUserAnswers.copy(address = Some(ukAddress))
          when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(alfResponseWithTradingName))
          when(mockAddressLookupService.addressChecker(
            ArgumentMatchers.eq(alfResponseWithTradingName.address),
            ArgumentMatchers.eq(alfId)))
            .thenReturn(ukAddress)
          when(mockSessionRepository.set(ArgumentMatchers.eq(updatedUserAnswers)))
            .thenReturn(Future.failed(new Exception("woopsie")))

          running(application) {
            val request = FakeRequest(GET, routes.RampOffController.businessAddressOffRamp(sdilId, alfId, mode).url)
            intercept[Exception](await(route(application, request).value))
          }
        }
      }

      s"$WarehouseDetails off ramp" - {
        "when address lookup returns a success response with an organisation name" - {
          "should add the warehouse to useranswers and redirect to the warehouse details page" in new Setup {
            val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[AddressLookupService].toInstance(mockAddressLookupService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

            val updatedUserAnswers = emptyUserAnswers.copy(warehouseList = Map(sdilId -> Warehouse(tradingName, ukAddress)))

            when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
              .thenReturn(Future.successful(alfResponseWithTradingName))
            when(mockAddressLookupService.addressChecker(
              ArgumentMatchers.eq(alfResponseWithTradingName.address),
              ArgumentMatchers.eq(alfId)))
              .thenReturn(ukAddress)
            when(mockSessionRepository.set(ArgumentMatchers.eq(updatedUserAnswers)))
              .thenReturn(Future.successful(true))

            running(app) {
              val request = FakeRequest(GET, routes.RampOffController.wareHouseDetailsOffRamp(sdilId, alfId, mode).url)

              val result = route(app, request).value
              status(result) mustBe SEE_OTHER
              redirectLocation(result).get mustBe controllers.routes.WarehouseDetailsController.onPageLoad(mode).url
            }
          }
        }

        "when address lookup returns a success response with no organisation name" - {
          "should add the alfResponseForLookupState to useranswers and redirect to the trading name page" in new Setup {
            val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[AddressLookupService].toInstance(mockAddressLookupService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

            val updatedUserAnswers = emptyUserAnswers.copy(alfResponseForLookupState = Some(AddressResponseForLookupState(ukAddress, WarehouseDetails, sdilId)))

            when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
              .thenReturn(Future.successful(alfResponseWithNoTradingName))
            when(mockAddressLookupService.addressChecker(
              ArgumentMatchers.eq(alfResponseWithNoTradingName.address),
              ArgumentMatchers.eq(alfId)))
              .thenReturn(ukAddress)
            when(mockSessionRepository.set(ArgumentMatchers.eq(updatedUserAnswers)))
              .thenReturn(Future.successful(true))

            running(app) {
              val request = FakeRequest(GET, routes.RampOffController.wareHouseDetailsOffRamp(sdilId, alfId, mode).url)

              val result = route(app, request).value
              status(result) mustBe SEE_OTHER
              redirectLocation(result).get mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
            }
          }
        }

        s"should return exception to the next page when ALF doesnt return Address successfully" in new Setup {
          val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AddressLookupService].toInstance(mockAddressLookupService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.failed(new Exception("woopsie")))

          running(application) {
            val request = FakeRequest(GET, routes.RampOffController.wareHouseDetailsOffRamp(sdilId, alfId, mode).url)

            intercept[Exception](await(route(application, request).value))
          }
        }
        s"should return exception to the next page when ALF returns address, but it can't be converted" in new Setup {
          val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AddressLookupService].toInstance(mockAddressLookupService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(alfResponseEmpty))
          when(mockAddressLookupService.addressChecker(
            ArgumentMatchers.eq(alfResponseEmpty.address),
            ArgumentMatchers.eq(alfId)))
            .thenThrow(new RuntimeException("foo"))

          running(application) {
            val request = FakeRequest(GET, routes.RampOffController.wareHouseDetailsOffRamp(sdilId, alfId, mode).url)

            intercept[Exception](await(route(application, request).value))
          }
        }
        s"should return exception to the next page when ALF returns address, it can be converted but fails to update db" in new Setup {
          val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AddressLookupService].toInstance(mockAddressLookupService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()
          val responseFromGetAddress: AlfResponse = AlfResponse(AlfAddress(Some("foo"), List.empty, None, None))
          val updatedUserAnswers: UserAnswers = emptyUserAnswers.copy(id = "foobarwizz")

          when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(alfResponseWithNoTradingName))
          when(mockAddressLookupService.addressChecker(
            ArgumentMatchers.eq(alfResponseWithNoTradingName.address),
            ArgumentMatchers.eq(alfId)))
            .thenReturn(ukAddress)
          when(mockSessionRepository.set(ArgumentMatchers.any()))
            .thenReturn(Future.failed(new Exception("woopsie")))

          running(application) {
            val request = FakeRequest(GET, routes.RampOffController.wareHouseDetailsOffRamp(sdilId, alfId, mode).url)
            intercept[Exception](await(route(application, request).value))
          }
        }
      }

      s"$PackingDetails off ramp" - {
        "when address lookup returns a success response with an organisation name" - {
          "should add the packagingSite to useranswers and redirect to the packaging site details page" in new Setup {
            val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[AddressLookupService].toInstance(mockAddressLookupService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

            val updatedUserAnswers = emptyUserAnswers.copy(packagingSiteList = Map(sdilId -> Site(ukAddress, None, tradingName, None)))

            when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
              .thenReturn(Future.successful(alfResponseWithTradingName))
            when(mockAddressLookupService.addressChecker(
              ArgumentMatchers.eq(alfResponseWithTradingName.address),
              ArgumentMatchers.eq(alfId)))
              .thenReturn(ukAddress)
            when(mockSessionRepository.set(ArgumentMatchers.eq(updatedUserAnswers)))
              .thenReturn(Future.successful(true))

            running(app) {
              val request = FakeRequest(GET, routes.RampOffController.packingSiteDetailsOffRamp(sdilId, alfId, mode).url)

              val result = route(app, request).value
              status(result) mustBe SEE_OTHER
              redirectLocation(result).get mustBe controllers.routes.PackagingSiteDetailsController.onPageLoad(mode).url
            }
          }
        }

        "when address lookup returns a success response with no organisation name" - {
          "should add the alfResponseForLookupState to useranswers and redirect to the trading name page" in new Setup {
            val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[AddressLookupService].toInstance(mockAddressLookupService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

            val updatedUserAnswers = emptyUserAnswers.copy(alfResponseForLookupState = Some(AddressResponseForLookupState(ukAddress, PackingDetails, sdilId)))

            when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
              .thenReturn(Future.successful(alfResponseWithNoTradingName))
            when(mockAddressLookupService.addressChecker(
              ArgumentMatchers.eq(alfResponseWithNoTradingName.address),
              ArgumentMatchers.eq(alfId)))
              .thenReturn(ukAddress)
            when(mockSessionRepository.set(ArgumentMatchers.eq(updatedUserAnswers)))
              .thenReturn(Future.successful(true))

            running(app) {
              val request = FakeRequest(GET, routes.RampOffController.packingSiteDetailsOffRamp(sdilId, alfId, mode).url)

              val result = route(app, request).value
              status(result) mustBe SEE_OTHER
              redirectLocation(result).get mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
            }
          }
        }

        s"should return exception to the next page when ALF doesnt return Address successfully" in new Setup {
          val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AddressLookupService].toInstance(mockAddressLookupService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.failed(new Exception("woopsie")))

          running(application) {
            val request = FakeRequest(GET, routes.RampOffController.packingSiteDetailsOffRamp(sdilId, alfId, mode).url)

            intercept[Exception](await(route(application, request).value))
          }
        }
        s"should return exception to the next page when ALF returns address, but it can't be converted" in new Setup {
          val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AddressLookupService].toInstance(mockAddressLookupService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(alfResponseEmpty))
          when(mockAddressLookupService.addressChecker(
            ArgumentMatchers.eq(alfResponseEmpty.address),
            ArgumentMatchers.eq(alfId)))
            .thenThrow(new RuntimeException("foo"))

          running(application) {
            val request = FakeRequest(GET, routes.RampOffController.packingSiteDetailsOffRamp(sdilId, alfId, mode).url)

            intercept[Exception](await(route(application, request).value))
          }
        }
        s"should return exception to the next page when ALF returns address, it can be converted but fails to update db" in new Setup {
          val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AddressLookupService].toInstance(mockAddressLookupService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          when(mockAddressLookupService.getAddress(ArgumentMatchers.eq(alfId))(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(alfResponseWithNoTradingName))
          when(mockAddressLookupService.addressChecker(
            ArgumentMatchers.eq(alfResponseWithNoTradingName.address),
            ArgumentMatchers.eq(alfId)))
            .thenReturn(ukAddress)
          when(mockSessionRepository.set(ArgumentMatchers.any()))
            .thenReturn(Future.failed(new Exception("woopsie")))

          running(application) {
            val request = FakeRequest(GET, routes.RampOffController.packingSiteDetailsOffRamp(sdilId, alfId, mode).url)
            intercept[Exception](await(route(application, request).value))
          }
        }
      }
    }
  }
}
