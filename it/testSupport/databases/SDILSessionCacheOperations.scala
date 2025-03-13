package testSupport.databases

import play.api.libs.json.Format
import repositories.SDILSessionCache
import testSupport.TestConfiguration

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SDILSessionCacheOperations {

  self: TestConfiguration =>

  def addToCache[T](key: String, data: T)(
    implicit fmt: Format[T], timeout: Duration): Unit = Await.result(
    sdilSessionCache.save[T]("some-id", key, data).map(_ => ()),
    timeout
  )

  def getFromCache[T](key: String)(implicit  fmt: Format[T], timeout: Duration): Option[T] = Await.result(
    sdilSessionCache.fetchEntry("some-id", key),
    timeout
  )

}
