package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{get, serverError, stubFor, urlPathMatching}
import errors.UnexpectedResponseFromSDIL
import org.scalatest.matchers.should.Matchers._
import play.api.http.Status.{ACCEPTED, NOT_FOUND, OK}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import uk.gov.hmrc.http.HeaderCarrier

class SoftDrinksIndustryLevyConnectorISpec extends Specifications with TestConfiguration with ITCoreTestData with FutureAwaits with DefaultAwaitTimeout {

  val connector: SoftDrinksIndustryLevyConnector = app.injector.instanceOf[SoftDrinksIndustryLevyConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "checkPendingQueue" - {
    s"should return $Registered when $OK returned" in {
      build.sdilBackend.checkPendingQueueRegistered("utr")

      val response = connector.checkPendingQueue("utr")
      whenReady(response.value) { res =>
        res shouldBe Right(Registered)
      }
    }
    s"should return $Pending when $ACCEPTED returned" in {
      build.sdilBackend.checkPendingQueuePending("utr")

      val response = connector.checkPendingQueue("utr")
      whenReady(response.value) { res =>
        res shouldBe Right(Pending)
      }
    }
    s"should return $DoesNotExist when $NOT_FOUND in" in {
      build.sdilBackend.checkPendingQueueDoesntExist("utr")

      val response = connector.checkPendingQueue("utr")
      whenReady(response.value) { res =>
        res shouldBe Right(DoesNotExist)
      }
    }
    s"should throw exception when other status returned" in {
      stubFor(
        get(
          urlPathMatching(s"/check-enrolment-status/utr"))
          .willReturn(serverError())
      )
      val response = connector.checkPendingQueue("utr")
      whenReady(response.value) { res =>
        res shouldBe Left(UnexpectedResponseFromSDIL)
      }
    }
  }
}
