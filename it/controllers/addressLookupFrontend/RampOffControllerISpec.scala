package controllers.addressLookupFrontend

import controllers.ControllerITTestHelper
import models.alf.AddressResponseForLookupState
import models.backend.{Site, UkAddress}
import models.{CheckMode, Mode, NormalMode, Warehouse}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.{INTERNAL_SERVER_ERROR, SEE_OTHER}
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import services.AddressLookupState
import services.AddressLookupState._


class RampOffControllerISpec extends ControllerITTestHelper {

  val sdilId: String = "foo"
  val alfId: String = "bar"
  val modes = List(NormalMode, CheckMode)

  def getOffRampUrl(addressLookupState: AddressLookupState, mode: Mode): String = {
    val path = addressLookupState match {
      case BusinessAddress if mode == NormalMode => s"/off-ramp/new-contact-address/$sdilId?id=$alfId"
      case BusinessAddress => s"/off-ramp/change-new-contact-address/$sdilId?id=$alfId"
      case PackingDetails if mode == NormalMode => s"/off-ramp/packing-site-details/$sdilId?id=$alfId"
      case PackingDetails => s"/off-ramp/change-packing-site-details/$sdilId?id=$alfId"
      case _ if mode == NormalMode => s"/off-ramp/warehouses/$sdilId?id=$alfId"
      case _ => s"/off-ramp/change-warehouses/$sdilId?id=$alfId"    }
    s"$baseUrl$path"
  }

  modes.foreach { mode =>
    s"In $mode" - {
      s"ramp off $BusinessAddress" - {
        val expectedRedirectLocation = if (mode == NormalMode) {
          controllers.routes.OrganisationTypeController.onPageLoad(mode).url
        } else {
          controllers.routes.CheckYourAnswersController.onPageLoad.url
        }
        "when ALF returns a valid address with a trading name" - {
          "should add the address to useranswers and redirect to the next page" - {
            "when no address exists in the database" in {
              given
                .commonPrecondition
                .alf.getAddress(alfId, true)
              setAnswers(emptyUserAnswers)

              WsTestClient.withClient { client =>
                val result = createClientRequestGet(client, getOffRampUrl(BusinessAddress, mode))

                whenReady(result) { res =>
                  val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
                  updatedUserAnswers.id mustBe emptyUserAnswers.id
                  updatedUserAnswers.data mustBe emptyUserAnswers.data
                  updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
                  updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
                  updatedUserAnswers.address mustBe Some(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)))
                  updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList

                  res.status mustBe SEE_OTHER
                  res.header(HeaderNames.LOCATION) mustBe Some(expectedRedirectLocation)
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
                val result = createClientRequestGet(client, getOffRampUrl(BusinessAddress, mode))

                whenReady(result) { res =>
                  val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
                  updatedUserAnswers.id mustBe emptyUserAnswers.id
                  updatedUserAnswers.data mustBe emptyUserAnswers.data
                  updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
                  updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
                  updatedUserAnswers.address mustBe Some(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)))
                  updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList

                  res.status mustBe SEE_OTHER
                  res.header(HeaderNames.LOCATION) mustBe Some(expectedRedirectLocation)
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
              val userAnswersBefore = emptyUserAnswers.copy(address = Some(UkAddress(List.empty, "", Some("foo"))))

              setAnswers(userAnswersBefore)

              WsTestClient.withClient { client =>
                val result = createClientRequestGet(client, getOffRampUrl(BusinessAddress, mode))

                whenReady(result) { res =>
                  val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
                  updatedUserAnswers.id mustBe emptyUserAnswers.id
                  updatedUserAnswers.data mustBe emptyUserAnswers.data
                  updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
                  updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
                  updatedUserAnswers.address mustBe Some(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)))
                  updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList

                  res.status mustBe SEE_OTHER
                  res.header(HeaderNames.LOCATION) mustBe Some(expectedRedirectLocation)
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
                val result = createClientRequestGet(client, getOffRampUrl(BusinessAddress, mode))

                whenReady(result) { res =>
                  val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
                  updatedUserAnswers.id mustBe emptyUserAnswers.id
                  updatedUserAnswers.data mustBe emptyUserAnswers.data
                  updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
                  updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
                  updatedUserAnswers.address mustBe Some(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId)))
                  updatedUserAnswers.warehouseList mustBe emptyUserAnswers.warehouseList

                  res.status mustBe SEE_OTHER
                  res.header(HeaderNames.LOCATION) mustBe Some(expectedRedirectLocation)
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
              val result = createClientRequestGet(client, getOffRampUrl(BusinessAddress, mode))

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
                val result = createClientRequestGet(client, getOffRampUrl(WarehouseDetails, mode))

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
                  res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehousesTradingNameController.onPageLoad(mode).url)
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
                val result = createClientRequestGet(client, getOffRampUrl(WarehouseDetails, mode))

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
                  res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehousesTradingNameController.onPageLoad(mode).url)
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
              val result = createClientRequestGet(client, getOffRampUrl(WarehouseDetails, mode))

              whenReady(result) { res =>
                val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get

                updatedUserAnswers.id mustBe emptyUserAnswers.id
                updatedUserAnswers.data mustBe emptyUserAnswers.data
                updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
                updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
                updatedUserAnswers.warehouseList mustBe Map(sdilId -> Warehouse("soft drinks ltd", UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId))))
                updatedUserAnswers.address mustBe emptyUserAnswers.address

                res.status mustBe SEE_OTHER
                res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehouseDetailsController.onPageLoad(mode).url)
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
              val result = createClientRequestGet(client, getOffRampUrl(WarehouseDetails, mode))

              whenReady(result) { res =>
                val updatedUserAnswers = getAnswers(emptyUserAnswers.id).get
                updatedUserAnswers.id mustBe emptyUserAnswers.id
                updatedUserAnswers.data mustBe emptyUserAnswers.data
                updatedUserAnswers.packagingSiteList mustBe emptyUserAnswers.packagingSiteList
                updatedUserAnswers.submittedOn mustBe emptyUserAnswers.submittedOn
                updatedUserAnswers.warehouseList mustBe Map(sdilId -> Warehouse("soft drinks ltd", UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId))))
                updatedUserAnswers.address mustBe emptyUserAnswers.address

                res.status mustBe SEE_OTHER
                res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.WarehouseDetailsController.onPageLoad(mode).url)
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
              val result = createClientRequestGet(client, getOffRampUrl(WarehouseDetails, mode))

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
                val result = createClientRequestGet(client, getOffRampUrl(PackingDetails, mode))

                whenReady(result) { res =>
                  res.status mustBe SEE_OTHER
                  res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.PackagingSiteNameController.onPageLoad(mode).url)
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
                val result = createClientRequestGet(client, getOffRampUrl(PackingDetails, mode))

                whenReady(result) { res =>
                  res.status mustBe SEE_OTHER
                  res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.PackagingSiteNameController.onPageLoad(mode).url)
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
            given
              .commonPrecondition
              .alf.getAddress(alfId)
            setAnswers(emptyUserAnswers)

            WsTestClient.withClient { client =>
              val result = createClientRequestGet(client, getOffRampUrl(PackingDetails, mode))

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
                res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.PackagingSiteDetailsController.onPageLoad(mode).url)
              }

            }
          }
          "an address already exists in DB currently for SDILID provided" in {
            val userAnswersBefore = emptyUserAnswers.copy(packagingSiteList = Map(sdilId -> Site(UkAddress(List.empty, "foo", Some("wizz")), None, aTradingName, None)))
            given
              .commonPrecondition
              .alf.getAddress(alfId)
            setAnswers(userAnswersBefore)

            WsTestClient.withClient { client =>
              val result = createClientRequestGet(client, getOffRampUrl(PackingDetails, mode))

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
                res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.PackagingSiteDetailsController.onPageLoad(mode).url)
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
              val result = createClientRequestGet(client, getOffRampUrl(PackingDetails, mode))

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
  }
}
