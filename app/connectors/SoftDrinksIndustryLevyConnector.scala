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

import config.FrontendAppConfig
import models._
import play.api.http.Status.{ACCEPTED, OK}
import repositories.{SDILSessionCache, SDILSessionKeys}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, HttpResponse, NotFoundException}
import utilities.GenericLogger
import uk.gov.hmrc.http.HttpReads.Implicits._
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
                              (implicit hc: HeaderCarrier): Future[Option[RosmWithUtr]] = {
    sdilSessionCache.fetchEntry[RosmWithUtr](internalId, SDILSessionKeys.ROSM_REGISTRATION).flatMap{
      case Some(rosmReg) if rosmReg.utr == utr => Future.successful(Some(rosmReg))
      case _ =>
        http.GET[Option[RosmRegistration]](getRosmRegistration(utr)).flatMap {
          case Some(rosmReg) =>
            val rosmWithUtr = RosmWithUtr(utr, RosmRegistration(rosmReg.safeId,rosmReg.organisation,rosmReg.individual,rosmReg.address))
            sdilSessionCache.save(internalId, SDILSessionKeys.ROSM_REGISTRATION, rosmWithUtr).map{_ =>Some(rosmWithUtr)}
          case None => Future.successful(None)
        }
    }
  }

  private def getSubscriptionUrl(identifierValue: String, identifierType: String): String = s"$sdilUrl/subscription/$identifierType/$identifierValue"

  def checkPendingQueue(utr: String)(implicit hc: HeaderCarrier): Future[SubscriptionStatus] = {
    def onError(status: Int) = {
      genericLogger.logger.warn(s"Returned unexpected status $status for ${hc.requestId} when attempting to check pending queue")
      throw new Exception(s"Unexpected status from Soft Drinks Levy BE when checking for registration ${hc.requestId} $status")
    }

    http.GET[HttpResponse](s"$sdilUrl/check-enrolment-status/$utr").map(_.status match {
      case OK => Registered
      case ACCEPTED => Pending
      case status => onError(status)
    }) recover {
      case _: NotFoundException => DoesNotExist
      case e: HttpException => onError(e.responseCode)
    }
  }

  def retrieveSubscription(identifierValue: String, identifierType: String, internalId: String)
                          (implicit hc: HeaderCarrier): Future[Option[RetrievedSubscription]] = {
    sdilSessionCache.fetchEntry[OptRetrievedSubscription](internalId, SDILSessionKeys.SUBSCRIPTION).flatMap{
      case Some(optSubscription) => Future.successful(optSubscription.optRetrievedSubscription)
      case None =>
        http.GET[Option[RetrievedSubscription]](getSubscriptionUrl(identifierValue: String, identifierType)).flatMap {
          optRetrievedSubscription =>
            sdilSessionCache.save(internalId, SDILSessionKeys.SUBSCRIPTION, OptRetrievedSubscription(optRetrievedSubscription))
              .map{_ =>
                optRetrievedSubscription}
        }
    }
  }
}