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
import models.CreatedSubscriptionAndAmountProducedGlobally
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{SDILSessionCache, SDILSessionKeys}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RegistrationConfirmationView
import views.summary.RegistrationSummary

import java.time.{LocalDateTime, ZoneId}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RegistrationConfirmationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       controllerActions: ControllerActions,
                                       sdilSessionCache: SDILSessionCache,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: RegistrationConfirmationView
                                     )(implicit config: FrontendAppConfig, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withUserWhoCanRegister.async {
    implicit request => {
      sdilSessionCache.fetchEntry[CreatedSubscriptionAndAmountProducedGlobally](
        request.internalId, SDILSessionKeys.CREATED_SUBSCRIPTION_AND_AMOUNT_PRODUCED_GLOBALLY).map{
        case Some(createdSubscriptionAndAmountProducedGlobally) if request.userAnswers.submittedOn.isDefined =>
          val sentDateTime = LocalDateTime.ofInstant(request.userAnswers.submittedOn.get, ZoneId.of("UTC"))
          val summaryList = RegistrationSummary.summaryList(createdSubscriptionAndAmountProducedGlobally, false)
          Ok(view(
            summaryList,
            sentDateTime,
            request.rosmWithUtr.rosmRegistration.organisationName,
            createdSubscriptionAndAmountProducedGlobally.subscription.contact.email
          ))
        case _ => Redirect(routes.RegistrationController.start)
      }
    }
  }
}
