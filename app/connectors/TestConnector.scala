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

import play.api.Configuration
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestConnector @Inject()(http: HttpClientV2, val configuration: Configuration)(implicit ec: ExecutionContext)
  extends ServicesConfig(configuration) {

  lazy val testUrl: String = baseUrl("soft-drinks-industry-levy")

  def resetPending(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val pendingUrl = s"$testUrl/test-only/reset-pending"
    http.get(url"$pendingUrl")
      .execute[HttpResponse]
  }

  def resetSubscriptions(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val resetUrl = s"$testUrl/test-only/reset-subscriptions"
    http.get(url"$resetUrl")
      .execute[HttpResponse]
  }
}
