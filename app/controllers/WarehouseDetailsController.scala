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

import javax.inject.Inject
import models.{Mode, Warehouse}
import navigation.Navigator
import pages.WarehouseDetailsPage
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.WarehouseDetailsView
import handlers.ErrorHandler
import models.backend.UkAddress
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.{ExecutionContext, Future}
import utilities.GenericLogger
import viewmodels.summary.WarehouseDetailsSummary

class WarehouseDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       warehouseDetailsChecker: WarehouseDetailsChecker,
                                       formProvider: WarehouseDetailsFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: WarehouseDetailsView,
                                       val errorHandler: ErrorHandler,
                                       val genericLogger: GenericLogger
                                     )(implicit ec: ExecutionContext) extends ControllerHelper with SummaryListFluency {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      warehouseDetailsChecker.checkWarehouseDetails(request.userAnswers) {
        val preparedForm = request.userAnswers.get(WarehouseDetailsPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        val warehouses = request.userAnswers.warehouseList

        Ok(view(preparedForm, mode, createWarehouseSummary(warehouses), warehouses.size))
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val warehouses = request.userAnswers.warehouseList

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, createWarehouseSummary(warehouses), warehouses.size))),

        value => {
          val updatedAnswers = request.userAnswers.set(WarehouseDetailsPage, value)
          updateDatabaseAndRedirect(updatedAnswers, WarehouseDetailsPage, mode)
        }
      )
  }

  private def createWarehouseSummary(warehouses: Map[String, Warehouse])(implicit messages:Messages) = {
    Some(SummaryListViewModel(rows = WarehouseDetailsSummary.warehouseDetailsRow(warehouses)))
  }
}
