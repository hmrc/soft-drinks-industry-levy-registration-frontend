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

import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions._
import errors.NoROSMRegistration
import forms.EnterBusinessDetailsFormProvider
import handlers.ErrorHandler
import models.backend.UkAddress
import models.{Identify, Mode, NormalMode, RegisterState, UserAnswers}
import navigation.Navigator
import pages.EnterBusinessDetailsPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.EnterBusinessDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnterBusinessDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: Navigator,
                                       controllerActions: ControllerActions,
                                       formProvider: EnterBusinessDetailsFormProvider,
                                       softDrinksIndustryLevyConnector: SoftDrinksIndustryLevyConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: EnterBusinessDetailsView,
                                       val errorHandler: ErrorHandler,
                                       val genericLogger: GenericLogger
                                     )(implicit ec: ExecutionContext, config: FrontendAppConfig) extends ControllerHelper {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiresBusinessDetailsAction.async {
    implicit request =>
      val preparedForm = request.userAnswers.get(EnterBusinessDetailsPage) match {
        case Some(value) => form.fill(value)
        case _ => form
      }
      Future.successful(Ok(view(preparedForm, mode)))
  }

  private def postcodesMatch(rosmAddress: UkAddress, identify: Identify) =
    rosmAddress.postCode.replaceAll(" ", "").equalsIgnoreCase(identify.postcode.replaceAll(" ", ""))

  private def wipeUserDetailsIfDifferentIdentifer(identify: Identify, userAnswers: UserAnswers): UserAnswers = {
    val userAnswersChanged: Boolean = !userAnswers.get(EnterBusinessDetailsPage).contains(identify)
    userAnswersChanged match {
      case true => new UserAnswers(userAnswers.id, registerState = RegisterState.RegisterWithOtherUTR)
      case false => userAnswers.copy(registerState = RegisterState.RegisterWithOtherUTR)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiresBusinessDetailsAction.async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        identify => {
          softDrinksIndustryLevyConnector.retreiveRosmSubscription(identify.utr, request.internalId).value flatMap {
            case Right(rosmReg) if postcodesMatch(rosmReg.rosmRegistration.address, identify) =>
              val updatedAnswers = wipeUserDetailsIfDifferentIdentifer(identify, request.userAnswers).set(EnterBusinessDetailsPage, identify)
              updateDatabaseAndRedirect(updatedAnswers, EnterBusinessDetailsPage, mode)
            case Right(_) => Future.successful(BadRequest(view(form.fill(identify).withError("utr", "enterBusinessDetails.no-record.utr"), NormalMode)))
            case Left(NoROSMRegistration) => Future.successful(BadRequest(view(form.fill(identify).withError("utr", "enterBusinessDetails.no-record.utr"), NormalMode)))
            case Left(_) => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
          }
        }
      )
  }
}
