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

import controllers.routes
import models.{NormalMode, UserAnswers}
import org.bson.json.JsonObject
import pages.{AskSecondaryWarehousesPage, WarehouseDetailsPage}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.mvc.BodyParser.Json
import services.SessionService
import utilities.GenericLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WarehouseDetailsChecker @Inject()(genericLogger: GenericLogger, val sessionService: SessionService)
                                       (implicit val executionContext: ExecutionContext) extends ActionHelpers {

  def checkWarehouseDetails(userAnswers: UserAnswers)(action: => Result): Result = {
    userAnswers match {
      case answers if answers.warehouseList.nonEmpty =>
        action
      case _ =>
        val updatedAnswers = userAnswers.remove(AskSecondaryWarehousesPage).get
        sessionService.set(updatedAnswers)
        genericLogger.logger.warn("Failed to load the requested page due to no warehouse being present")
        Redirect(routes.AskSecondaryWarehousesController.onPageLoad(NormalMode))
    }
  }
}
