package testSupport

import models._
import models.backend.{Site, UkAddress}
import org.scalatest.TryValues
import pages._
import play.api.libs.json.Json

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

trait ITCoreTestData extends TryValues {

  val userAnswersForPackagingSiteDetailsPage: Map[String, UserAnswers] = {
    val yesSelected = userAnswersWith1PackingSite.set(PackagingSiteDetailsPage, true).success.value
    val noSelected = userAnswersWith1PackingSite.set(PackagingSiteDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val year = 2022
  val month = 11
  val day = 10
  val date = LocalDate.of(year, month, day)

  val validDateJson = Json.obj(
    "value.day" -> day.toString,
    "value.month" -> month.toString,
    "value.year" -> year.toString
  )

  val dateMap = Map("day" -> day, "month" -> month, "year" -> year)

  def sdilNumber = "XKSDIL000000022"
  val producerName = Some("Super Cola Ltd")

  def identifier = "some-id"

  implicit val duration = 5.seconds
  def emptyUserAnswers = UserAnswers(identifier, Json.obj())

  val PackagingSite1 = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    Some("Wild Lemonade Group"),
    None)

  lazy val packagingSiteListWith1 = Map(("78941132", PackagingSite1))

  def userAnswersWith1PackingSite = emptyUserAnswers.copy(packagingSiteList = packagingSiteListWith1)
}
