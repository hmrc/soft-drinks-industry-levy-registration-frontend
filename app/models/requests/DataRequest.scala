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

package models.requests

import models.backend.UkAddress
import models.{RosmWithUtr, UserAnswers}
import play.api.mvc.{Request, WrappedRequest}

import java.time.Instant

case class OptionalDataRequest[A] (request: Request[A],
                                   internalId: String,
                                   hasCTEnrolment: Boolean = false,
                                   authUtr: Option[String] = None,
                                   userAnswers: Option[UserAnswers]
                                   ) extends WrappedRequest[A](request)

case class DataRequest[A] (request: Request[A],
                           internalId: String,
                           hasCTEnrolment: Boolean = false,
                           authUtr: Option[String] = None,
                           userAnswers: UserAnswers,
                           rosmWithUtr: RosmWithUtr) extends WrappedRequest[A](request)

case class DataRequestForEnterBusinessDetails[A] (request: Request[A],
                           internalId: String,
                           hasCTEnrolment: Boolean = false,
                           authUtr: Option[String] = None,
                           userAnswers: UserAnswers) extends WrappedRequest[A](request)

case class DataRequestForApplicationSubmitted[A](request: Request[A],
                                                 internalId: String,
                                                 userAnswers: UserAnswers,
                                                 rosmWithUtr: RosmWithUtr,
                                                 submittedDateTime: Instant) extends WrappedRequest[A](request)

case class DataRequestForEnterTradingName[A](request: Request[A],
                                             internalId: String,
                                             hasCTEnrolment: Boolean = false,
                                             authUtr: Option[String] = None,
                                             userAnswers: UserAnswers,
                                             aflAddress: UkAddress,
                                             tradingName: Option[String]) extends WrappedRequest[A](request)

case class DataRequiredForPackagingSites[A](request: Request[A],
                                            internalId: String,
                                            userAnswers: UserAnswers) extends WrappedRequest[A](request)