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

import controllers.routes._
import models.RegisterState._
import models.{NormalMode, RegisterState}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core._
trait ActionHelpers {

  val registrationRetrieval = allEnrolments and credentialRole and internalId and affinityGroup
  val TWO = 2
  val FOUR = 4

  protected def getSdilEnrolment(enrolments: Enrolments): Option[EnrolmentIdentifier] = {
    val sdil = for {
      enrolment <- enrolments.enrolments if enrolment.key.equalsIgnoreCase("HMRC-OBTDS-ORG")
      sdil      <- enrolment.getIdentifier("EtmpRegistrationNumber") if sdil.value.slice(TWO, FOUR) == "SD"
    } yield {
      sdil
    }

    sdil.headOption
  }

  protected def getUtr(enrolments: Enrolments): Option[String] =
    enrolments
      .getEnrolment("IR-CT")
      .orElse(enrolments.getEnrolment("IR-SA"))
      .flatMap(_.getIdentifier("UTR").map(_.value))

  protected def hasCTEnrolment(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment("IR-CT").isDefined

  protected def hasValidRoleAndAffinityGroup(credentialRole: Option[CredentialRole], affinityGroup: Option[AffinityGroup]): Boolean = {
    val isNotAssistant = credentialRole.fold(true)(_ != Assistant)
    lazy val validAffinityGroup = affinityGroup.fold(false)(_ != Agent)
    isNotAssistant && validAffinityGroup
  }
}

object ActionHelpers extends ActionHelpers {
  def getRouteForRegisterState(registerState: RegisterState) = registerState match {
    case RequiresBusinessDetails => EnterBusinessDetailsController.onPageLoad
    case AlreadyRegistered => AlreadyRegisteredController.onPageLoad
    case RegisterApplicationAccepted => ApplicationAlreadySubmittedController.onPageLoad
    case RegistrationPending => RegistrationPendingController.onPageLoad
    case _ => VerifyController.onPageLoad(NormalMode)
  }
}
