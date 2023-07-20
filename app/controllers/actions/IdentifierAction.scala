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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.SoftDrinksIndustryLevyConnector
import controllers.routes
import handlers.ErrorHandler
import models.requests.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionRefiner[Request, IdentifierRequest]  with ActionBuilder[IdentifierRequest, AnyContent]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               val parser: BodyParsers.Default,
                                               sdilConnector: SoftDrinksIndustryLevyConnector,
                                               errorHandler: ErrorHandler)
                                             (implicit val executionContext: ExecutionContext)
  extends IdentifierAction with AuthorisedFunctions with ActionHelpers {

  override protected def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    implicit val req: Request[A] = request
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(AuthProviders(GovernmentGateway)).retrieve(registrationRetrieval) {
      case enrolments ~ role ~ id ~ affinity =>
        id.fold[Future[Either[Result, IdentifierRequest[A]]]](Future.successful(Left(InternalServerError(errorHandler.internalServerErrorTemplate))))
          { internalId =>
          val maybeUtr = getUtr(enrolments)
          val maybeSdil = getSdilEnrolment(enrolments)
          (maybeUtr, maybeSdil) match {
            case (Some(utr), _) => handleUserWithUTR(internalId, utr, maybeSdil.isDefined, hasCTEnrolment(enrolments))
            case (None, Some(sdil)) => handleUserWithNoUTRAndSDILEnrolment(internalId, sdil, hasCTEnrolment(enrolments))
            case _ if hasValidRoleAndAffinityGroup(role, affinity) =>
              Future.successful(Right(IdentifierRequest(request, internalId, hasCTEnrolment(enrolments), None)))
            case _ => Future.successful(Left(Redirect(config.sdilFrontendBaseUrl)))
          }
        }
    } recover {
      case _: NoActiveSession =>
        Left(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
      case _: AuthorisationException =>
        Left(Redirect(routes.UnauthorisedController.onPageLoad))
    }
  }

  private def handleUserWithUTR[A](internalId: String, utr: String, hasSdilEnrolment: Boolean, hasCTEnrolment: Boolean)
                                  (implicit hc: HeaderCarrier, request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    sdilConnector.retrieveSubscription(utr, "utr", internalId).flatMap {
      case Some(sub) if sub.deregDate.isEmpty =>
        Future.successful(Left(Redirect(config.sdilFrontendBaseUrl)))
      case _ =>
        Future.successful(Right(IdentifierRequest(request, internalId, hasCTEnrolment, Some(utr))))
    }
  }

  private def handleUserWithNoUTRAndSDILEnrolment[A](internalId: String, sdil: EnrolmentIdentifier, hasCTEnrolment: Boolean)
                                 (implicit hc: HeaderCarrier, request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
      sdilConnector.retrieveSubscription(sdil.value, "sdil", internalId).map{
        case Some(sub) if sub.deregDate.nonEmpty => Right(IdentifierRequest(request, internalId, hasCTEnrolment, None))
        case _ => Left(Redirect(config.sdilFrontendBaseUrl))
      }
  }
}
