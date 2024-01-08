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

package repositories

import play.api.libs.json.Format

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class SDILSessionCache @Inject() (
  sdilSessionCacheRepository: SDILSessionCacheRepository,
  cascadeUpsert: CascadeUpsert)(implicit val ec: ExecutionContext) {

  def save[A](_id: String, key: String, value: A)(
    implicit
    fmt: Format[A]): Future[CacheMap] = {
    sdilSessionCacheRepository.get(_id).flatMap { optionalCacheMap =>
      val updatedCacheMap = cascadeUpsert(
        key,
        value,
        optionalCacheMap.getOrElse(CacheMap(_id, Map())))
      sdilSessionCacheRepository.upsert(updatedCacheMap).map { _ =>
        updatedCacheMap
      }
    }
  }

  def remove(_id: String, key: String): Future[Boolean] = {
    sdilSessionCacheRepository.get(_id).flatMap { optionalCacheMap =>
      optionalCacheMap.fold(Future(false)) { cacheMap =>
        val newCacheMap = cacheMap copy (data = cacheMap.data - key)
        sdilSessionCacheRepository.upsert(newCacheMap)
      }
    }
  }

  def removeRecord(_id: String): Future[Boolean] = {
    sdilSessionCacheRepository.removeRecord(_id)
  }

  def fetch(_id: String): Future[Option[CacheMap]] =
    sdilSessionCacheRepository.get(_id)

  def fetchEntry[T](_id: String, key: String)(implicit fmt: Format[T]): Future[Option[T]] = {
    fetch(_id).map(optCacheMap =>
      optCacheMap.fold[Option[T]](None)(cachedMap =>
        cachedMap.data.get(key)
          .map(json =>
            json.as[T])))
  }

  def extendSession(_id: String): Future[Boolean] = {
    sdilSessionCacheRepository.updateLastUpdated(_id)
  }

}
