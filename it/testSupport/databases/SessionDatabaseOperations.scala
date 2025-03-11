package testSupport.databases

import models.UserAnswers
import repositories.SessionRepository
import testSupport.TestConfiguration

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SessionDatabaseOperations {

  self: TestConfiguration =>
  
  def setAnswers(userAnswers: UserAnswers)(implicit timeout: Duration): Unit = Await.result(
    self.sessionRepository.set(userAnswers),
    timeout
  )

  def getAnswers(id: String)(implicit timeout: Duration): Option[UserAnswers] = Await.result(
    self.sessionRepository.get(id),
    timeout
  )

  def remove(id: String)(implicit timeout: Duration): Boolean = Await.result(
    sessionRepository.clear(id),
    timeout
  )

}
