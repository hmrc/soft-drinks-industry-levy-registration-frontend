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
import models.backend.Site
import models.{Mode, RosmRegistration}
import navigation.Navigator
import pages.PackAtBusinessAddressPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AddressLookupState.PackingDetails
import services.{AddressLookupService, SessionService}
import utilities.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.PackAtBusinessAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackAtBusinessAddressController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: Navigator,
                                       controllerActions: ControllerActions,
                                       formProvider: PackAtBusinessAddressFormProvider,
                                       addressLookupService: AddressLookupService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: PackAtBusinessAddressView,
                                       val errorHandler: ErrorHandler,
                                       val genericLogger: GenericLogger
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  private val formattedAddress = (rosmRegistration: RosmRegistration) =>
    AddressFormattingHelper.formatBusinessAddress(rosmRegistration.address, Some(rosmRegistration.organisationName))

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withUserWhoCanRegister {
    implicit request =>

      val preparedForm = request.userAnswers.get(PackAtBusinessAddressPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, formattedAddress(request.rosmWithUtr.rosmRegistration), mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withUserWhoCanRegister.async {
    implicit request =>

      val rosmReg = request.rosmWithUtr.rosmRegistration

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, formattedAddress(request.rosmWithUtr.rosmRegistration), mode))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PackAtBusinessAddressPage, value))
            _              <- updateDatabaseWithoutRedirect(updatedAnswers, PackAtBusinessAddressPage)
            onwardUrl              <- if(value){
              updateDatabaseWithoutRedirect(updatedAnswers.copy(packagingSiteList = updatedAnswers.packagingSiteList ++ Map("1" ->
                Site(
                  address = rosmReg.address,
                  ref = None,
                  tradingName = rosmReg.organisationName,
                  closureDate = None
                )
              )), PackAtBusinessAddressPage).flatMap(_ =>
                Future.successful(routes.PackagingSiteDetailsController.onPageLoad(mode).url))
            }else {
              addressLookupService.initJourneyAndReturnOnRampUrl(PackingDetails, mode = mode)
            }
          }yield Redirect(onwardUrl)
        }
      )
  }
}
