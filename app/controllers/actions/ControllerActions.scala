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

package controllers.actions

import com.google.inject.{Inject, Singleton}
import connectors.SoftDrinksIndustryLevyConnector
import handlers.ErrorHandler
import models.Mode
import models.RegisterState._
import models.requests.{DataRequest, DataRequestForApplicationSubmitted, DataRequestForEnterBusinessDetails, DataRequestForEnterTradingName}
import play.api.mvc.{ActionBuilder, AnyContent}
import services.AddressLookupState
import utilities.GenericLogger

import scala.concurrent.ExecutionContext

@Singleton
class ControllerActions @Inject()(identify: IdentifierAction,
                                  getData: DataRetrievalAction,
                                  dataRequired: DataRequiredAction,
                                  val sdilConnector: SoftDrinksIndustryLevyConnector,
                                  val genericLogger: GenericLogger,
                                  val errorHandler: ErrorHandler)(implicit ec: ExecutionContext) extends ControllerActionHelper {

  def withUserWhoCanEnterTradingName(addressLookupState: AddressLookupState, ref: String, mode: Mode): ActionBuilder[DataRequestForEnterTradingName, AnyContent] = {
    withUserWhoCanRegister andThen dataRequiredForEnterTradingNameAction(addressLookupState, ref, mode)
  }

  def withUserWhoCanRegister: ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen dataRequired
  }

  def withUserWhoHasSubmittedRegistrationAction: ActionBuilder[DataRequestForApplicationSubmitted, AnyContent] = {
    identify andThen getData andThen dataRequiredForUserWhoHasSubmittedApplicationAction
  }

  def withAlreadyRegisteredAction: ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen dataRequiredForUserWhoCannotRegisterAction(AlreadyRegistered)
  }

  def withPendingRegistrationAction: ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen dataRequiredForUserWhoCannotRegisterAction(RegistrationPending)
  }

  def withRegisterApplicationAcceptedAction: ActionBuilder[DataRequest, AnyContent] = {
    identify andThen getData andThen dataRequiredForUserWhoCannotRegisterAction(RegisterApplicationAccepted)
  }

  def withRequiresBusinessDetailsAction: ActionBuilder[DataRequestForEnterBusinessDetails, AnyContent] = {
    identify andThen getData andThen dataRequiredForRequiresBusinessDetailsAction
  }

}
