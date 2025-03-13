/*
 * Copyright 2025 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class TestConnectorSpec extends AnyFlatSpec with Matchers with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
  val mockConfig: Configuration = Configuration(
    "microservice.services.soft-drinks-industry-levy.host" -> "localhost",
    "microservice.services.soft-drinks-industry-levy.port" -> "8701"
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttpClient, mockRequestBuilder)
  }

  "TestConnector" should "call resetPending endpoint correctly" in {
    val connector = new TestConnector(mockHttpClient, mockConfig)
    val mockResponse = HttpResponse(200, "")

    when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.execute[HttpResponse](any(), any())).thenReturn(Future.successful(mockResponse))
    val result = connector.resetPending
    whenReady(result) { response =>
      response shouldBe mockResponse
    }
    verify(mockHttpClient).get(any())(any())
  }

  it should "call resetSubscriptions endpoint correctly" in {
    val localMockHttpClient: HttpClientV2 = mock[HttpClientV2]
    val localMockRequestBuilder: RequestBuilder = mock[RequestBuilder]
    val connector = new TestConnector(localMockHttpClient, mockConfig)
    val mockResponse = HttpResponse(200, "")

    when(localMockHttpClient.get(any())(any())).thenReturn(localMockRequestBuilder)
    when(localMockRequestBuilder.execute[HttpResponse](any(), any())).thenReturn(Future.successful(mockResponse))
    val result = connector.resetSubscriptions
    whenReady(result) { response =>
      response shouldBe mockResponse
    }
    verify(localMockHttpClient).get(any())(any())
  }

  it should "construct the base URL correctly" in {
    val connector = new TestConnector(mockHttpClient, mockConfig)
    connector.testUrl shouldBe "http://localhost:8701"
  }
}
