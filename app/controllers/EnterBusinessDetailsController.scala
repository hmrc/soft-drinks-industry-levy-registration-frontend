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
import controllers.actions._
import errors.{ EnteredBusinessDetailsDoNotMatch, NoROSMRegistration }
import forms.EnterBusinessDetailsFormProvider
import handlers.ErrorHandler
import models.{ Identify, NormalMode, UserAnswers }
import navigation.Navigator
import orchestrators.RegistrationOrchestrator
import pages.EnterBusinessDetailsPage
import play.api.i18n.MessagesApi
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import services.SessionService
import utilities.GenericLogger
import views.html.EnterBusinessDetailsView

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class EnterBusinessDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  val sessionService: SessionService,
  val navigator: Navigator,
  controllerActions: ControllerActions,
  formProvider: EnterBusinessDetailsFormProvider,
  registrationOrchestrator: RegistrationOrchestrator,
  val controllerComponents: MessagesControllerComponents,
  view: EnterBusinessDetailsView,
  val errorHandler: ErrorHandler,
  val genericLogger: GenericLogger)(implicit ec: ExecutionContext, config: FrontendAppConfig) extends ControllerHelper {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiresBusinessDetailsAction.async {
    implicit request =>
      val preparedForm = request.userAnswers.get(EnterBusinessDetailsPage) match {
        case Some(value) => form.fill(value)
        case _ => form
      }
      Future.successful(Ok(view(preparedForm)))
  }

  def onSubmit: Action[AnyContent] = controllerActions.withRequiresBusinessDetailsAction.async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        identify => {
          if (sameBusinessDetailsEntered(identify, request.userAnswers)) {
            Future.successful(Redirect(navigator.nextPage(EnterBusinessDetailsPage, NormalMode, request.userAnswers)))
          } else {
            registrationOrchestrator.checkEnteredBusinessDetailsAreValidAndUpdateUserAnswers(identify, request.internalId).value flatMap {
              case Right(state) =>
                val updatedUserAnswersWithPageDataAndState = UserAnswers(request.userAnswers.id, registerState = state)
                  .set(EnterBusinessDetailsPage, identify)
                updateDatabaseAndRedirect(updatedUserAnswersWithPageDataAndState, EnterBusinessDetailsPage, NormalMode)
              case Left(error) if List(NoROSMRegistration, EnteredBusinessDetailsDoNotMatch).contains(error) =>
                genericLogger.logger.error(s"${getClass.getName} - ${request.userAnswers.id} - no rosm registration or business details don't match")
                Future.successful(BadRequest(view(form.fill(identify).withError("utr", "enterBusinessDetails.no-record.utr"))))
              case Left(error) =>
                genericLogger.logger.error(s"${getClass.getName} - ${request.userAnswers.id} - ${error.getClass.getName} error returned")
                errorHandler.internalServerErrorTemplate.map(errorView => InternalServerError(errorView))
            }
          }
        })
  }

  private def sameBusinessDetailsEntered(identify: Identify, userAnswers: UserAnswers): Boolean = {
    userAnswers.get(EnterBusinessDetailsPage).fold(false) { businessDetails =>
      val postcodesMatch = identify.postcode.replaceAll(" ", "")
        .equalsIgnoreCase(businessDetails.postcode.replaceAll(" ", ""))
      (businessDetails.utr == identify.utr) && postcodesMatch
    }
  }
}
