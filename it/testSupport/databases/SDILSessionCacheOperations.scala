/*
 * Copyright 2026 HM Revenue & Customs
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

package testSupport.databases

import play.api.libs.json.Format
import testSupport.TestConfiguration

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SDILSessionCacheOperations {

  self: TestConfiguration =>

  def addToCache[T](key: String, data: T)(implicit fmt: Format[T], timeout: Duration): Unit = Await.result(
    sdilSessionCache.save[T]("some-id", key, data).map(_ => ()),
    timeout
  )

  def getFromCache[T](key: String)(implicit fmt: Format[T], timeout: Duration): Option[T] = Await.result(
    sdilSessionCache.fetchEntry("some-id", key),
    timeout
  )

}
