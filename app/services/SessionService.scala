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

package services

import cats.data.EitherT
import com.google.inject.{ Inject, Singleton }
import errors._
import models.UserAnswers
import repositories.SessionRepository
import service.RegistrationResult

import scala.concurrent.ExecutionContext

@Singleton
class SessionService @Inject() (sessionRepository: SessionRepository)(implicit ec: ExecutionContext) {

  def keepAlive(id: String): RegistrationResult[Boolean] = EitherT {
    sessionRepository.keepAlive(id)
      .map(Right(_))
      .recover {
        case _ => Left(SessionDatabaseInsertError)
      }
  }

  def get(id: String): RegistrationResult[Option[UserAnswers]] = EitherT {
    sessionRepository.get(id)
      .map(Right(_))
      .recover {
        case _ => Left(SessionDatabaseGetError)
      }
  }

  def set(answers: UserAnswers): RegistrationResult[Boolean] = EitherT {
    sessionRepository.set(answers)
      .map(Right(_))
      .recover {
        case _ => Left(SessionDatabaseInsertError)
      }
  }

  def clear(id: String): RegistrationResult[Boolean] = EitherT {
    sessionRepository.clear(id)
      .map(Right(_))
      .recover {
        case _ => Left(SessionDatabaseDeleteError)
      }
  }

}
