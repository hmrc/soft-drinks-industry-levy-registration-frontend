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

import connectors.SoftDrinksIndustryLevyConnector
import controllers.actions._
import forms.EnterBusinessDetailsFormProvider
import handlers.ErrorHandler
import models.backend.UkAddress
import models.{CheckMode, Identification, Mode, NormalMode, UserAnswers}
import navigation.Navigator
import pages.EnterBusinessDetailsPage
import play.api.i18n.MessagesApi
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utilities.GenericLogger
import views.html.EnterBusinessDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnterBusinessDetailsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       formProvider: EnterBusinessDetailsFormProvider,
                                       softDrinksIndustryLevyConnector: SoftDrinksIndustryLevyConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: EnterBusinessDetailsView,
                                       val errorHandler: ErrorHandler,
                                       val genericLogger: GenericLogger
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      val preparedForm = request.userAnswers.fold[Form[Identification]](form) { ua =>
        ua.get(EnterBusinessDetailsPage) match {
          case Some(value) if mode == CheckMode => form.fill(value)
          case _ => form
        }
      }
      Future.successful(Ok(view(preparedForm, mode)))
  }

  private def postcodesMatch(rosmAddress: UkAddress, identification: Identification) =
    rosmAddress.postCode.replaceAll(" ", "").equalsIgnoreCase(identification.postcode.replaceAll(" ", ""))


  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      val answers = request.userAnswers.getOrElse(UserAnswers(id = request.internalId))

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        identification => {
          softDrinksIndustryLevyConnector.retreiveRosmSubscription(identification.utr, request.internalId) flatMap {
            case Some(rosmReg) if postcodesMatch(rosmReg.address, identification) =>
              val updatedAnswers = answers.set(EnterBusinessDetailsPage, identification)
              updateDatabaseAndRedirect(updatedAnswers, EnterBusinessDetailsPage, mode)
            case _ => Future.successful(BadRequest(view(form.fill(identification).withError("utr", "enterBusinessDetails.no-record.utr"), NormalMode)))
          }
        }
      )
  }
}
