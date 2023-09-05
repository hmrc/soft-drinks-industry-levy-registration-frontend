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
import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import errors.AuthenticationError
import handlers.ErrorHandler
import models.NormalMode
import models.RegisterState._
import orchestrators.RegistrationOrchestrator
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger

import scala.concurrent.ExecutionContext

class RegistrationController @Inject()(identify: IdentifierAction,
                                       registrationOrchestrator: RegistrationOrchestrator,
                                       val controllerComponents: MessagesControllerComponents,
                                       errorHandler: ErrorHandler,
                                       val genericLogger: GenericLogger,
                                       config: FrontendAppConfig
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def start: Action[AnyContent] = identify.async {
    implicit request =>
      registrationOrchestrator.handleRegistrationRequest.value.map{
        case Right(RegistrationPending) => Redirect(routes.RegistrationPendingController.onPageLoad)
        case Right(RequiresBusinessDetails) => Redirect(routes.EnterBusinessDetailsController.onPageLoad)
        case Right(AlreadyRegistered) => Redirect(routes.AlreadyRegisteredController.onPageLoad)
        case Right(RegisterApplicationAccepted) => Redirect(routes.ApplicationAlreadySubmittedController.onPageLoad)
        case Right(_) => Redirect(routes.VerifyController.onPageLoad(NormalMode))
        case Left(AuthenticationError) => Redirect(config.loginUrl, Map("continue_url" -> Seq(config.sdilHomeUrl), "origin" -> Seq(config.appName)))
        case Left(error) =>
          genericLogger.logger.error(s"${getClass.getName} - $error while handling registration request")
          InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }
}
