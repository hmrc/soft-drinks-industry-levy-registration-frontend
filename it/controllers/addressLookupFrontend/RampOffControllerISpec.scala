package controllers.addressLookupFrontend

import controllers.ControllerITTestHelper
import models.backend.{Site, UkAddress}
import models.{NormalMode, Warehouse}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.{INTERNAL_SERVER_ERROR, SEE_OTHER}
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import services.AddressLookupState._


class RampOffControllerISpec extends ControllerITTestHelper {

  s"ramp off $BusinessAddress" - {
    "should redirect to next page when request is valid and address is returned from ALF when" - {
      "no address exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        given
          .commonPrecondition
          .alf.getAddress(alfId)
        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/new-contact-address/$sdilId?id=$alfId")

          whenReady(result) { res =>
            val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
            updatedUserAnswers.id mustBe emptyUserAnswers.id
            updatedUserAnswers.data mustBe emptyUserAnswers.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
            updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
            updatedUserAnswers.address mustBe Some(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)))
            updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.OrganisationTypeController.onPageLoad(NormalMode).url)
          }

        }
      }
      "an address already exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        val userAnswersBefore = emptyUserAnswers.copy(address = Some(UkAddress(List.empty,"", Some("foo"))))
        given
          .commonPrecondition
          .alf.getAddress(alfId)
        setAnswers(userAnswersBefore)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/new-contact-address/$sdilId?id=$alfId")

          whenReady(result) { res =>
            val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
            updatedUserAnswers.id mustBe emptyUserAnswers.id
            updatedUserAnswers.data mustBe emptyUserAnswers.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
            updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
            updatedUserAnswers.address mustBe Some(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)))
            updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.OrganisationTypeController.onPageLoad(NormalMode).url)
          }
        }
      }
    }
    s"return $INTERNAL_SERVER_ERROR when" - {
      "alf returns error" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        given
          .commonPrecondition
          .alf.getBadAddress(alfId)
        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/new-contact-address/$sdilId?id=$alfId")

          whenReady(result) { res =>
            res.status mustBe INTERNAL_SERVER_ERROR

            val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
            updatedUserAnswers.id mustBe emptyUserAnswers.id
            updatedUserAnswers.data mustBe emptyUserAnswers.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
            updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
            updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList
            updatedUserAnswers.address mustBe emptyUserAnswers.address
          }
        }
      }
    }
  }
  s"ramp off $WarehouseDetails" - {
    "should redirect to next page when request is valid and address is returned from ALF when" - {
      "no address exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        given
          .commonPrecondition
          .alf.getAddress(alfId)
        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/warehouses/$sdilId?id=$alfId")

          whenReady(result) { res =>
            val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get

            println(Console.YELLOW + updatedUserAnswers.warehouseList + Console.WHITE)
            println(Console.YELLOW + emptyUserAnswers.warehouseList + Console.WHITE)

            updatedUserAnswers.id mustBe emptyUserAnswers.id
            updatedUserAnswers.data mustBe emptyUserAnswers.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
            updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
            updatedUserAnswers.warehouseList mustBe Map(sdilId -> Warehouse("soft drinks ltd", UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId))))
            updatedUserAnswers.address mustBe emptyUserAnswers.address



            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
          }

        }
      }
      "an address already exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        val userAnswersBefore = emptyUserAnswers.copy(warehouseList = Map(sdilId -> Warehouse(aTradingName, UkAddress(List.empty, "foo", Some("wizz")))))
        given
          .commonPrecondition
          .alf.getAddress(alfId)
        setAnswers(userAnswersBefore)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/warehouses/$sdilId?id=$alfId")

          whenReady(result) { res =>
            val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
            updatedUserAnswers.id mustBe emptyUserAnswers.id
            updatedUserAnswers.data mustBe emptyUserAnswers.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
            updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
            updatedUserAnswers.warehouseList mustBe Map(sdilId -> Warehouse("soft drinks ltd", UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId))))
            updatedUserAnswers.address mustBe emptyUserAnswers.address

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
          }
        }
      }
    }
    s"return $INTERNAL_SERVER_ERROR when" - {
      "alf returns error" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        given
          .commonPrecondition
          .alf.getBadAddress(alfId)
        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/warehouses/$sdilId?id=$alfId")

          whenReady(result) { res =>
            res.status mustBe INTERNAL_SERVER_ERROR

            val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
            updatedUserAnswers.id mustBe emptyUserAnswers.id
            updatedUserAnswers.data mustBe emptyUserAnswers.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
            updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
            updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList
            updatedUserAnswers.address mustBe emptyUserAnswers.address
          }
        }
      }
    }
  }
  s"ramp off $PackingDetails should" - {
    "redirect to next page when request is valid and address is returned from ALF when" - {
      "no address exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        given
          .commonPrecondition
          .alf.getAddress(alfId)
        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/packing-site-details/$sdilId?id=$alfId")

          whenReady(result) { res =>
            val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
            updatedUserAnswers.id mustBe emptyUserAnswers.id
            updatedUserAnswers.data mustBe emptyUserAnswers.data
            updatedUserAnswers.packagingSiteList mustBe Map(sdilId ->
              Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), None, "soft drinks ltd", None))
            updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
            updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList
            updatedUserAnswers.address mustBe emptyUserAnswers.address

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
          }

        }
      }
      "an address already exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        val userAnswersBefore = emptyUserAnswers.copy(packagingSiteList = Map(sdilId -> Site(UkAddress(List.empty, "foo", Some("wizz")), None, aTradingName, None)))
        given
          .commonPrecondition
          .alf.getAddress(alfId)
        setAnswers(userAnswersBefore)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/packing-site-details/$sdilId?id=$alfId")

          whenReady(result) { res =>
            val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
            updatedUserAnswers.id mustBe emptyUserAnswers.id
            updatedUserAnswers.data mustBe emptyUserAnswers.data
            updatedUserAnswers.packagingSiteList mustBe Map(sdilId ->
              Site(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), None, "soft drinks ltd", None))
            updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
            updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList
            updatedUserAnswers.address mustBe emptyUserAnswers.address

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
          }
        }
      }
    }
    s"return $INTERNAL_SERVER_ERROR when" - {
      "alf returns error" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        given
          .commonPrecondition
          .alf.getBadAddress(alfId)
        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, s"$baseUrl/off-ramp/packing-site-details/$sdilId?id=$alfId")

          whenReady(result) { res =>
            res.status mustBe INTERNAL_SERVER_ERROR

            val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
            updatedUserAnswers.id mustBe emptyUserAnswers.id
            updatedUserAnswers.data mustBe emptyUserAnswers.data
            updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
            updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
            updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList
            updatedUserAnswers.address mustBe emptyUserAnswers.address

          }
        }
      }
    }
  }
}
