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
import models.NormalMode
import pages.ContactDetailsPage

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RegistrationConfirmationView
import views.summary.RegistrationSummary

import java.time.{LocalDateTime, ZoneId}

class RegistrationConfirmationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       controllerActions: ControllerActions,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: RegistrationConfirmationView
                                     )(implicit config: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withUserWhoCanRegister {
    implicit request =>
      (request.userAnswers.submittedOn, request.userAnswers.get(ContactDetailsPage)) match {
        case (Some(dtInstant), Some(contactDetails)) =>
          val sentDateTime = LocalDateTime.ofInstant(dtInstant, ZoneId.of("UTC"))
          val summaryList = RegistrationSummary.summaryList(request.userAnswers, request.rosmWithUtr, false)
          Ok(view(
            summaryList,
            sentDateTime,
            request.rosmWithUtr.rosmRegistration.organisationName,
            contactDetails.email
          ))
        case _ =>
          Redirect(routes.VerifyController.onPageLoad(NormalMode).url)
      }
  }
}
