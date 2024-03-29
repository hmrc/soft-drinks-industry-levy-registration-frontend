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
import forms.WarehousesTradingNameFormProvider

import javax.inject.Inject
import models.{ Mode, WarehousesTradingName }
import navigation.Navigator
import pages.WarehousesTradingNamePage
import play.api.i18n.MessagesApi
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import services.SessionService
import views.html.WarehousesTradingNameView
import handlers.ErrorHandler
import play.api.data.Form
import services.AddressLookupState.WarehouseDetails

import scala.concurrent.{ ExecutionContext, Future }
import utilities.GenericLogger

class WarehousesTradingNameController @Inject() (
  override val messagesApi: MessagesApi,
  val sessionService: SessionService,
  val navigator: Navigator,
  controllerActions: ControllerActions,
  formProvider: WarehousesTradingNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WarehousesTradingNameView,
  val errorHandler: ErrorHandler,
  val genericLogger: GenericLogger)(implicit ec: ExecutionContext) extends ControllerHelper {

  val form: Form[WarehousesTradingName] = formProvider()

  def onPageLoad(mode: Mode, ref: String): Action[AnyContent] = controllerActions
    .withUserWhoCanEnterTradingName(WarehouseDetails, ref, mode) { implicit request =>
      val preparedForm = request.tradingName match {
        case None => form
        case Some(value) => form.fill(WarehousesTradingName(value))
      }

      Ok(view(preparedForm, mode, ref))
    }

  def onSubmit(mode: Mode, ref: String): Action[AnyContent] = controllerActions
    .withUserWhoCanEnterTradingName(WarehouseDetails, ref, mode).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, ref))),

        value => {
          val updatedAnswers = request.userAnswers
            .addWarehouse(request.aflAddress, value.warehouseTradingName, ref)
          updateDatabaseAndRedirect(updatedAnswers, WarehousesTradingNamePage, mode)
        })
    }
}
