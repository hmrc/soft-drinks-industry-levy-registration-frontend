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

package models

import pages.EnterBusinessDetailsPage

sealed trait RegisterState

object RegisterState extends Enumerable.Implicits {

  object RequiresBusinessDetails extends WithName("RequiresBusinessDetails") with RegisterState
  object AlreadyRegistered extends WithName("AlreadyRegistered") with RegisterState
  object RegisterApplicationAccepted extends WithName("RegisterApplicationAccepted") with RegisterState
  object RegistrationPending extends WithName("RegistrationPending") with RegisterState
  object RegisterWithAuthUTR extends WithName("RegisterWithAuthUTR") with RegisterState
  object RegisterWithOtherUTR extends WithName("RegisterWithOtherUTR") with RegisterState


  val values: Seq[RegisterState] = Seq(
    RequiresBusinessDetails, AlreadyRegistered, RegisterApplicationAccepted, RegistrationPending, RegisterWithAuthUTR, RegisterWithOtherUTR
  )

  def canRegister(state: RegisterState): Boolean = List(RegisterWithAuthUTR, RegisterWithOtherUTR).contains(state)

  def canAccessEnterBusinessDetails(userAnswers: UserAnswers): Boolean = {
    (userAnswers.registerState.toString == RequiresBusinessDetails.toString) || userAnswers.get(EnterBusinessDetailsPage).nonEmpty
  }


  implicit val enumerable: Enumerable[RegisterState] =
    Enumerable(values.map(v => v.toString -> v): _*)

}