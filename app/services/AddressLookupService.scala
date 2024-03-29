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

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import connectors.httpParsers.ResponseHttpParser.HttpResult
import controllers.routes
import models.Mode
import models.alf.init._
import models.alf.{ AlfAddress, AlfResponse }
import models.backend.UkAddress
import play.api.Logger
import play.api.i18n.Messages
import services.AddressLookupState._
import uk.gov.hmrc.http.HeaderCarrier
import utilities.AddressHelper

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class AddressLookupService @Inject() (
  addressLookupConnector: AddressLookupConnector,
  frontendAppConfig: FrontendAppConfig) extends AddressHelper {

  val logger: Logger = Logger(this.getClass)

  def addressChecker(address: AlfAddress, alfId: String): UkAddress = {
    val ukAddress: UkAddress = UkAddress(address.lines, address.postcode.getOrElse(""), alfId = Some(alfId))

    if (ukAddress.lines.isEmpty && ukAddress.postCode == "" && address.organisation.isEmpty) {
      throw new RuntimeException("Not Found (Alf has returned an empty address and organisation name)")
    } else {
      ukAddress
    }
  }

  def getAddress(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AlfResponse] = {
    addressLookupConnector.getAddress(id).map {
      case Right(addressResponse) => addressResponse
      case Left(error) => throw new Exception(s"Error returned from ALF for $id ${error.status} ${error.message} for ${hc.requestId}")
    }
  }

  def initJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[String]] = {
    addressLookupConnector.initJourney(journeyConfig)
  }

  def initJourneyAndReturnOnRampUrl(state: AddressLookupState, sdilId: String = generateId, mode: Mode)(implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages): Future[String] = {
    val journeyConfig: JourneyConfig = createJourneyConfig(state, sdilId, mode)
    initJourney(journeyConfig).map {
      case Right(onRampUrl) => onRampUrl
      case Left(error) => throw new Exception(s"Failed to init ALF ${error.message} with status ${error.status} for ${hc.requestId}")
    }
  }

  def createJourneyConfig(state: AddressLookupState, sdilId: String, mode: Mode)(implicit messages: Messages): JourneyConfig = {
    JourneyConfig(
      version = frontendAppConfig.AddressLookupConfig.version,
      options = JourneyOptions(
        continueUrl = registrationContinueUrl(state, sdilId, mode),
        homeNavHref = None,
        signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
        accessibilityFooterUrl = Some(frontendAppConfig.accessibilityFooterUrl),
        phaseFeedbackLink = None,
        deskProServiceName = None,
        showPhaseBanner = Some(false),
        alphaPhase = None,
        includeHMRCBranding = Some(true),
        ukMode = Some(true),
        selectPageConfig = Some(SelectPageConfig(
          proposalListLimit = Some(frontendAppConfig.AddressLookupConfig.selectPageConfigProposalLimit),
          showSearchAgainLink = Some(true))),
        showBackButtons = Some(true),
        disableTranslations = Some(true),
        allowedCountryCodes = None,
        confirmPageConfig = Some(ConfirmPageConfig(
          showSearchAgainLink = Some(true),
          showSubHeadingAndInfo = Some(true),
          showChangeLink = Some(true),
          showConfirmChangeText = Some(true))),
        timeoutConfig = Some(TimeoutConfig(
          timeoutAmount = frontendAppConfig.timeout,
          timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
          timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url))),
        serviceHref = Some(frontendAppConfig.sdilHomeUrl),
        pageHeadingStyle = Some("govuk-heading-l")),
      labels = returnJourneyLabels(state),
      requestedVersion = None)
  }

  private def returnJourneyLabels(state: AddressLookupState)(implicit messages: Messages): Option[JourneyLabels] = {
    state match {

      case BusinessAddress => Some(
        JourneyLabels(
          en = Some(LanguageLabels(
            appLevelLabels = Some(AppLevelLabels(
              navTitle = Some(messages("service.name")),
              phaseBannerHtml = None)),
            selectPageLabels = None,
            lookupPageLabels = Some(
              LookupPageLabels(
                title = Some(messages("addressLookupFrontend.businessAddress.lookupPageLabels.title")),
                heading = Some(messages("addressLookupFrontend.businessAddress.lookupPageLabels.title")),
                postcodeLabel = Some(messages("addressLookupFrontend.businessAddress.lookupPageLabels.postcodeLabel")))),
            editPageLabels = Some(
              EditPageLabels(
                title = Some(messages("addressLookupFrontend.businessAddress.editPageLabels.title")),
                heading = Some(messages("addressLookupFrontend.businessAddress.editPageLabels.title")),
                line1Label = Some(messages("addressLookupFrontend.businessAddress.editPageLabels.line1Label")),
                line2Label = Some(messages("addressLookupFrontend.businessAddress.editPageLabels.line2Label")),
                line3Label = Some(messages("addressLookupFrontend.businessAddress.editPageLabels.line3Label")),
                townLabel = Some(messages("addressLookupFrontend.businessAddress.editPageLabels.townLabel")),
                postcodeLabel = Some(messages("addressLookupFrontend.businessAddress.editPageLabels.postcodeLabel")))),
            confirmPageLabels = None,
            countryPickerLabels = None))))

      case PackingDetails => Some(
        JourneyLabels(
          en = Some(LanguageLabels(
            appLevelLabels = Some(AppLevelLabels(
              navTitle = Some(messages("service.name")),
              phaseBannerHtml = None)),
            selectPageLabels = None,
            lookupPageLabels = Some(
              LookupPageLabels(
                title = Some(messages("addressLookupFrontend.packingDetails.lookupPageLabels.title")),
                heading = Some(messages("addressLookupFrontend.packingDetails.lookupPageLabels.title")),
                postcodeLabel = Some(messages("addressLookupFrontend.packingDetails.lookupPageLabels.postcodeLabel")))),
            editPageLabels = Some(
              EditPageLabels(
                title = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.title")),
                heading = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.title")),
                line1Label = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.line1Label")),
                line2Label = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.line2Label")),
                line3Label = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.line3Label")),
                townLabel = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.townLabel")),
                postcodeLabel = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.postcodeLabel")),
                organisationLabel = Some(messages("addressLookupFrontend.packingDetails.editPageLabels.organisationLabel")))),
            confirmPageLabels = None,
            countryPickerLabels = None))))

      case WarehouseDetails => Some(
        JourneyLabels(
          en = Some(LanguageLabels(
            appLevelLabels = Some(AppLevelLabels(
              navTitle = Some(messages("service.name")),
              phaseBannerHtml = None)),
            selectPageLabels = None,
            lookupPageLabels = Some(
              LookupPageLabels(
                title = Some(messages("addressLookupFrontend.warehouseDetails.lookupPageLabels.title")),
                heading = Some(messages("addressLookupFrontend.warehouseDetails.lookupPageLabels.title")),
                postcodeLabel = Some(messages("addressLookupFrontend.warehouseDetails.lookupPageLabels.postcodeLabel")))),
            editPageLabels = Some(
              EditPageLabels(
                title = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.title")),
                heading = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.title")),
                line1Label = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.line1Label")),
                line2Label = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.line2Label")),
                line3Label = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.line3Label")),
                townLabel = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.townLabel")),
                postcodeLabel = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.postcodeLabel")),
                organisationLabel = Some(messages("addressLookupFrontend.warehouseDetails.editPageLabels.organisationLabel")))),
            confirmPageLabels = None,
            countryPickerLabels = None))))
    }
  }

  private def registrationContinueUrl(state: AddressLookupState, sdilId: String, mode: Mode): String = {
    state match {
      case BusinessAddress => frontendAppConfig.AddressLookupConfig.BusinessAddress.offRampUrl(sdilId, mode)
      case WarehouseDetails => frontendAppConfig.AddressLookupConfig.WarehouseDetails.offRampUrl(sdilId, mode)
      case PackingDetails => frontendAppConfig.AddressLookupConfig.PackingDetails.offRampUrl(sdilId, mode)
    }
  }
}
