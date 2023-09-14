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

import connectors.SoftDrinksIndustryLevyConnector
import controllers.routes
import handlers.ErrorHandler
import models.RegisterState.canAccessEnterBusinessDetails
import models.requests._
import models.{Mode, RegisterState, RosmWithUtr, UserAnswers}
import pages.EnterBusinessDetailsPage
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc.{ActionRefiner, Result}
import services.AddressLookupState
import services.AddressLookupState.WarehouseDetails
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utilities.GenericLogger

import scala.concurrent.{ExecutionContext, Future}

trait ControllerActionHelper {

  val sdilConnector: SoftDrinksIndustryLevyConnector
  val genericLogger: GenericLogger
  val errorHandler: ErrorHandler

  def dataRequiredForRequiresBusinessDetailsAction(implicit ec: ExecutionContext): ActionRefiner[OptionalDataRequest, DataRequestForEnterBusinessDetails] = {
    new ActionRefiner[OptionalDataRequest, DataRequestForEnterBusinessDetails] {
      override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequestForEnterBusinessDetails[A]]] = {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        request.userAnswers match {
          case Some(userAnswers) if userAnswers.submittedOn.isDefined => Future.successful(Left(Redirect(routes.RegistrationConfirmationController.onPageLoad)))
          case Some(userAnswers) if canAccessEnterBusinessDetails(userAnswers) =>
            Future.successful(Right(DataRequestForEnterBusinessDetails(request = request.request, internalId = request.internalId,
              hasCTEnrolment = request.hasCTEnrolment, authUtr = request.authUtr, userAnswers = userAnswers)))
          case Some(userAnswers) =>
            val call = ActionHelpers.getRouteForRegisterState(userAnswers.registerState)
            Future.successful(Left(Redirect(call)))
          case _ =>
            genericLogger.logger.info(s"User has no user answers ${hc.requestId}")
            Future.successful(Left(Redirect(routes.RegistrationController.start)))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }
  }

  def dataRequiredForUserWhoCannotRegisterAction(registerState: RegisterState)(implicit ec: ExecutionContext): ActionRefiner[OptionalDataRequest, DataRequest] = {
    new ActionRefiner[OptionalDataRequest, DataRequest] {
      override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        request.userAnswers match {
          case Some(userAnswers) if userAnswers.submittedOn.isDefined => Future.successful(Left(Redirect(routes.RegistrationConfirmationController.onPageLoad)))
          case Some(userAnswers) if userAnswers.registerState.toString == registerState.toString =>
            getRosmData(userAnswers, request).map {
              case Right(rosmWithUtr) => Right(DataRequest(request, request.internalId, request.hasCTEnrolment, request.authUtr, userAnswers, rosmWithUtr))
              case Left(result) => Left(result)
            }
          case Some(userAnswers) =>
            val call = ActionHelpers.getRouteForRegisterState(userAnswers.registerState)
            Future.successful(Left(Redirect(call)))
          case _ =>
            genericLogger.logger.info(s"User has no user answers ${hc.requestId}")
            Future.successful(Left(Redirect(routes.RegistrationController.start)))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }
  }

  def dataRequiredForUserWhoHasSubmittedApplicationAction(implicit ec: ExecutionContext): ActionRefiner[OptionalDataRequest, DataRequestForApplicationSubmitted] = {
    new ActionRefiner[OptionalDataRequest, DataRequestForApplicationSubmitted] {
      override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequestForApplicationSubmitted[A]]] = {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        request.userAnswers match {
          case Some(userAnswers) if userAnswers.submittedOn.isDefined =>
            getRosmData(userAnswers, request).map {
              case Right(rosmWithUtr) => Right(DataRequestForApplicationSubmitted(request, request.internalId, userAnswers, rosmWithUtr, userAnswers.submittedOn.get))
              case Left(result) => Left(result)
            }
          case _ => genericLogger.logger.info(s"User has no user answers or no submitted time ${hc.requestId}")
            Future.successful(Left(Redirect(routes.RegistrationController.start)))
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }
  }

  def dataRequiredForEnterTradingNameAction(addressLookupState: AddressLookupState, ref: String, mode: Mode)(implicit ec: ExecutionContext): ActionRefiner[DataRequest, DataRequestForEnterTradingName] = {
    new ActionRefiner[DataRequest, DataRequestForEnterTradingName] {
      override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequestForEnterTradingName[A]]] = {
        Future.successful {
          val userAnswers = request.userAnswers
          userAnswers.alfResponseForLookupState match {
            case Some(alfResponseForLookupState)
              if alfResponseForLookupState.addressLookupState == addressLookupState && alfResponseForLookupState.sdilId == ref =>
              Right(DataRequestForEnterTradingName(request.request, request.internalId, request.hasCTEnrolment,
                request.authUtr, request.userAnswers, alfResponseForLookupState.address, None))
            case _ if addressLookupState == WarehouseDetails =>
              userAnswers.warehouseList.get(ref) match {
                case Some(warehouse) => Right(DataRequestForEnterTradingName(request.request, request.internalId, request.hasCTEnrolment,
                  request.authUtr, request.userAnswers, warehouse.address, Some(warehouse.tradingName)))
                case None if userAnswers.warehouseList.nonEmpty => Left(Redirect(routes.WarehouseDetailsController.onPageLoad(mode)))
                case None => Left(Redirect(routes.AskSecondaryWarehousesController.onPageLoad(mode)))
              }
            case _ =>
              userAnswers.packagingSiteList.get(ref) match {
                case Some(packagingSite) => Right(DataRequestForEnterTradingName(request.request, request.internalId, request.hasCTEnrolment,
                  request.authUtr, request.userAnswers, packagingSite.address, Some(packagingSite.tradingName)))
                case None if userAnswers.packagingSiteList.nonEmpty => Left(Redirect(routes.PackagingSiteDetailsController.onPageLoad(mode)))
                case None => Left(Redirect(routes.PackAtBusinessAddressController.onPageLoad(mode)))
              }
          }
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }
  }

  def getUtr[A](userAnswers: UserAnswers, request: OptionalDataRequest[A]): Option[String] = {
    userAnswers.get(EnterBusinessDetailsPage).map(_.utr).orElse(request.authUtr)
  }

  def getRosmData[A](userAnswers: UserAnswers, request: OptionalDataRequest[A])
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Result, RosmWithUtr]] = {
    getUtr(userAnswers, request) match {
      case Some(utr) =>
        sdilConnector.retreiveRosmSubscription(utr, request.internalId).value.map {
          case Right(rosmWithUtr) => Right(rosmWithUtr)
          case Left(_) => Left(InternalServerError(errorHandler.internalServerErrorTemplate(request)))
        }
      case None =>
        genericLogger.logger.error(s"User has no utr when required for register state ${userAnswers.registerState}")
        Future.successful(Left(InternalServerError(errorHandler.internalServerErrorTemplate(request))))
    }
  }

}
