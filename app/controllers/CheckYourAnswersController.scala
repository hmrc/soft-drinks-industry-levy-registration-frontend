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

import com.google.inject.Inject
import controllers.actions.{ ControllerActions, RequiredUserAnswers }
import errors.MissingRequiredUserAnswers
import handlers.ErrorHandler
import models.NormalMode
import orchestrators.RegistrationOrchestrator
import pages.CheckYourAnswersPage
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger
import views.html.CheckYourAnswersView
import views.summary.RegistrationSummary

import scala.concurrent.{ ExecutionContext, Future }

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  controllerActions: ControllerActions,
  requiredUserAnswers: RequiredUserAnswers,
  registrationOrchestrator: RegistrationOrchestrator,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView,
  errorHandler: ErrorHandler,
  val genericLogger: GenericLogger)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withUserWhoCanRegister.async {
    implicit request =>
      requiredUserAnswers.requireData(CheckYourAnswersPage) {
        registrationOrchestrator.getSubscriptionAndHowManyLitresGlobally(request.userAnswers, request.rosmWithUtr) match {
          case Right(createdSubscriptionAndAmountProducedGlobally) =>
            val summaryList = RegistrationSummary.summaryList(createdSubscriptionAndAmountProducedGlobally)
            Future.successful(Ok(view(summaryList, routes.CheckYourAnswersController.onSubmit)))
          case Left(_) => Future.successful(Redirect(routes.RegistrationController.start))
        }
      }
  }

  def onSubmit: Action[AnyContent] = controllerActions.withUserWhoCanRegister.async {
    implicit request =>
      requiredUserAnswers.requireData(CheckYourAnswersPage) {
        registrationOrchestrator.createSubscriptionAndUpdateUserAnswers.value.flatMap {
          case Right(_) => Future.successful(
            Redirect(controllers.routes.RegistrationConfirmationController.onPageLoad.url)
          )
          case Left(MissingRequiredUserAnswers) => Future.successful(
            Redirect(controllers.routes.VerifyController.onPageLoad(NormalMode))
          )
          case Left(_) =>
            genericLogger.logger.error(s"${getClass.getName} - ${request.userAnswers.id} - failed to create subscription and create user answers")
            errorHandler.internalServerErrorTemplate.map(errorView => InternalServerError(errorView))
        }
      }
  }
}

