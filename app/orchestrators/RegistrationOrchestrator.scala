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

package orchestrators

import cats.data.EitherT
import com.google.inject.{Inject, Singleton}
import connectors.SoftDrinksIndustryLevyConnector
import models.backend.Subscription
import models.requests.DataRequest
import play.api.mvc.AnyContent
import service.RegistrationResult
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier
import cats.implicits._
import errors.{MissingRequiredUserAnswers, RegistrationErrors}
import models.{RosmWithUtr, UserAnswers}
import utilities.GenericLogger

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class RegistrationOrchestrator @Inject()(sdilConnector: SoftDrinksIndustryLevyConnector,
                                         sessionService: SessionService,
                                         genericLogger: GenericLogger) {

  def createSubscriptionAndUpdateUserAnswers(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): RegistrationResult[Unit] = {
    val submittedDateTime = Instant.now
    val updatedUserAnswers = request.userAnswers.copy(submittedOn = Some(submittedDateTime))

    for {
      subscription <- EitherT(Future(createSubscriptionFromUserAnswers(updatedUserAnswers, request.rosmWithUtr)))
      created <- sdilConnector.createSubscription(subscription, request.rosmWithUtr.rosmRegistration.safeId)
      _ <- sessionService.set(updatedUserAnswers)
    } yield created
  }

  private def createSubscriptionFromUserAnswers(userAnswers: UserAnswers, rosmWithUtr: RosmWithUtr)
                                               (implicit ec: ExecutionContext): Either[RegistrationErrors, Subscription] = {
   Try {
     Subscription.generate(userAnswers, rosmWithUtr)
   } match {
     case Success(sub) =>
       Right(sub)
     case Failure(ex) =>
       genericLogger.logger.error(s"unable to create subscription as ${ex.getMessage}")
     Left(MissingRequiredUserAnswers)
   }
  }
}
