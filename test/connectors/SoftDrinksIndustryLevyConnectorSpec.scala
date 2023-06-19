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

import base.SpecBase
import models.{OptRetrievedSubscription, RetrievedSubscription, RosmRegistration, RosmWithUtr}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{ACCEPTED, NOT_FOUND, OK}
import play.api.libs.json.Json
import repositories.{CacheMap, SDILSessionCache}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utilities.GenericLogger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoftDrinksIndustryLevyConnectorSpec extends SpecBase with MockitoSugar with ScalaFutures {

  val (host, localPort) = ("host", "123")

  val mockHttp = mock[HttpClient]
  val mockSDILSessionCache = mock[SDILSessionCache]
  val softDrinksIndustryLevyConnector = new SoftDrinksIndustryLevyConnector(http = mockHttp, frontendAppConfig, mockSDILSessionCache, application.injector.instanceOf[GenericLogger])

  implicit val hc = HeaderCarrier()

  val utr: String = "1234567891"

  val identifierMap = Map("sdil" -> sdilNumber, "utr" -> utr)

  "SoftDrinksIndustryLevyConnector" - {

    s"should not call the backend and return the rosm registration" in{
      when(mockSDILSessionCache.fetchEntry[RosmWithUtr](any(), any())(any()))
      .thenReturn(Future.successful(Some(rosmRegistration)))

      val res = softDrinksIndustryLevyConnector.retreiveRosmSubscription(utr = utr, "foo")
      whenReady(
        res
      ) {
        response =>
          response mustEqual Some(rosmRegistration)
      }
    }

    s"should not call the backend and return None" in{
      when(mockSDILSessionCache.fetchEntry[RosmRegistration](any(), any())(any()))
        .thenReturn(Future.successful(None))
      when(mockHttp.GET[Option[RosmRegistration]](any(),any(), any())(any(), any(), any())).thenReturn(Future.successful(None))
      val res = softDrinksIndustryLevyConnector.retreiveRosmSubscription("utr here", "foo")
      whenReady(
        res
      ) {
        response =>
          response mustEqual (None)
      }
    }

    "should call the backend, update the cache" - {
      "and return the rosm when one is returned" in {
        when(mockSDILSessionCache.fetchEntry[RosmRegistration](any(), any())(any())).thenReturn(Future.successful(None))
        when(mockHttp.GET[Option[RosmRegistration]](any(),any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(rosmRegistration.rosmRegistration)))
        when(mockSDILSessionCache.save[RosmRegistration](any, any, any)(any())).thenReturn(Future.successful(CacheMap("test", Map("ROSM_REGISTRATION" -> Json.toJson(rosmRegistration)))))
        val res = softDrinksIndustryLevyConnector.retreiveRosmSubscription(utr = utr, "foo")
        whenReady(
          res) {
          response =>
            response mustEqual Some(rosmRegistration)
        }
      }
    }

    identifierMap.foreach { case (identifierType, identiferValue) =>
      s"when the identifier type is $identifierType" - {
        "and the cache contains an entry for subscription" - {
          "that includes a retrievedSubscription" - {
            "should not call the backend and return the subscription" in {
              when(mockSDILSessionCache.fetchEntry[OptRetrievedSubscription](any(), any())(any()))
                .thenReturn(Future.successful(Some(OptRetrievedSubscription(Some(aSubscription)))))

              val res = softDrinksIndustryLevyConnector.retrieveSubscription(identiferValue, identifierType, "id")

              whenReady(
                res
              ) {
                response =>
                  response mustEqual (Some(aSubscription))
              }
            }
          }
          "that does not include a retrievedSubscription" - {
            "should not call the backend and return None" in {
              when(mockSDILSessionCache.fetchEntry[OptRetrievedSubscription](any(), any())(any()))
                .thenReturn(Future.successful(Some(OptRetrievedSubscription(None))))

              val res = softDrinksIndustryLevyConnector.retrieveSubscription(identiferValue, identifierType, "id")

              whenReady(
                res
              ) {
                response =>
                  response mustEqual None
              }
            }
          }
          "and the cache contains no entry for subscription" - {
            "should call the backend, update the cache" - {
              "and return the subscription when one is returned" in {
                when(mockSDILSessionCache.fetchEntry[OptRetrievedSubscription](any(), any())(any())).thenReturn(Future.successful(None))
                when(mockHttp.GET[Option[RetrievedSubscription]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(aSubscription)))
                when(mockSDILSessionCache.save[OptRetrievedSubscription](any, any, any)(any())).thenReturn(Future.successful(CacheMap("test", Map("SUBSCRIPTION" -> Json.toJson(OptRetrievedSubscription(Some(aSubscription)))))))
                val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType, "id")

                whenReady(
                  res
                ) {
                  response =>
                    response mustEqual Some(aSubscription)
                }
              }
              "and return None when no subscription returned" in {
                when(mockSDILSessionCache.fetchEntry[OptRetrievedSubscription](any(), any())(any())).thenReturn(Future.successful(None))
                when(mockHttp.GET[Option[RetrievedSubscription]](any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(None))
                when(mockSDILSessionCache.save[OptRetrievedSubscription](any, any, any)(any())).thenReturn(Future.successful(CacheMap("test", Map("SUBSCRIPTION" -> Json.toJson(OptRetrievedSubscription(None))))))
                val res = softDrinksIndustryLevyConnector.retrieveSubscription(sdilNumber, identifierType, "id")

                whenReady(
                  res
                ) {
                  response =>
                    response mustEqual None
                }
              }
            }
          }
        }
      }
    }
  }
}
