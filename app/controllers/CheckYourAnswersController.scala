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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, RequiredUserAnswers}
import handlers.ErrorHandler
import pages.CheckYourAnswersPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CheckYourAnswersView
import views.summary.RegistrationSummary

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            requiredUserAnswers: RequiredUserAnswers,
                                            sessionService: SessionService,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView,
                                            errorHandler: ErrorHandler
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      requiredUserAnswers.requireData(CheckYourAnswersPage) {
        val summaryList = RegistrationSummary.summaryList(request.userAnswers, request.rosmWithUtr)
        Future.successful(Ok(view(summaryList, routes.CheckYourAnswersController.onSubmit)))
      }
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      requiredUserAnswers.requireData(CheckYourAnswersPage) {
        //ToDo submit the registration request to the backend
        val submittedDateTime = Instant.now
        val updatedUserAnswers = request.userAnswers.copy(submittedOn = Some(submittedDateTime))
        sessionService.set(updatedUserAnswers).map{
          case Right(_) => Redirect(controllers.routes.RegistrationConfirmationController.onPageLoad.url)
          case Left(_) => InternalServerError(errorHandler.internalServerErrorTemplate)
        }
    }
  }
}


