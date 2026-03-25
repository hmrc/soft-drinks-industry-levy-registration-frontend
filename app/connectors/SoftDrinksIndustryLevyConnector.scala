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
import play.api.libs.json.{JsValue, Json}
import repositories.{SDILSessionCache, SDILSessionKeys}
import service.RegistrationResult
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import utilities.GenericLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
class SoftDrinksIndustryLevyConnector @Inject() (
  val http: HttpClientV2,
  frontendAppConfig: FrontendAppConfig,
  sdilSessionCache: SDILSessionCache,
  genericLogger: GenericLogger
)(implicit ec: ExecutionContext) {

  lazy val sdilUrl: String = frontendAppConfig.sdilBaseUrl

  private val logger = genericLogger.logger

  private class RawHttpReads extends HttpReads[HttpResponse] {
    override def read(method: String, url: String, response: HttpResponse): HttpResponse = response
  }

  private val rawHttpReads = new RawHttpReads

  private def outboundHeaderCarrier(hc: HeaderCarrier): HeaderCarrier                                    =
    HeaderCarrier(
      requestId = hc.requestId,
      sessionId = hc.sessionId
    )

  private def sdilContext(
    path: String,
    status: Option[Int] = None,
    startTime: Option[Long] = None
  ): String =
    Seq(
      Some(s"path=$path"),
      status.map(st => s"status=$st"),
      startTime.map(st => s"durationMs=${System.currentTimeMillis() - st}")
    ).flatten.mkString(" ")

  private def executeGet[A](operation: String, path: String)(implicit
    hc: HeaderCarrier,
    rds: HttpReads[A]
  ): Future[A] = {
    val urlString = s"$sdilUrl$path"
    val startTime = System.currentTimeMillis()
    logger.info(
      s"SDIL $operation request ${sdilContext(path, startTime = Some(startTime))}"
    )
    http
      .get(url"$urlString")(using outboundHeaderCarrier(hc))
      .execute[HttpResponse](using rawHttpReads, ec)
      .map { response =>
        logger.info(
          s"SDIL $operation response ${sdilContext(path, status = Some(response.status), startTime = Some(startTime))}"
        )
        rds.read("GET", urlString, response)
      }
      .recoverWith { case NonFatal(e) =>
        logger.error(
          s"SDIL $operation failure ${sdilContext(path, startTime = Some(startTime))} error=${e.getMessage}",
          e
        )
        Future.failed(e)
      }
  }

  private def executePost[A](operation: String, path: String, body: JsValue)(implicit
    hc: HeaderCarrier,
    rds: HttpReads[A]
  ): Future[A] = {
    val urlString = s"$sdilUrl$path"
    val startTime = System.currentTimeMillis()
    logger.info(
      s"SDIL $operation request ${sdilContext(path, startTime = Some(startTime))}"
    )
    http
      .post(url"$urlString")(using outboundHeaderCarrier(hc))
      .withBody(body)
      .execute[HttpResponse](using rawHttpReads, ec)
      .map { response =>
        logger.info(
          s"SDIL $operation response ${sdilContext(path, status = Some(response.status), startTime = Some(startTime))}"
        )
        rds.read("POST", urlString, response)
      }
      .recoverWith { case NonFatal(e) =>
        logger.error(
          s"SDIL $operation failure ${sdilContext(path, startTime = Some(startTime))} error=${e.getMessage}",
          e
        )
        Future.failed(e)
      }
  }

  def retreiveRosmSubscription(utr: String, internalId: String)(implicit
    hc: HeaderCarrier
  ): RegistrationResult[RosmWithUtr] = EitherT {
    sdilSessionCache.fetchEntry[RosmWithUtr](internalId, SDILSessionKeys.ROSM_REGISTRATION).flatMap {
      case Some(rosmReg) if rosmReg.utr == utr => Future.successful(Right(rosmReg))
      case _                                   =>
        executeGet[Option[RosmRegistration]](
          operation = "retreiveRosmSubscription",
          path = s"/rosm-registration/lookup/$utr"
        )
          .flatMap {
            case Some(rosmReg) =>
              val rosmWithUtr = RosmWithUtr(
                utr,
                RosmRegistration(rosmReg.safeId, rosmReg.organisation, rosmReg.individual, rosmReg.address)
              )
              sdilSessionCache.save(internalId, SDILSessionKeys.ROSM_REGISTRATION, rosmWithUtr).map { _ =>
                Right(rosmWithUtr)
              }
            case None          => Future.successful(Left(NoROSMRegistration))
          }
          .recover { case NonFatal(_) =>
            Left(UnexpectedResponseFromSDIL)
          }
    }
  }
  def checkPendingQueue(utr: String)(implicit hc: HeaderCarrier): RegistrationResult[SubscriptionStatus] = EitherT {
    val path = s"/check-enrolment-status/$utr"
    executeGet[HttpResponse](
      operation = "checkPendingQueue",
      path = path
    )(using hc, rawHttpReads)
      .map(_.status match {
        case OK        => Right(Registered)
        case ACCEPTED  => Right(Pending)
        case NOT_FOUND => Right(DoesNotExist)
        case status    =>
          logger.warn(
            s"SDIL checkPendingQueue unexpected-response ${sdilContext(path, status = Some(status))}"
          )
          Left(UnexpectedResponseFromSDIL)
      })
  }

  def retrieveSubscription(identifierValue: String, identifierType: String, internalId: String)(implicit
    hc: HeaderCarrier
  ): RegistrationResult[Option[RetrievedSubscription]] = EitherT {
    sdilSessionCache.fetchEntry[OptRetrievedSubscription](internalId, SDILSessionKeys.SUBSCRIPTION).flatMap {
      case Some(optSubscription) => Future.successful(Right(optSubscription.optRetrievedSubscription))
      case None                  =>
        executeGet[Option[RetrievedSubscription]](
          operation = "retrieveSubscription",
          path = s"/subscription/$identifierType/$identifierValue"
        )
          .flatMap { optRetrievedSubscription =>
            sdilSessionCache
              .save(internalId, SDILSessionKeys.SUBSCRIPTION, OptRetrievedSubscription(optRetrievedSubscription))
              .map { _ =>
                Right(optRetrievedSubscription)
              }
          }
          .recover { case NonFatal(_) =>
            Left(UnexpectedResponseFromSDIL)
          }
    }
  }

  def createSubscription(subscription: Subscription, safeId: String)(implicit
    hc: HeaderCarrier
  ): RegistrationResult[Unit] = EitherT {
    val path = s"/subscription/utr/${subscription.utr}/$safeId"
    executePost[HttpResponse](
      operation = "createSubscription",
      path = path,
      body = Json.toJson(subscription)
    )(using hc, rawHttpReads)
      .map { resp =>
        resp.status match {
          case OK       => Right((): Unit)
          case CONFLICT =>
            logger.warn(
              s"SDIL createSubscription conflict ${sdilContext(path, status = Some(resp.status))}"
            )
            Right((): Unit)
          case status   =>
            logger.error(
              s"SDIL createSubscription unexpected-response ${sdilContext(path, status = Some(status))}"
            )
            Left(UnexpectedResponseFromSDIL)
        }
      }
      .recover { case NonFatal(_) =>
        Left(UnexpectedResponseFromSDIL)
      }
  }
}
