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
import forms.AskSecondaryWarehousesFormProvider
import handlers.ErrorHandler
import models.requests.DataRequest
import models.{ CheckMode, Mode, UserAnswers }
import navigation.Navigator
import pages.AskSecondaryWarehousesPage
import play.api.i18n.{ Messages, MessagesApi }
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import services.AddressLookupState.WarehouseDetails
import services.{ AddressLookupService, SessionService }
import uk.gov.hmrc.http.HeaderCarrier
import utilities.GenericLogger
import views.html.AskSecondaryWarehousesView

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class AskSecondaryWarehousesController @Inject() (
  override val messagesApi: MessagesApi,
  val sessionService: SessionService,
  val navigator: Navigator,
  controllerActions: ControllerActions,
  formProvider: AskSecondaryWarehousesFormProvider,
  addressLookupService: AddressLookupService,
  val controllerComponents: MessagesControllerComponents,
  view: AskSecondaryWarehousesView,
  val errorHandler: ErrorHandler,
  val genericLogger: GenericLogger)(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withUserWhoCanRegister {
    implicit request =>

      val preparedForm = request.userAnswers.get(AskSecondaryWarehousesPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withUserWhoCanRegister.async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AskSecondaryWarehousesPage, value))
            onwardUrl <- getOnwardUrl(request, value, mode, updatedAnswers)
          } yield Redirect(onwardUrl)
        })
  }

  def getOnwardUrl(request: DataRequest[AnyContent], userAnsweredYes: Boolean, mode: Mode, updatedAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages) = {

    val hasPreviousYesAnswer = request.userAnswers.get(AskSecondaryWarehousesPage).contains(true)

    if ((hasPreviousYesAnswer && request.userAnswers.warehouseList.nonEmpty) && userAnsweredYes) {
      Future.successful(routes.WarehouseDetailsController.onPageLoad(mode).url)
    } else if (userAnsweredYes) {
      updateDatabaseWithoutRedirect(updatedAnswers, AskSecondaryWarehousesPage).flatMap(
        _ => addressLookupService.initJourneyAndReturnOnRampUrl(WarehouseDetails, mode = mode))
    } else {
      updateDatabaseWithoutRedirect(updatedAnswers.copy(warehouseList = Map.empty), AskSecondaryWarehousesPage).flatMap(_ =>
        mode match {
          case CheckMode => Future.successful(routes.CheckYourAnswersController.onPageLoad.url)
          case _ => Future.successful(routes.ContactDetailsController.onPageLoad(mode).url)
        })
    }
  }

}
