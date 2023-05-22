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

import handlers.ErrorHandler
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.Page
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Request, Result}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait ControllerHelper extends FrontendBaseController with I18nSupport {

  val sessionService: SessionService
  val navigator: Navigator
  val errorHandler: ErrorHandler

  def updateDatabaseAndRedirect(updatedAnswers: Try[UserAnswers], page: Page, mode: Mode)
                               (implicit ec: ExecutionContext, request: Request[AnyContent]): Future[Result] = {
    updatedAnswers match {
      case Failure(_) => Future.successful(
        InternalServerError(errorHandler.internalServerErrorTemplate)
      )
      case Success(answers) => sessionService.set(answers).map {
        case Right(_) => Redirect(navigator.nextPage(page, mode, answers))
        case Left(_) => InternalServerError(errorHandler.internalServerErrorTemplate)
      }
    }
  }

  def updateDatabaseAndRedirect(updatedAnswers: UserAnswers, page: Page, mode: Mode)
                               (implicit ec: ExecutionContext, request: Request[AnyContent]): Future[Result] = {
    sessionService.set(updatedAnswers).map {
      case Right(_) => Redirect(navigator.nextPage(page, mode, updatedAnswers))
      case Left(_) => InternalServerError(errorHandler.internalServerErrorTemplate)
    }
  }

}
