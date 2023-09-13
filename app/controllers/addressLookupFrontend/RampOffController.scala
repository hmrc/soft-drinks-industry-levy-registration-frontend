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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.{Mode, NormalMode}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AddressLookupService
import services.AddressLookupState._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.annotation.unused
import scala.concurrent.ExecutionContext

class RampOffController @Inject()(identify: IdentifierAction,
                                  getData: DataRetrievalAction,
                                  requireData: DataRequiredAction,
                                  addressLookupService: AddressLookupService,
                                  sessionRepository: SessionRepository,
                                  val controllerComponents: MessagesControllerComponents)
                                 (implicit val ex: ExecutionContext) extends FrontendBaseController {

  def businessAddressOffRamp(@unused sdilId: String, alfId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        alfResponse         <- addressLookupService.getAddress(alfId)
        ukAddress           = addressLookupService.addressChecker(alfResponse.address, alfId)
        updatedUserAnswers = request.userAnswers.setBusinessAddress(ukAddress)
        _                   <- sessionRepository.set(updatedUserAnswers)
      } yield {
        val redirectUrl = if(mode == NormalMode) {
          controllers.routes.OrganisationTypeController.onPageLoad(NormalMode)
        } else {
          controllers.routes.CheckYourAnswersController.onPageLoad
        }
        Redirect(redirectUrl)
      }
  }

  def wareHouseDetailsOffRamp(sdilId: String, alfId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val userAnswers = request.userAnswers
      for {
        alfResponse         <- addressLookupService.getAddress(alfId)
        ukAddress           = addressLookupService.addressChecker(alfResponse.address, alfId)
        optTradingName      = alfResponse.address.organisation
        updatedUserAnswers = optTradingName match {
          case Some(tradingName) => userAnswers.addWarehouse(ukAddress, tradingName, sdilId)
          case None => userAnswers.setAlfResponse(ukAddress, WarehouseDetails)
        }
        _                   <- sessionRepository.set(updatedUserAnswers)
      } yield {
        if(optTradingName.nonEmpty) {
          Redirect(controllers.routes.WarehouseDetailsController.onPageLoad(mode))
        } else {
          //ToDo redirect to new page
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
  }

  def packingSiteDetailsOffRamp(sdilId: String, alfId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val userAnswers = request.userAnswers
      for {
        alfResponse <- addressLookupService.getAddress(alfId)
        ukAddress = addressLookupService.addressChecker(alfResponse.address, alfId)
        optTradingName = alfResponse.address.organisation
        updatedUserAnswers = optTradingName match {
          case Some(tradingName) => userAnswers.addPackagingSite(ukAddress, tradingName, sdilId)
          case None => userAnswers.setAlfResponse(ukAddress, PackingDetails)
        }
        _ <- sessionRepository.set(updatedUserAnswers)
      } yield {
        if (optTradingName.nonEmpty) {
          Redirect(controllers.routes.PackagingSiteDetailsController.onPageLoad(mode))
        } else {
          //ToDo redirect to new page
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
  }
}
