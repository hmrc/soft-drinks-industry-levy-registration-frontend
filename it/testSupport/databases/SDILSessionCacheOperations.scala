package testSupport.databases

import play.api.libs.json.Format
import repositories.SDILSessionCache
import testSupport.TestConfiguration

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SDILSessionCacheOperations {

  self: TestConfiguration =>

  def addToCache[T](key: String, data: T)(
    implicit fmt: Format[T], cacheTimeout: Duration): Unit = Await.result(
    self.sdilSessionCache.save[T]("some-id", key, data).map(_ => ()),
    cacheTimeout
  )

  def getFromCache[T](key: String)(implicit  fmt: Format[T], cacheTimeout: Duration): Option[T] = Await.result(
    self.sdilSessionCache.fetchEntry("some-id", key),
    cacheTimeout
  )

}
