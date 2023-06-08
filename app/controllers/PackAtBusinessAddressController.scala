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
import forms.PackAtBusinessAddressFormProvider
import handlers.ErrorHandler
import models.Mode
import navigation.Navigator
import pages.PackAtBusinessAddressPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.PackAtBusinessAddressView
import viewmodels.AddressFormattingHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackAtBusinessAddressController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: PackAtBusinessAddressFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: PackAtBusinessAddressView,
                                       val errorHandler: ErrorHandler,
                                       val genericLogger: GenericLogger
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(PackAtBusinessAddressPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val formattedAddress =  AddressFormattingHelper.formatBusinessAddress(request.rosmRegistration.address, request.rosmRegistration.organisation.map(name => name.organisationName))

      Ok(view(preparedForm, formattedAddress , mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val formattedAddress =  AddressFormattingHelper.formatBusinessAddress(request.rosmRegistration.address, request.rosmRegistration.organisation.map(name => name.organisationName))

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, formattedAddress, mode))),

        value => {
          val updatedAnswers = request.userAnswers.set(PackAtBusinessAddressPage, value)
          updateDatabaseAndRedirect(updatedAnswers, PackAtBusinessAddressPage, mode)
        }
      )
  }
}
