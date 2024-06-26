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

package base

import base.SpecBase.aTradingName
import cats.data.EitherT
import cats.implicits._
import config.FrontendAppConfig
import controllers.actions._
import controllers.routes
import errors.RegistrationErrors
import models.backend.{ Site, UkAddress }
import models.{ Contact, IndividualDetails, LitresInBands, OrganisationDetails, RegisterState, RetrievedActivity, RetrievedSubscription, RosmRegistration, RosmWithUtr, UserAnswers, Warehouse }
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{ BeforeAndAfterEach, OptionValues, TryValues }
import play.api.i18n.{ Lang, Messages, MessagesApi, MessagesImpl }
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Writes
import play.api.mvc.{ Call, MessagesControllerComponents }
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import play.api.{ Application, Play }
import queries.Settable
import service.RegistrationResult
import uk.gov.hmrc.http.HeaderCarrier
import utilities.GenericLogger

import java.time.LocalDate
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Try }

object SpecBase {
  val aTradingName = "Wild Lemonade Group"
  val aSubscription: RetrievedSubscription = RetrievedSubscription(
    utr = "0000000022",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None)

}

trait SpecBase
  extends AnyFreeSpec
  with Matchers
  with TryValues
  with OptionValues
  with ScalaFutures
  with IntegrationPatience
  with BeforeAndAfterEach {

  def createSuccessRegistrationResult[T](result: T): RegistrationResult[T] =
    EitherT.right[RegistrationErrors](Future.successful(result))

  def createFailureRegistrationResult[T](error: RegistrationErrors): RegistrationResult[T] =
    EitherT.left(Future.successful(error))

  def recoveryCall: Call = routes.JourneyRecoveryController.onPageLoad()

  def identifier: String = "id"
  val sdilNumber: String = "XKSDIL000000022"

  lazy val application: Application = applicationBuilder(userAnswers = None, rosmRegistration = rosmRegistration).build()
  implicit lazy val messagesAPI: MessagesApi = application.injector.instanceOf[MessagesApi]
  implicit lazy val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesAPI)
  lazy val mcc: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  implicit lazy val frontendAppConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
  val utr = "1234567891"
  val rosmRegistration: RosmWithUtr = RosmWithUtr(utr, RosmRegistration(
    safeId = "safeid",
    organisation = Some(OrganisationDetails(organisationName = "Super Lemonade Plc")),
    individual = Some(IndividualDetails(firstName = "Ava", lastName = "Adams")),
    address = UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL")))
  lazy val logger: GenericLogger = application.injector.instanceOf[GenericLogger]
  implicit lazy val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def afterEach(): Unit = {
    Play.stop(application)
    super.afterEach()
  }
  val emptyUserAnswers: UserAnswers = UserAnswers(identifier, RegisterState.RegisterWithAuthUTR)

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(hasCTEnrolment: Boolean = false, utr: Option[String] = None, userAnswers: Option[UserAnswers] = None, rosmRegistration: RosmWithUtr = rosmRegistration): GuiceApplicationBuilder = {
    val bodyParsers = stubControllerComponents().parsers.defaultBodyParser
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].toInstance(new FakeIdentifierAction(bodyParsers, hasCTEnrolment, utr)),
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[DataRequiredAction].toInstance(new FakeDataRequiredAction(rosmRegistration, userAnswers)))
  }

  val packingSiteAddress45Characters = Site(
    UkAddress(List("29 Station Pl.", "The Railyard", "Cambridge"), "CB1 2FP"),
    None,
    aTradingName,
    None)

  val packingSiteAddress47Characters = Site(
    UkAddress(List("29 Station Place", "The Railyard", "Cambridge"), "CB1 2FP"),
    Some("10"),
    aTradingName,
    None)

  val packingSiteAddress49Characters = Site(
    UkAddress(List("29 Station PlaceDr", "The Railyard", "Cambridge"), "CB1 2FP"),
    None,
    aTradingName,
    None)

  def packagingSiteListWith3 = Map(("12345678", packingSiteAddress45Characters), ("23456789", packingSiteAddress47Characters), ("34567890", packingSiteAddress49Characters))

  def warehouse1 = Warehouse("Warehouse One", UkAddress(List("29 Station Place", "The Railyard", "Cambridge"), "CB1 2FP"))
  def warehouse2 = Warehouse("Warehouse Two", UkAddress(List("42 Hitch Place", "The Railyard", "Cambridge"), "CB1 2FF"))
  def warehouseListWith1 = Map("78941132" -> warehouse1)
  def warehouseListWith2 = Map("78941132" -> warehouse1, "11111111" -> warehouse2)

  val aSubscription = RetrievedSubscription(
    utr = "0000000022",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None)

  val userDetailsWithSetMethodsReturningFailure: UserAnswers = new UserAnswers("sdilId", RegisterState.RegisterWithAuthUTR) {
    override def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = Failure[UserAnswers](new Exception(""))

    override def setAndRemoveLitresIfReq(page: Settable[Boolean], litresPage: Settable[LitresInBands], value: Boolean)(implicit writes: Writes[Boolean]): Try[UserAnswers] = Failure[UserAnswers](new Exception(""))
  }
}
