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

package connectors

import cats.data.EitherT
import config.FrontendAppConfig
import errors.{NoROSMRegistration, UnexpectedResponseFromSDIL}
import models._
import models.backend.Subscription
import play.api.http.Status._
import repositories.{SDILSessionCache, SDILSessionKeys}
import service.RegistrationResult
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import utilities.GenericLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SoftDrinksIndustryLevyConnector @Inject()(
                                                 val http: HttpClient,
                                                 frontendAppConfig: FrontendAppConfig,
                                                 sdilSessionCache: SDILSessionCache,
                                                 genericLogger: GenericLogger
                                               )(implicit ec: ExecutionContext) {

  lazy val sdilUrl: String = frontendAppConfig.sdilBaseUrl

  private def getRosmRegistration(utr: String):String = s"$sdilUrl/rosm-registration/lookup/$utr"


  def retreiveRosmSubscription(utr: String, internalId: String)
                              (implicit hc: HeaderCarrier): RegistrationResult[RosmWithUtr] = EitherT {
    sdilSessionCache.fetchEntry[RosmWithUtr](internalId, SDILSessionKeys.ROSM_REGISTRATION).flatMap{
      case Some(rosmReg) if rosmReg.utr == utr => Future.successful(Right(rosmReg))
      case _ =>
        http.GET[Option[RosmRegistration]](getRosmRegistration(utr)).flatMap {
          case Some(rosmReg) =>
            val rosmWithUtr = RosmWithUtr(utr, RosmRegistration(rosmReg.safeId,rosmReg.organisation,rosmReg.individual,rosmReg.address))
            sdilSessionCache.save(internalId, SDILSessionKeys.ROSM_REGISTRATION, rosmWithUtr).map{_ => Right(rosmWithUtr)}
          case None => Future.successful(Left(NoROSMRegistration))
        }.recover {
          case _ =>
            genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][retreiveRosmSubscription] - unexpected response for ${utr}")
            Left(UnexpectedResponseFromSDIL)
        }
    }
  }

  private def getSubscriptionUrl(identifierValue: String, identifierType: String): String = s"$sdilUrl/subscription/$identifierType/$identifierValue"

  def checkPendingQueue(utr: String)(implicit hc: HeaderCarrier): RegistrationResult[SubscriptionStatus] = EitherT {
    http.GET[HttpResponse](s"$sdilUrl/check-enrolment-status/$utr").map(_.status match {
      case OK => Right(Registered)
      case ACCEPTED => Right(Pending)
      case NOT_FOUND => Right(DoesNotExist)
      case status => genericLogger.logger.warn(s"Returned unexpected status $status for ${hc.requestId} when attempting to check pending queue")
        Left(UnexpectedResponseFromSDIL)
    })
  }

  def retrieveSubscription(identifierValue: String, identifierType: String, internalId: String)
                          (implicit hc: HeaderCarrier): RegistrationResult[Option[RetrievedSubscription]] = EitherT{
    sdilSessionCache.fetchEntry[OptRetrievedSubscription](internalId, SDILSessionKeys.SUBSCRIPTION).flatMap{
      case Some(optSubscription) => Future.successful(Right(optSubscription.optRetrievedSubscription))
      case None =>
        http.GET[Option[RetrievedSubscription]](getSubscriptionUrl(identifierValue: String, identifierType)).flatMap {
          optRetrievedSubscription =>
            sdilSessionCache.save(internalId, SDILSessionKeys.SUBSCRIPTION, OptRetrievedSubscription(optRetrievedSubscription))
              .map{_ =>
                Right(optRetrievedSubscription)
              }
        }.recover {
          case _ =>
            genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][retrieveSubscription] - unexpected response for ${identifierValue}")
            Left(UnexpectedResponseFromSDIL)
        }
    }
  }

  def createSubscription(subscription: Subscription, safeId: String)
                        (implicit hc: HeaderCarrier): RegistrationResult[Unit] = EitherT {
    val url = s"$sdilUrl/subscription/utr/${subscription.utr}/$safeId"
    http.POST[Subscription, HttpResponse](url, subscription).map{ resp => resp.status match {
      case OK => Right((): Unit)
      case CONFLICT =>
        genericLogger.logger.warn(s"[SoftDrinksIndustryLevyConnector][createSubscription] - CONFLICT returned for ${subscription.utr}")
        Right((): Unit)
      case status => genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][createSubscription] - unexpected response $status for ${subscription.utr}")
        Left(UnexpectedResponseFromSDIL)
      }
    }.recover{
      case _ =>
        genericLogger.logger.error(s"[SoftDrinksIndustryLevyConnector][createSubscription] - unexpected response for ${subscription.utr}")
        Left(UnexpectedResponseFromSDIL)
    }
  }
}
