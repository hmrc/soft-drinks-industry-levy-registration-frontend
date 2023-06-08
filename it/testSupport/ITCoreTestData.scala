package testSupport

import models._
import models.backend.{Site, UkAddress}
import org.scalatest.TryValues
import pages._
import play.api.libs.json.Json

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

trait ITCoreTestData extends TryValues {

  val contactDetails: ContactDetails = ContactDetails("test", "test", "89432789234", "test@example.com")
  val contactDetailsDiff: ContactDetails = ContactDetails("diff", "diff", "8793820901", "sample@example.com")

  val userAnswersForPackAtBusinessAddressPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswers.set(PackAtBusinessAddressPage, true).success.value
    val noSelected = emptyUserAnswers.set(PackAtBusinessAddressPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForContractPackingPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswers.set(ContractPackingPage, true).success.value
    val noSelected = emptyUserAnswers.set(ContractPackingPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForOperatePackagingSitesPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswers.set(OperatePackagingSitesPage, true).success.value
    val noSelected = emptyUserAnswers.set(OperatePackagingSitesPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }
  val userAnswersForPackagingSiteDetailsPage: Map[String, UserAnswers] = {
    val yesSelected = userAnswersWith1PackingSite.set(PackagingSiteDetailsPage, true).success.value
    val noSelected = userAnswersWith1PackingSite.set(PackagingSiteDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  val userAnswersForImportsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswers.set(ImportsPage, true).success.value
    val noSelected = emptyUserAnswers.set(ImportsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForAskSecondaryWarehousesPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswers.set(AskSecondaryWarehousesPage, true).success.value
    val noSelected = emptyUserAnswers.set(AskSecondaryWarehousesPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val userAnswersForThirdPartyPackagersPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswers.set(ThirdPartyPackagersPage, true).success.value
    val noSelected = emptyUserAnswers.set(ThirdPartyPackagersPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
    }

  val year = 2022
  val month = 11
  val day = 10
  val date = LocalDate.of(year, month, day)

  val validDateJson = Json.obj(
    "startDate.day" -> day.toString,
    "startDate.month" -> month.toString,
    "startDate.year" -> year.toString
  )

  val dateMap = Map("day" -> day, "month" -> month, "year" -> year)

  def sdilNumber = "XKSDIL000000022"
  val producerName = Some("Super Cola Ltd")

  def identifier = "some-id"

  implicit val duration = 5.seconds
  def emptyUserAnswers = UserAnswers(identifier, Json.obj())

  def packagingSite1 = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    Some("Wild Lemonade Group"),
    None)

  def packagingSiteListWith1 = Map(("78941132", packagingSite1))

  val address45Characters = Site(
    UkAddress(List("29 Station Pl.", "The Railyard", "Cambridge"), "CB1 2FP"),
    None,
    None,
    None)

  val address47Characters = Site(
    UkAddress(List("29 Station Place", "The Railyard", "Cambridge"), "CB1 2FP"),
    Some("10"),
    None,
    None)

  val address49Characters = Site(
    UkAddress(List("29 Station PlaceDr", "The Railyard", "Cambridge"), "CB1 2FP"),
    None,
    None,
    None)

  def packagingSiteListWith3 = Map(("12345678", address45Characters), ("23456789", address47Characters), ("34567890", address49Characters))


  def userAnswersWith1PackingSite = emptyUserAnswers.copy(packagingSiteList = packagingSiteListWith1)
}
