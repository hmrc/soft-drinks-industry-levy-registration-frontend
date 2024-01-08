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
import forms.OrganisationTypeFormProvider
import handlers.ErrorHandler
import models.Mode
import navigation.Navigator
import pages.OrganisationTypePage
import play.api.i18n.MessagesApi
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import services.SessionService
import utilities.GenericLogger
import views.html.OrganisationTypeView

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class OrganisationTypeController @Inject() (
  override val messagesApi: MessagesApi,
  val sessionService: SessionService,
  val navigator: Navigator,
  controllerActions: ControllerActions,
  formProvider: OrganisationTypeFormProvider,
  val genericLogger: GenericLogger,
  val controllerComponents: MessagesControllerComponents,
  view: OrganisationTypeView,
  val errorHandler: ErrorHandler)(implicit ec: ExecutionContext) extends ControllerHelper {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withUserWhoCanRegister {
    implicit request =>
      val withoutSoleTrader: Boolean = if (request.hasCTEnrolment) true else false
      val preparedForm = request.userAnswers.get(OrganisationTypePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, withoutSoleTrader))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withUserWhoCanRegister.async {
    implicit request =>
      val withoutSoleTrader: Boolean = if (request.hasCTEnrolment) true else false
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, withoutSoleTrader))),

        value => {
          val updatedAnswers = request.userAnswers.set(OrganisationTypePage, value)
          updateDatabaseAndRedirect(updatedAnswers, OrganisationTypePage, mode)
        })
  }
}
