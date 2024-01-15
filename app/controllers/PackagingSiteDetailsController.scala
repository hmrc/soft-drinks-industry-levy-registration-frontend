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
import forms.PackagingSiteDetailsFormProvider
import handlers.ErrorHandler
import models.{ Mode, NormalMode }
import navigation.Navigator
import pages.PackagingSiteDetailsPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import services.AddressLookupState.PackingDetails
import services.{ AddressLookupService, SessionService }
import utilities.GenericLogger
import views.html.PackagingSiteDetailsView

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class PackagingSiteDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  val sessionService: SessionService,
  val navigator: Navigator,
  controllerActions: ControllerActions,
  formProvider: PackagingSiteDetailsFormProvider,
  addressLookupService: AddressLookupService,
  val controllerComponents: MessagesControllerComponents,
  view: PackagingSiteDetailsView,
  val errorHandler: ErrorHandler,
  val genericLogger: GenericLogger)(implicit ec: ExecutionContext) extends ControllerHelper {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withUserWhoCanRegister {
    implicit request =>

      if (request.userAnswers.packagingSiteList.nonEmpty) {
        Ok(view(form, mode, request.userAnswers.packagingSiteList))
      } else {
        genericLogger.logger.info(s"User at ${PackagingSiteDetailsPage.toString} with an empty packaging site list.  Redirected to Pack at business address")
        Redirect(routes.PackAtBusinessAddressController.onPageLoad(mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withUserWhoCanRegister.async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, request.userAnswers.packagingSiteList))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PackagingSiteDetailsPage, value))
            _ <- updateDatabaseWithoutRedirect(updatedAnswers, PackagingSiteDetailsPage)
            onwardUrl <- if (value) {
              addressLookupService.initJourneyAndReturnOnRampUrl(PackingDetails, mode = mode)
            } else if (mode == NormalMode) {
              Future.successful(routes.AskSecondaryWarehousesController.onPageLoad(mode).url)
            } else {
              Future.successful(routes.CheckYourAnswersController.onPageLoad.url)
            }
          } yield Redirect(onwardUrl)
        })
  }
}
