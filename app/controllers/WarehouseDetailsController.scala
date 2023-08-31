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
import forms.WarehouseDetailsFormProvider
import handlers.ErrorHandler
import models.{Mode, NormalMode, Warehouse}
import navigation.Navigator
import pages.{AskSecondaryWarehousesPage, WarehouseDetailsPage}
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AddressLookupService, SessionService, WarehouseDetails}
import utilities.GenericLogger
import viewmodels.govuk.SummaryListFluency
import views.html.WarehouseDetailsView
import views.summary.WarehouseDetailsSummary

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WarehouseDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: Navigator,
                                       controllerActions: ControllerActions,
                                       warehouseDetailsChecker: WarehouseDetailsChecker,
                                       formProvider: WarehouseDetailsFormProvider,
                                       addressLookupService: AddressLookupService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: WarehouseDetailsView,
                                       val errorHandler: ErrorHandler,
                                       val genericLogger: GenericLogger
                                     )(implicit ec: ExecutionContext) extends ControllerHelper with SummaryListFluency {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withUserWhoCanRegister {
    implicit request =>

      warehouseDetailsChecker.checkWarehouseDetails(request.userAnswers, mode) {
        val preparedForm = request.userAnswers.get(WarehouseDetailsPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        val warehouses = request.userAnswers.warehouseList

        Ok(view(preparedForm, mode, createWarehouseSummary(warehouses, mode), warehouses.size))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withUserWhoCanRegister.async {
    implicit request =>

      val warehouses = request.userAnswers.warehouseList

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, createWarehouseSummary(warehouses, mode), warehouses.size))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WarehouseDetailsPage, value))
            _              <- updateDatabaseWithoutRedirect(updatedAnswers, WarehouseDetailsPage)
            onwardUrl              <- if(value){
              addressLookupService.initJourneyAndReturnOnRampUrl(WarehouseDetails, mode = mode)
            }else if(mode == NormalMode){
              Future.successful(routes.ContactDetailsController.onPageLoad(mode).url)
            } else {
              Future.successful(routes.CheckYourAnswersController.onPageLoad.url)
            }
          } yield Redirect(onwardUrl)
        }
      )
  }

  private def createWarehouseSummary(warehouses: Map[String, Warehouse], mode: Mode)(implicit messages:Messages) = {
    Some(SummaryListViewModel(rows = WarehouseDetailsSummary.warehouseDetailsRow(warehouses, mode)))
  }
}
