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
import forms.RemovePackagingSiteDetailsFormProvider
import handlers.ErrorHandler
import models.{ Mode, UserAnswers }
import navigation.Navigator
import pages.RemovePackagingSiteDetailsPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import play.twirl.api.Html
import services.SessionService
import utilities.GenericLogger
import viewmodels.AddressFormattingHelper
import views.html.RemovePackagingSiteDetailsView

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class RemovePackagingSiteDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  val sessionService: SessionService,
  val navigator: Navigator,
  controllerActions: ControllerActions,
  formProvider: RemovePackagingSiteDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemovePackagingSiteDetailsView,
  val errorHandler: ErrorHandler,
  val genericLogger: GenericLogger)(implicit ec: ExecutionContext) extends ControllerHelper {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, ref: String): Action[AnyContent] = controllerActions.withUserWhoCanRegister {
    implicit request =>
      if (request.userAnswers.packagingSiteList.size > 1) {
        getPackagingSiteAddressBaseOnRef(ref, request.userAnswers) match {
          case None =>
            genericLogger.logger.warn(s"user has potentially hit page and ref does not exist for packaging site" +
              s"$ref ${request.userAnswers.id} amount currently: ${request.userAnswers.packagingSiteList.size}")
            Redirect(routes.PackagingSiteDetailsController.onPageLoad(mode))
          case Some(packagingSiteDetails) =>
            Ok(view(form, mode, ref, packagingSiteDetails))
        }
      } else {
        genericLogger.logger.info(
          s"User at ${RemovePackagingSiteDetailsPage.toString} with 1 or less sites in packaging site list. Redirected to PackagingSiteDetails")
        Redirect(routes.PackagingSiteDetailsController.onPageLoad(mode))
      }
  }

  private def getPackagingSiteAddressBaseOnRef(ref: String, userAnswers: UserAnswers): Option[Html] = {
    userAnswers.packagingSiteList
      .get(ref)
      .map(packagingSite => AddressFormattingHelper.addressFormatting(packagingSite.address, packagingSite.tradingName))
  }

  def onSubmit(mode: Mode, ref: String): Action[AnyContent] = controllerActions.withUserWhoCanRegister.async {
    implicit request =>
      def removePackagingDetailsFromUserAnswers(userSelection: Boolean, userAnswers: UserAnswers, refOfSite: String): UserAnswers = {
        if (userSelection) {
          userAnswers.copy(packagingSiteList = userAnswers.packagingSiteList.filterNot(_._1 == refOfSite))
        } else {
          userAnswers
        }
      }

      getPackagingSiteAddressBaseOnRef(ref, request.userAnswers) match {
        case None =>
          genericLogger.logger.warn(s"user has potentially submit page and ref does not exist for packaging site" +
            s"$ref ${request.userAnswers.id} amount currently: ${request.userAnswers.packagingSiteList.size}")
          Future.successful(Redirect(routes.PackagingSiteDetailsController.onPageLoad(mode)))
        case Some(packagingSiteDetails) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, ref, packagingSiteDetails))),

            value => {
              val updatedAnswersAfterUserAnswer = removePackagingDetailsFromUserAnswers(value, request.userAnswers, ref)
              updateDatabaseAndRedirect(updatedAnswersAfterUserAnswer, RemovePackagingSiteDetailsPage, mode)
            })
      }
  }
}
