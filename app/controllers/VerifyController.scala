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
import forms.VerifyFormProvider
import handlers.ErrorHandler
import models.Verify.{No, YesNewAddress}
import models.{Mode, RosmRegistration}
import navigation.Navigator
import pages.VerifyPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AddressLookupState.BusinessAddress
import services.{AddressLookupService, SessionService}
import utilities.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.VerifyView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VerifyController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: Navigator,
                                       controllerActions: ControllerActions,
                                       formProvider: VerifyFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: VerifyView,
                                       val errorHandler: ErrorHandler,
                                       val genericLogger: GenericLogger,
                                       addressLookupService: AddressLookupService
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  private val formattedAddress = (rosmRegistration: RosmRegistration) =>
    AddressFormattingHelper.formatBusinessAddress(rosmRegistration.address, Some(rosmRegistration.organisationName))

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withUserWhoCanRegister {
    implicit request =>
      val preparedForm = request.userAnswers.get(VerifyPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, request.rosmWithUtr.utr, formattedAddress(request.rosmWithUtr.rosmRegistration)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withUserWhoCanRegister.async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, request.rosmWithUtr.utr, formattedAddress(request.rosmWithUtr.rosmRegistration)))),
        value => {
          val updatedAnswers = request.userAnswers.set(VerifyPage, value)
          value match {
            case YesNewAddress =>
              updateDatabaseWithoutRedirect(updatedAnswers, VerifyPage)(
                addressLookupService.initJourneyAndReturnOnRampUrl(BusinessAddress, mode = mode)
                .map(alfOnRamp => Redirect(alfOnRamp)))
            case No => Future.successful(Redirect(auth.routes.AuthController.signOutNoSurvey))
            case _ => updateDatabaseAndRedirect (updatedAnswers.map(_.copy(address = Some(request.rosmWithUtr.rosmRegistration.address))), VerifyPage, mode)
          }
        }
      )
  }
}
