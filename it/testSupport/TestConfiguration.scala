package testSupport

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{configureFor, reset, resetAllScenarios}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import config.FrontendAppConfig
import controllers.actions._
import org.mongodb.scala.bson.BsonDocument
import org.scalatest.concurrent.{IntegrationPatience, PatienceConfiguration}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite, TestSuite}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.i18n.{Lang, MessagesApi, MessagesImpl}
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{CookieHeaderEncoding, MessagesControllerComponents, Session, SessionCookieBaker}
import play.api.test.Helpers._
import play.api.{Application, Environment, Mode}
import repositories.{SDILSessionCache, SDILSessionCacheRepository, SessionRepository}
import testSupport.databases.{SDILSessionCacheOperations, SessionDatabaseOperations}
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto

import java.time.{Clock, ZoneOffset}
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import org.mongodb.scala.{ObservableFuture, SingleObservableFuture}

trait TestConfiguration
  extends GuiceOneServerPerSuite
    with IntegrationPatience
    with PatienceConfiguration
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with SessionDatabaseOperations
    with SDILSessionCacheOperations {

  me: Suite with TestSuite =>

  lazy val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  val wiremockHost: String = "localhost"
  val wiremockPort: Int = Port.randomAvailable

  val baseUrl = s"http://localhost:$port/soft-drinks-industry-levy-registration"

  val sessionId = "sessionId-eb3158c2-0aff-4ce8-8d1b-f2208ace52fe"
  val xSessionId: (String, String) = "X-Session-ID" -> sessionId
  val xRequestId: (String, String) = "X-Request-ID" -> sessionId
  val AUTHORIZE_HEADER_VALUE =
    "Bearer BXQ3/Treo4kQCZvVcCqKPhhpBYpRtQQKWTypn1WBfRHWUopu5V/IFWF5phY/fymAP1FMqQR27MmCJxb50Hi5GD6G3VMjMtSLu7TAAIuqDia6jByIpXJpqOgLQuadi7j0XkyDVkl0Zp/zbKtHiNrxpa0nVHm3+GUC4H2h4Ki8OjP9KwIkeIPK/mMlBESjue4V"

  val sessionBaker: SessionCookieBaker = app.injector.instanceOf[SessionCookieBaker]
  val cookieHeaderEncoding: CookieHeaderEncoding = app.injector.instanceOf[CookieHeaderEncoding]
  val sessionCookieCrypto: SessionCookieCrypto = app.injector.instanceOf[SessionCookieCrypto]

  def createSessionCookieAsString(sessionData: Map[String, String]): String = {
    val sessionCookie = sessionBaker.encodeAsCookie(Session(sessionData))
    val encryptedSessionCookieValue =
      sessionCookieCrypto.crypto.encrypt(PlainText(sessionCookie.value)).value
    val encryptedSessionCookie =
      sessionCookie.copy(value = encryptedSessionCookieValue)
    cookieHeaderEncoding.encodeCookieHeader(Seq(encryptedSessionCookie))
  }
  val authData = Map("authToken" -> AUTHORIZE_HEADER_VALUE)
  val sessionAndAuth  = Map("authToken" -> AUTHORIZE_HEADER_VALUE, "sessionId" -> sessionId)

  lazy val sessionRepository: SessionRepository = app.injector.instanceOf[SessionRepository]

  val authCookie: String = createSessionCookieAsString(authData).substring(5)
  val authAndSessionCookie: String = createSessionCookieAsString(sessionAndAuth).substring(5)
  abstract override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(
      timeout = Span(4, Seconds),
      interval = Span(50, Millis))

  lazy val config = Map(
    s"microservice.services.address-lookup-frontend.host" -> s"$wiremockHost",
    s"microservice.services.address-lookup-frontend.port" -> s"$wiremockPort",
    s"microservice.services.auth.host" -> s"$wiremockHost",
    s"microservice.services.auth.port" -> s"$wiremockPort",
    s"microservice.services.bas-gateway.host" -> s"$wiremockHost",
    s"microservice.services.bas-gateway.port" -> s"$wiremockPort",
    s"microservice.services.soft-drinks-industry-levy.host" -> s"$wiremockHost",
    s"microservice.services.soft-drinks-industry-levy.port" -> s"$wiremockPort",
    "play.filters.csrf.header.bypassHeaders.X-Requested-With" -> "*",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "json.encryption.key" -> "fqpLDZ4sumDsekHkeEBlCA==",
    "json.encryption.previousKeys" -> "[]",
    "play.http.router" -> "testOnlyDoNotUseInAppConf.Routes",
    "helpdeskPhoneNumber" -> "0300-200-1000",
    "addressLookupFrontendTest.enabled" -> "false"
  )

  override implicit lazy val app: Application = appBuilder().build()
  lazy val sessionCache = app.injector.instanceOf[SDILSessionCacheRepository]
  lazy val sdilSessionCache: SDILSessionCache = app.injector.instanceOf[SDILSessionCache]
  lazy val mongo: SessionRepository = app.injector.instanceOf[SessionRepository]

  def configParams: Map[String, Any] = Map()

  protected def appBuilder(): GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .in(Environment.simple(mode = Mode.Dev))
      .configure(config ++ configParams)
      .overrides(
        bind[DataRetrievalAction].to[DataRetrievalActionImpl],
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[AuthenticatedIdentifierAction],
        bind[Clock].toInstance(Clock.systemDefaultZone().withZone(ZoneOffset.UTC))
      )
  }

  //app.injector.instanceOf[HealthController]

  val wireMockServer = new WireMockServer(wireMockConfig().port(wiremockPort))

  override def beforeAll() = {
    wireMockServer.stop()
    wireMockServer.start()
    configureFor(wiremockHost, wiremockPort)
  }

  override def beforeEach() = {
    await(mongo.collection.deleteMany(BsonDocument()).toFuture())
    await(sessionCache.collection.deleteMany(BsonDocument()).toFuture())
    resetAllScenarios()
    reset()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
  }

  override def afterEach(): Unit = {
    wireMockServer.getAllServeEvents.asScala.toList
      .sortBy(_.getRequest.getLoggedDate)
      .map(_.getRequest).foreach(r => s"${r.getLoggedDate.toInstant.toEpochMilli}\t${r.getMethod}\t${r.getUrl}")
  }

  lazy val messagesAPI: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val messagesProvider: MessagesImpl = MessagesImpl(Lang("en"), messagesAPI)
  lazy val mcc = app.injector.instanceOf[MessagesControllerComponents]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
}
