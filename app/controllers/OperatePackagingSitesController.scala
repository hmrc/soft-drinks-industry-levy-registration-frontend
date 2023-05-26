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
import forms.OperatePackagingSitesFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.OperatePackagingSitesPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.OperatePackagingSitesView
import handlers.ErrorHandler

import scala.concurrent.{ExecutionContext, Future}
import utilities.GenericLogger

class OperatePackagingSitesController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: OperatePackagingSitesFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: OperatePackagingSitesView,
                                       val errorHandler: ErrorHandler,
                                       val genericLogger: GenericLogger
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(OperatePackagingSitesPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value => {
          val updatedAnswers = request.userAnswers.set(OperatePackagingSitesPage, value)
          updateDatabaseAndRedirect(updatedAnswers, OperatePackagingSitesPage, mode)
        }
      )
  }
}
