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

package mocks

import connectors.HttpClientV2Helper
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HttpClient, HttpReads}

import scala.concurrent.Future

trait MockHttp extends HttpClientV2Helper with BeforeAndAfterEach {
  def setupMockHttpGet[T](response: T): OngoingStubbing[Future[T]] =
    when(requestBuilderExecute[T]).thenReturn(Future.successful(response))

  def setupMockHttpPost[O](response: O): OngoingStubbing[Future[O]] =
    when(requestBuilderExecute[O]).thenReturn(Future.successful(response))

  def setupMockHttpPut[O](response: O): OngoingStubbing[Future[O]] =
    when(requestBuilderExecute[O]).thenReturn(Future.successful(response))

}
