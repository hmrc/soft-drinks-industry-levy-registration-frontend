package testSupport

import controllers.routes
import models.HowManyLitresGlobally.{Large, Small}
import models.OrganisationType.LimitedCompany
import models.Verify.YesRegister
import models._
import models.backend.{Site, UkAddress}
import org.scalatest.TryValues
import pages._
import play.api.libs.json.{JsObject, Json}

import java.time.LocalDate
import scala.concurrent.duration.{DurationInt, FiniteDuration}

trait ITCoreTestData extends TryValues {

  val warehousesTradingName: WarehousesTradingName = WarehousesTradingName("test1")
  val warehousesTradingNameDiff: WarehousesTradingName = WarehousesTradingName("diff1")

  val packagingSiteName: PackagingSiteName = PackagingSiteName("test1")
  val packagingSiteNameDiff: PackagingSiteName = PackagingSiteName("diff1")

  val userAnswersForWarehouseDetailsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswers
      .set(WarehouseDetailsPage, true).success.value
      .copy(warehouseList = warehouseListWith1)
    val noSelected = emptyUserAnswers
      .set(WarehouseDetailsPage, false).success.value
      .copy(warehouseList = warehouseListWith1)
    Map("yes" -> yesSelected,"no" -> noSelected)
  }

  val aTradingName = "Bold Drinks Ltd"

  def userAnswersForUpdateRegisteredDetailsRemoveWarehouseDetailsPage(index: String): Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswers
      .copy(warehouseList = Map(index -> Warehouse(aTradingName, ukAddress)))
      .set(RemoveWarehouseDetailsPage, true).success.value

    val noSelected = emptyUserAnswers
      .copy(warehouseList = Map(index -> Warehouse(aTradingName, ukAddress)))
      .set(RemoveWarehouseDetailsPage, false).success.value
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

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
      .copy(packagingSiteList = packagingSiteListWith1)
    val noSelected = userAnswersWith1PackingSite.set(PackagingSiteDetailsPage, false).success.value
      .copy(packagingSiteList = packagingSiteListWith1)
    Map("yes" -> yesSelected, "no" -> noSelected)
  }

  val userAnswersForImportsPage: Map[String, UserAnswers] = {
    val yesSelected = emptyUserAnswers
      .set(ImportsPage, true).success.value
      .set(HowManyImportsPage, LitresInBands(0,0)).success.value
    val noSelected = emptyUserAnswers
      .set(ImportsPage, false).success.value
      .set(HowManyImportsPage, LitresInBands(0,0)).success.value
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

  val userAnswersForEnterBusinessDetailsPage = emptyUserAnswers
    .set(EnterBusinessDetailsPage, Identify("0000001611", "TF8 4FH")).success.value

  var year = 2022
  var month = 11
  var day = 10
  val date = LocalDate.of(year, month, day)

  val validDateJson: JsObject = Json.obj(
    "startDate.day" -> day.toString,
    "startDate.month" -> month.toString,
    "startDate.year" -> year.toString
  )

  val dateMap: Map[String, Int] = Map("day" -> day, "month" -> month, "year" -> year)

  val ukAddress: UkAddress = UkAddress(List("foo", "bar"),"wizz", None)

  def sdilNumber = "XKSDIL000000022"
  val producerName = Some("Super Cola Ltd")

  def identifier = "some-id"

  val defaultCall = routes.RegistrationController.start

  implicit val duration: FiniteDuration = 5.seconds
  def emptyUserAnswers = UserAnswers(identifier, RegisterState.RegisterWithAuthUTR, Json.obj())

  def packagingSite1 = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    aTradingName,
    None)

  def packagingSiteListWith1 = Map(("78941132", packagingSite1))

  val address45Characters = Site(
    UkAddress(List("29 Station Pl.", "The Railyard", "Cambridge"), "CB1 2FP"),
    None,
    aTradingName,
    None)

  val address47Characters = Site(
    UkAddress(List("29 Station Place", "The Railyard", "Cambridge"), "CB1 2FP"),
    Some("10"),
    aTradingName,
    None)

  val address49Characters = Site(
    UkAddress(List("29 Station PlaceDr", "The Railyard", "Cambridge"), "CB1 2FP"),
    None,
    aTradingName,
    None)

  def packagingSiteListWith3 = Map(("12345678", address45Characters), ("23456789", address47Characters), ("34567890", address49Characters))

  def userAnswersWith1PackingSite = emptyUserAnswers.copy(packagingSiteList = packagingSiteListWith1)


  def warehouse1 = Warehouse("Warehouse One", UkAddress(List("29 Station Place", "The Railyard", "Cambridge"), "CB1 2FP"))
  def warehouse2 = Warehouse("Warehouse Two", UkAddress(List("42 Hitch Place", "The Railyard", "Cambridge"), "CB1 2FF"))
  def warehouseListWith1 = Map("78941132" -> warehouse1)
  def warehouseListWith2 = Map("78941132" -> warehouse1, "11111111" -> warehouse2)
  def userAnswersWith1Warehouse = emptyUserAnswers.copy(warehouseList = warehouseListWith1)
  def userAnswersWith2Warehouses = emptyUserAnswers.copy(warehouseList = warehouseListWith2)

  val radioYes = true
  val radioNo = false

  val fullExampleUserAnswers = {
    emptyUserAnswers
      .set(VerifyPage, YesRegister).success.value
      .set(OrganisationTypePage, LimitedCompany).success.value
      .set(HowManyLitresGloballyPage, Large).success.value
      .set(OperatePackagingSitesPage, false).success.value
      .set(ContractPackingPage, false).success.value
      .set(ImportsPage, false).success.value
      .set(StartDatePage, LocalDate.now()).success.value
      .set(PackAtBusinessAddressPage, true).success.value
      .set(PackagingSiteDetailsPage, true).success.value
      .set(AskSecondaryWarehousesPage, true).success.value
      .set(WarehouseDetailsPage, true).success.value
      .set(ContactDetailsPage, contactDetails).success.value
  }

  val largeProducerImportsTrueUserAnswers: UserAnswers = {
    emptyUserAnswers
      .set(VerifyPage, YesRegister).success.value
      .set(OrganisationTypePage, LimitedCompany).success.value
      .set(HowManyLitresGloballyPage, Large).success.value
      .set(OperatePackagingSitesPage, false).success.value
      .set(ContractPackingPage, true).success.value
      .set(HowManyContractPackingPage, LitresInBands(1,1)).success.value
      .set(ImportsPage, true).success.value
  }

  val largeProducerNoPackagingRouteUserAnswers: UserAnswers = {
    emptyUserAnswers
      .set(VerifyPage, YesRegister).success.value
      .set(OrganisationTypePage, LimitedCompany).success.value
      .set(HowManyLitresGloballyPage, Large).success.value
      .set(OperatePackagingSitesPage, false).success.value
      .set(ContractPackingPage, false).success.value
      .set(ImportsPage, false).success.value
      .set(StartDatePage, LocalDate.now()).success.value
  }

  val smallProducerUserAnswers: UserAnswers = {
    emptyUserAnswers
      .set(VerifyPage, YesRegister).success.value
      .set(OrganisationTypePage, LimitedCompany).success.value
      .set(HowManyLitresGloballyPage, Small).success.value
      .set(ThirdPartyPackagersPage, true).success.value
      .set(OperatePackagingSitesPage, false).success.value
      .set(ContractPackingPage, true).success.value
      .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
      .set(ImportsPage, false).success.value
      .set(StartDatePage, LocalDate.now()).success.value
      .set(PackAtBusinessAddressPage, true).success.value
      .set(PackagingSiteDetailsPage, true).success.value
      .set(AskSecondaryWarehousesPage, true).success.value
      .set(WarehouseDetailsPage, true).success.value
      .set(ContactDetailsPage, contactDetails).success.value
  }

  val smallProducerNoPackagingRouteUserAnswers: UserAnswers = {
    emptyUserAnswers
      .set(VerifyPage, YesRegister).success.value
      .set(OrganisationTypePage, LimitedCompany).success.value
      .set(HowManyLitresGloballyPage, Small).success.value
      .set(ThirdPartyPackagersPage, true).success.value
      .set(OperatePackagingSitesPage, false).success.value
      .set(ContractPackingPage, false).success.value
      .set(ImportsPage, true).success.value
      .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
      .set(StartDatePage, LocalDate.now()).success.value
      .set(AskSecondaryWarehousesPage, true).success.value
      .set(WarehouseDetailsPage, true).success.value
      .set(ContactDetailsPage, contactDetails).success.value
  }

  val smallProducerDoNotRegisterUserAnswers: UserAnswers = {
    emptyUserAnswers
      .set(VerifyPage, YesRegister).success.value
      .set(OrganisationTypePage, LimitedCompany).success.value
      .set(HowManyLitresGloballyPage, Small).success.value
      .set(ThirdPartyPackagersPage, false).success.value
      .set(OperatePackagingSitesPage, false).success.value
      .set(ContractPackingPage, false).success.value
      .set(ImportsPage, false).success.value
  }

  val nonProducerUserAnswers: UserAnswers = {
    emptyUserAnswers
      .set(VerifyPage, YesRegister).success.value
      .set(OrganisationTypePage, LimitedCompany).success.value
      .set(HowManyLitresGloballyPage, HowManyLitresGlobally.None).success.value
      .set(ContractPackingPage, true).success.value
      .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
      .set(ImportsPage, false).success.value
      .set(StartDatePage, LocalDate.now()).success.value
      .set(PackAtBusinessAddressPage, true).success.value
      .set(PackagingSiteDetailsPage, true).success.value
      .set(AskSecondaryWarehousesPage, true).success.value
      .set(WarehouseDetailsPage, true).success.value
      .set(ContactDetailsPage, contactDetails).success.value
  }

  val nonProducerNoPackagingRouteUserAnswers: UserAnswers = {
    emptyUserAnswers
      .set(VerifyPage, YesRegister).success.value
      .set(OrganisationTypePage, LimitedCompany).success.value
      .set(HowManyLitresGloballyPage, HowManyLitresGlobally.None).success.value
      .set(ContractPackingPage, false).success.value
      .set(ImportsPage, true).success.value
      .set(HowManyImportsPage, LitresInBands(1, 1)).success.value
      .set(StartDatePage, LocalDate.now()).success.value
      .set(AskSecondaryWarehousesPage, true).success.value
      .set(WarehouseDetailsPage, true).success.value
      .set(ContactDetailsPage, contactDetails).success.value
  }

  val nonProducerDeregisterUserAnswers: UserAnswers = {
    emptyUserAnswers
      .set(VerifyPage, YesRegister).success.value
      .set(OrganisationTypePage, LimitedCompany).success.value
      .set(HowManyLitresGloballyPage, HowManyLitresGlobally.None).success.value
      .set(ContractPackingPage, false).success.value
      .set(ImportsPage, false).success.value
  }

}
