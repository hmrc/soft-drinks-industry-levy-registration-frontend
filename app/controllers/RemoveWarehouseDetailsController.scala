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

import controllers.actions._
import forms.RemoveWarehouseDetailsFormProvider
import handlers.ErrorHandler
import models.{Mode, UserAnswers, Warehouse}
import navigation.Navigator
import pages.RemoveWarehouseDetailsPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import services.SessionService
import utilities.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.RemoveWarehouseDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveWarehouseDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  val sessionService: SessionService,
  val navigator: Navigator,
  controllerActions: ControllerActions,
  formProvider: RemoveWarehouseDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveWarehouseDetailsView,
  val genericLogger: GenericLogger,
  val errorHandler: ErrorHandler)(implicit ec: ExecutionContext) extends ControllerHelper {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, index: String): Action[AnyContent] = controllerActions.withUserWhoCanRegister {
    implicit request =>
      request.userAnswers.warehouseList.get(index) match {
        case Some(warehouse) =>
          val formattedAddress = AddressFormattingHelper.addressFormatting(warehouse.address, warehouse.tradingName)
          Ok(view(form, mode, formattedAddress, index))
        case _ =>
          genericLogger.logger.warn(s"Warehouse index $index doesn't exist ${request.userAnswers.id} warehouse list length:" +
            s"${request.userAnswers.warehouseList.size}")
          Redirect(routes.WarehouseDetailsController.onPageLoad(mode))
      }
  }

  def onSubmit(mode: Mode, index: String): Action[AnyContent] = controllerActions.withUserWhoCanRegister.async {
    implicit request =>
      val warehouseToRemove: Option[Warehouse] = request.userAnswers.warehouseList.get(index)
      warehouseToRemove match {
        case None =>
          genericLogger.logger.warn(s"Warehouse index $index doesn't exist ${request.userAnswers.id} warehouse list length:" +
            s"${request.userAnswers.warehouseList.size}")
          Future.successful(Redirect(routes.WarehouseDetailsController.onPageLoad(mode)))
        case Some(warehouse) =>
          val formattedAddress: Html = AddressFormattingHelper.addressFormatting(warehouse.address, warehouse.tradingName)
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, formattedAddress, index))),
            value => {
              val updatedAnswersFinal: UserAnswers = if (value) {
                request.userAnswers.copy(warehouseList = request.userAnswers.warehouseList.removed(index))
              } else {
                request.userAnswers
              }
              updateDatabaseAndRedirect(updatedAnswersFinal, RemoveWarehouseDetailsPage, mode)
            })
      }
  }
}
