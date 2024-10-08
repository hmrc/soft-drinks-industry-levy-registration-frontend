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
import models.{ Mode, UserAnswers }
import navigation.Navigator
import pages.Page
import play.api.i18n.I18nSupport
import play.api.mvc.{ AnyContent, Request, Result }
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utilities.GenericLogger

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

trait ControllerHelper extends FrontendBaseController with I18nSupport {

  val sessionService: SessionService
  val navigator: Navigator
  val errorHandler: ErrorHandler
  val genericLogger: GenericLogger
  private val internalServerErrorBaseMessage = "Failed to set value in session repository"
  private def sessionRepo500ErrorMessage(page: Page): String = s"$internalServerErrorBaseMessage while attempting set on ${page.toString}"

  def updateDatabaseAndRedirect(updatedAnswers: Try[UserAnswers], page: Page, mode: Mode, previousAnswer: Option[String] = None)(implicit ec: ExecutionContext, request: Request[AnyContent]): Future[Result] = {
    updatedAnswers match {
      case Failure(_) =>
        genericLogger.logger.error(s"Failed to resolve user answers while on ${page.toString}")
        errorHandler.internalServerErrorTemplate.map(errorView => InternalServerError(errorView))
      case Success(answers) => sessionService.set(answers).value.flatMap {
        case Right(_) => Future.successful(
          Redirect(navigator.nextPage(page, mode, answers, previousAnswer))
        )
        case Left(_) =>
          genericLogger.logger.error(sessionRepo500ErrorMessage(page))
          errorHandler.internalServerErrorTemplate.map(errorView => InternalServerError(errorView))
      }
    }
  }

  def updateDatabaseWithoutRedirect(updatedAnswers: UserAnswers, page: Page)(implicit ec: ExecutionContext): Future[Status] = {
    sessionService.set(updatedAnswers).value.map {
      case Right(_) => Ok
      case Left(_) =>
        genericLogger.logger.error(sessionRepo500ErrorMessage(page))
        InternalServerError
    }
  }

  def updateDatabaseAndRedirect(updatedAnswers: UserAnswers, page: Page, mode: Mode)(implicit ec: ExecutionContext, request: Request[AnyContent]): Future[Result] = {
    sessionService.set(updatedAnswers).value.flatMap {
      case Right(_) => Future.successful(Redirect(navigator.nextPage(page, mode, updatedAnswers)))
      case Left(_) =>
        genericLogger.logger.error(sessionRepo500ErrorMessage(page))
        errorHandler.internalServerErrorTemplate.map(errorView => InternalServerError(errorView))
    }
  }
  def updateDatabaseWithoutRedirect(updatedAnswers: Try[UserAnswers], page: Page)(success: Future[Result])(implicit ec: ExecutionContext, request: Request[AnyContent]): Future[Result] = {
    updatedAnswers match {
      case Failure(_) =>
        genericLogger.logger.error(s"Failed to resolve user answers while on ${page.toString}")
        errorHandler.internalServerErrorTemplate.map(errorView => InternalServerError(errorView))
      case Success(userAnswers) =>
        sessionService.set(userAnswers).value.flatMap {
          case Right(_) => success
          case Left(_) =>
            genericLogger.logger.error(sessionRepo500ErrorMessage(page))
            Future.successful(InternalServerError)
        }
    }
  }

}
