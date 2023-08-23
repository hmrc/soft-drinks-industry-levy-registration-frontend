package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{get, status, stubFor, urlPathMatching}
import errors.UnexpectedResponseFromSDIL
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.{ACCEPTED, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import testSupport.{ITCoreTestData, Specifications, TestConfiguration}
import uk.gov.hmrc.http.HeaderCarrier

class SoftDrinksIndustryLevyConnectorISpec extends Specifications with TestConfiguration with ITCoreTestData with FutureAwaits with DefaultAwaitTimeout {

  val connector: SoftDrinksIndustryLevyConnector = app.injector.instanceOf[SoftDrinksIndustryLevyConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "checkPendingQueue" - {
    s"should return $Registered when $OK returned" in {
      given.sdilBackend.checkPendingQueueRegistered("utr")

      val response = connector.checkPendingQueue("utr")
      whenReady(response.value) { res =>
        res shouldBe Right(Registered)
      }
    }
    s"should return $Pending when $ACCEPTED returned" in {
      given.sdilBackend.checkPendingQueuePending("utr")

      val response = connector.checkPendingQueue("utr")
      whenReady(response.value) { res =>
        res shouldBe Right(Pending)
      }
    }
    s"should return $DoesNotExist when $NOT_FOUND in" in {
      given.sdilBackend.checkPendingQueueDoesntExist("utr")

      val response = connector.checkPendingQueue("utr")
      whenReady(response.value) { res =>
        res shouldBe Right(DoesNotExist)
      }
    }
    s"should throw exception when other status returned" in {
      stubFor(
        get(
          urlPathMatching(s"/check-enrolment-status/utr"))
          .willReturn(status(INTERNAL_SERVER_ERROR))
      )
      val response = connector.checkPendingQueue("utr")
      whenReady(response.value) { res =>
        res shouldBe Left(UnexpectedResponseFromSDIL)
      }
    }
  }
}
