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

package config

import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import models.Mode
import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate

@Singleton
class FrontendAppConfig @Inject() (servicesConfig: ServicesConfig, configuration: Configuration) {

  val registrationBaseUrl: String    = servicesConfig.baseUrl("soft-drinks-industry-levy-registration-frontend")
  val appName: String = servicesConfig.getString("appName")
  val sdilFoundingDate: LocalDate =   LocalDate.of(servicesConfig.getInt("sdilFoundingDate.year"), servicesConfig.getInt("sdilFoundingDate.month"), servicesConfig.getInt("sdilFoundingDate.day"))

  private val contactHost = servicesConfig.getString("contact-frontend.host")
  private val contactFormServiceIdentifier = "soft-drinks-industry-levy-registration"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(registrationBaseUrl + request.uri).encodedUrl}"

  val basGatewayBaseUrl: String = servicesConfig.baseUrl("bas-gateway")
  val sdilFrontendBaseUrl: String = s"${servicesConfig.baseUrl("soft-drinks-industry-levy-frontend")}/soft-drinks-industry-levy"
  val sdilBaseUrl: String = servicesConfig.baseUrl("soft-drinks-industry-levy")

  val loginUrl: String         = s"$basGatewayBaseUrl/bas-gateway/sign-in"
  val loginContinueUrl: String = s"$sdilFrontendBaseUrl/soft-drinks-industry-levy"
  val signOutUrl: String       = s"$basGatewayBaseUrl/bas-gateway/sign-out-without-state"
  val sdilHomeUrl: String      = servicesConfig.getString("sdilHomeUrl")

  val sdilAccountFrontendBaseUrl: String = servicesConfig.baseUrl("soft-drinks-industry-levy-account-frontend")
  val accountFrontendHomeUrl: String = s"$sdilAccountFrontendBaseUrl/soft-drinks-industry-levy-account-frontend/home"

  private val exitSurveyBaseUrl: String = servicesConfig.baseUrl("feedback-frontend")
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/soft-drinks-industry-levy-registration-frontend"

  val timeout: Int   = servicesConfig.getInt("timeout-dialog.timeout")
  val countdown: Int = servicesConfig.getInt("timeout-dialog.countdown")

  val cacheTtl: Int = servicesConfig.getInt("mongodb.timeToLiveInSeconds")

  val lowerBandCostPerLitre: BigDecimal = BigDecimal(servicesConfig.getString("lowerBandCostPerLitre"))
  val higherBandCostPerLitre: BigDecimal = BigDecimal(servicesConfig.getString("higherBandCostPerLitre"))

  val helpdeskPhoneNumber: String = servicesConfig.getString("helpdeskPhoneNumber").replace("-"," ")
  val addressLookupService: String  = servicesConfig.baseUrl("address-lookup-frontend")
  val addressLookUpFrontendTestEnabled: Boolean = servicesConfig.getBoolean("addressLookupFrontendTest.enabled")
  val findALostUtrLink: String = servicesConfig.getString("findALostUtrLink")
  val addressLookupOffRampUrl: String  = servicesConfig.getString("addressLookupOffRampUrl")

  object AddressLookupConfig {

    private val addressLookupInitConfig: Config = configuration
      .getOptional[Configuration](s"address-lookup-frontend-init-config")
      .getOrElse(throw new IllegalArgumentException(s"Configuration for address-lookup-frontend-init-config not found"))
      .underlying

    val version: Int = addressLookupInitConfig.getInt("version")
    val selectPageConfigProposalLimit: Int = addressLookupInitConfig.getInt("select-page-config.proposalListLimit")

    object BusinessAddress {
      def offRampUrl(sdilId: String, mode: Mode): String = {
        s"$addressLookupOffRampUrl${controllers.addressLookupFrontend.routes.RampOffController.businessAddressOffRamp(sdilId, "", mode).url.replace("?id=", "")}"
      }
    }

    object WarehouseDetails {
      def offRampUrl(sdilId: String, mode: Mode): String = {
        s"$addressLookupOffRampUrl${controllers.addressLookupFrontend.routes.RampOffController.wareHouseDetailsOffRamp(sdilId, "", mode).url.replace("?id=", "")}"
      }
    }

    object PackingDetails {
      def offRampUrl(sdilId: String, mode: Mode): String = {
        s"$addressLookupOffRampUrl${controllers.addressLookupFrontend.routes.RampOffController.packingSiteDetailsOffRamp(sdilId, "", mode).url.replace("?id=", "")}"
      }
    }

  }
}
