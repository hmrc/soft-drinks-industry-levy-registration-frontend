package controllers.addressLookupFrontend

import controllers.ControllerITTestHelper
import models.alf.AddressResponseForLookupState
import models.backend.{Site, UkAddress}
import models.{NormalMode, Warehouse}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.{INTERNAL_SERVER_ERROR, SEE_OTHER}
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import services.AddressLookupState._


class RampOffControllerISpec extends ControllerITTestHelper {

  val sdilId: String = "foo"
  val alfId: String = "bar"

  s"ramp off $BusinessAddress" - {
    "when ALF returns a valid address with a trading name" - {
      "should add the address to useranswers and redirect to the next page" - {
        "when no address exists in the database" in {
          given
            .commonPrecondition
            .alf.getAddress(alfId, true)
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
      }

      "should override the address in useranswers and redirect to the next page" - {
        "when an address exists in the database" in {
          given
            .commonPrecondition
            .alf.getAddress(alfId, true)
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
      }
    }

    "when ALF returns a valid address with no trading name" - {
      "should add the address to useranswers and redirect to the next page" - {
        "when no address exists in the database" in {
          given
            .commonPrecondition
            .alf.getAddress(alfId, false)
          val userAnswersBefore = emptyUserAnswers.copy(address = Some(UkAddress(List.empty,"", Some("foo"))))

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

      "should override the address in useranswers and redirect to the next page" - {
        "when an address exists in the database" in {
          given
            .commonPrecondition
            .alf.getAddress(alfId, true)
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
      }
    }

    s"return $INTERNAL_SERVER_ERROR when" - {
      "alf returns error" in {
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
    "should not add the warehouse to useranswers but add a new alfResponseForLookupState" - {
      "then redirect to the trading name page when the request is valid and address is returned from ALF without a trading name and" - {
        "no warehouses or alfResponseForLookupState exist in DB currently for SDILID provided" in {
          given
            .commonPrecondition
            .alf.getAddress(alfId, false)
          setAnswers(emptyUserAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, s"$baseUrl/off-ramp/warehouses/$sdilId?id=$alfId")

            whenReady(result) { res =>
              val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get

              updatedUserAnswers.id mustBe emptyUserAnswers.id
              updatedUserAnswers.data mustBe emptyUserAnswers.data
              updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
              updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
              updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList
              updatedUserAnswers.alfResponseForLookupState mustBe Some(AddressResponseForLookupState(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), WarehouseDetails, sdilId))
              updatedUserAnswers.address mustBe emptyUserAnswers.address

              res.status mustBe SEE_OTHER
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
            }
          }
        }
        "an warehouses and alfResponseForLookupState already exists in DB currently for SDILID provided" in {
          val userAnswersBefore = emptyUserAnswers.copy(
            warehouseList = Map(sdilId -> Warehouse(aTradingName, UkAddress(List.empty, "foo", Some("wizz")))),
            alfResponseForLookupState = Some(AddressResponseForLookupState(UkAddress(List.empty, "foo", Some("wizz")), PackingDetails, sdilId)))
          given
            .commonPrecondition
            .alf.getAddress(alfId, false)
          setAnswers(userAnswersBefore)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, s"$baseUrl/off-ramp/warehouses/$sdilId?id=$alfId")

            whenReady(result) { res =>
              val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
              updatedUserAnswers.id mustBe emptyUserAnswers.id
              updatedUserAnswers.data mustBe emptyUserAnswers.data
              updatedUserAnswers.packagingSiteList mustBe userAnswersBefore.packagingSiteList
              updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
              updatedUserAnswers.warehouseList mustBe userAnswersBefore.warehouseList
              updatedUserAnswers.alfResponseForLookupState mustBe Some(AddressResponseForLookupState(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), WarehouseDetails, sdilId))
              updatedUserAnswers.address mustBe emptyUserAnswers.address

              res.status mustBe SEE_OTHER
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
            }
          }
        }
      }
    }
    "should redirect to warehouse details page when the request is valid and address is returned from ALF with a trading name and" - {
      "no address exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        given
          .commonPrecondition
          .alf.getAddress(alfId, true)
        setAnswers(emptyUserAnswers)

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
      "an address already exists in DB currently for SDILID provided" in {
        val sdilId: String = "foo"
        val alfId: String = "bar"
        val userAnswersBefore = emptyUserAnswers.copy(warehouseList = Map(sdilId -> Warehouse(aTradingName, UkAddress(List.empty, "foo", Some("wizz")))))
        given
          .commonPrecondition
          .alf.getAddress(alfId, true)
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
    "should not add the packagingSite to useranswers but add a new alfResponseForLookupState" - {
      "then redirect to the trading name page when the request is valid and address is returned from ALF without a trading name and" - {
        "no packaging sites or alfResponseForLookupState exist in DB currently for SDILID provided" in {
          given
            .commonPrecondition
            .alf.getAddress(alfId, false)
          setAnswers(emptyUserAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, s"$baseUrl/off-ramp/packing-site-details/$sdilId?id=$alfId")

            whenReady(result) { res =>
              res.status mustBe SEE_OTHER
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
              val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get

              updatedUserAnswers.id mustBe emptyUserAnswers.id
              updatedUserAnswers.data mustBe emptyUserAnswers.data
              updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
              updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
              updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList
              updatedUserAnswers.alfResponseForLookupState mustBe Some(AddressResponseForLookupState(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), PackingDetails, sdilId))
              updatedUserAnswers.address mustBe emptyUserAnswers.address
            }
          }
        }
        "an packaging site and alfResponseForLookupState already exists in DB currently for SDILID provided" in {
          val userAnswersBefore = emptyUserAnswers.copy(
            packagingSiteList = Map(sdilId -> Site(UkAddress(List.empty, "foo", Some("wizz")), None, aTradingName, None)),
            alfResponseForLookupState = Some(AddressResponseForLookupState(UkAddress(List.empty, "foo", Some("wizz")), WarehouseDetails, sdilId)))
          given
            .commonPrecondition
            .alf.getAddress(alfId, false)
          setAnswers(userAnswersBefore)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, s"$baseUrl/off-ramp/packing-site-details/$sdilId?id=$alfId")

            whenReady(result) { res =>
              res.status mustBe SEE_OTHER
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
              val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
              updatedUserAnswers.id mustBe emptyUserAnswers.id
              updatedUserAnswers.data mustBe emptyUserAnswers.data
              updatedUserAnswers.packagingSiteList mustBe userAnswersBefore.packagingSiteList
              updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
              updatedUserAnswers.warehouseList mustBe userAnswersBefore.warehouseList
              updatedUserAnswers.alfResponseForLookupState mustBe Some(AddressResponseForLookupState(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)), PackingDetails, sdilId))
              updatedUserAnswers.address mustBe emptyUserAnswers.address

            }
          }
        }
      }
    }

    "redirect to packaging site details page when request is valid and address is returned from ALF with a trading name and" - {
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
