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

package models

import base.SpecBase
import models.alf.AddressResponseForLookupState
import models.backend.{ Site, UkAddress }
import services.AddressLookupState
import services.AddressLookupState.PackingDetails

class UserAnswersSpec extends SpecBase {

  val userAnswers = UserAnswers("testing", RegisterState.RegisterWithAuthUTR)
  val ukAddress = UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some("bar"))
  val diffAddress = UkAddress(List("29 Station Pl.", "The Railyard", "Cambridge"), "CB1 2FP")
  val sdilId = "123456"
  val tradingName = "Sugary Lemonade"
  val alfResponseForLookupState = AddressResponseForLookupState(ukAddress, PackingDetails, sdilId)
  val userAnswersWithALFState = userAnswers.copy(alfResponseForLookupState = Some(alfResponseForLookupState))

  "setAlfResponse" - {
    AddressLookupState.values.foreach { addressLookupState =>
      s"when provided with a ukAddress and addressLookupState of $addressLookupState" - {
        "should add the alfResponseForLookupState to userAnswer when none exists" in {
          val expectedUserAnswers = userAnswers.copy(alfResponseForLookupState = Some(AddressResponseForLookupState(ukAddress, addressLookupState, sdilId)))
          val res = userAnswers.setAlfResponse(ukAddress, addressLookupState, sdilId)
          res mustBe expectedUserAnswers
        }

        "should override the alfResponseForLookupState to userAnswer when one already exists" in {
          val userAnswerWithAlfResponseForLookupState = userAnswers.copy(alfResponseForLookupState = Some(AddressResponseForLookupState(diffAddress, addressLookupState, "23456")))
          val expectedUserAnswers = userAnswers.copy(alfResponseForLookupState = Some(AddressResponseForLookupState(ukAddress, addressLookupState, sdilId)))
          val res = userAnswerWithAlfResponseForLookupState.setAlfResponse(ukAddress, addressLookupState, sdilId)
          res mustBe expectedUserAnswers
        }
      }
    }
  }

  "addPackagingSite" - {
    "when there is no alfResponseWithState in the userAnswers" - {
      "should create a packaging site from the ukAddress" - {
        "and add it to the packagingSites" - {
          "when no packaging sites currently exist" in {
            val expectedUserAnswers = userAnswers.copy(packagingSiteList = Map(sdilId -> Site(ukAddress, None, tradingName, None)))
            val res = userAnswers.addPackagingSite(ukAddress, tradingName, sdilId)
            res mustBe expectedUserAnswers
          }

          "when packaging sites currently exist but the sdilRef is different" in {
            val userAnswersWithPackingSitesDiffSdilRef = userAnswers.copy(packagingSiteList = Map("234567" -> packingSiteAddress45Characters))
            val expectedUserAnswers = userAnswers.copy(packagingSiteList = Map("234567" -> packingSiteAddress45Characters, sdilId -> Site(ukAddress, None, tradingName, None)))
            val res = userAnswersWithPackingSitesDiffSdilRef.addPackagingSite(ukAddress, tradingName, sdilId)
            res mustBe expectedUserAnswers
          }
        }

        "and replace the packaging site that has the same sdilRef" in {
          val userAnswersWithPackingSitesSameSdilRef = userAnswers.copy(packagingSiteList = Map(sdilId -> packingSiteAddress45Characters))
          val expectedUserAnswers = userAnswers.copy(packagingSiteList = Map(sdilId -> Site(ukAddress, None, tradingName, None)))
          val res = userAnswersWithPackingSitesSameSdilRef.addPackagingSite(ukAddress, tradingName, sdilId)
          res mustBe expectedUserAnswers
        }
      }
    }

    "when there is a alfResponseForLookupState in the userAnswers" - {
      "should remove the alfResponseForLookupState, create a packaging site from the ukAddress" - {
        "and add it to the packagingSites" - {
          "when no packaging sites currently exist" in {
            val expectedUserAnswers = userAnswers.copy(
              packagingSiteList = Map(sdilId -> Site(ukAddress, None, tradingName, None)))
            val res = userAnswersWithALFState.addPackagingSite(ukAddress, tradingName, sdilId)
            res mustBe expectedUserAnswers
          }

          "when packaging sites currently exist but the sdilRef is different" in {
            val userAnswersWithPackingSitesDiffSdilRef = userAnswersWithALFState.copy(
              packagingSiteList = Map("234567" -> packingSiteAddress45Characters))
            val expectedUserAnswers = userAnswers.copy(packagingSiteList = Map("234567" -> packingSiteAddress45Characters, sdilId -> Site(ukAddress, None, tradingName, None)))
            val res = userAnswersWithPackingSitesDiffSdilRef.addPackagingSite(ukAddress, tradingName, sdilId)
            res mustBe expectedUserAnswers
          }
        }

        "and replace the packaging site that has the same sdilRef" in {
          val userAnswersWithPackingSitesSameSdilRef = userAnswersWithALFState.copy(
            packagingSiteList = Map(sdilId -> packingSiteAddress45Characters))
          val expectedUserAnswers = userAnswers.copy(packagingSiteList = Map(sdilId -> Site(ukAddress, None, tradingName, None)))
          val res = userAnswersWithPackingSitesSameSdilRef.addPackagingSite(ukAddress, tradingName, sdilId)
          res mustBe expectedUserAnswers
        }
      }
    }
  }

  "addWarehouse" - {
    "when there is no alfResponseForLookupState in the userAnswers" - {
      "should create a warehouse from the ukAddress" - {
        "and add it to the warehouses" - {
          "when no warehouses currently exist" in {
            val expectedUserAnswers = userAnswers.copy(warehouseList = Map(sdilId -> Warehouse(tradingName, ukAddress)))
            val res = userAnswers.addWarehouse(ukAddress, tradingName, sdilId)
            res mustBe expectedUserAnswers
          }

          "when warehouses currently exist but the sdilId is different" in {
            val userAnswersWithWarehouseDiffSdilRef = userAnswers.copy(warehouseList = Map("234567" -> warehouse1))
            val expectedUserAnswers = userAnswers.copy(warehouseList = Map("234567" -> warehouse1, sdilId -> Warehouse(tradingName, ukAddress)))
            val res = userAnswersWithWarehouseDiffSdilRef.addWarehouse(ukAddress, tradingName, sdilId)
            res mustBe expectedUserAnswers
          }
        }

        "and replace the warehouse that has the same sdilId" in {
          val userAnswersWithWarehouseSameSdilRef = userAnswers.copy(warehouseList = Map(sdilId -> warehouse1))
          val expectedUserAnswers = userAnswers.copy(warehouseList = Map(sdilId -> Warehouse(tradingName, ukAddress)))
          val res = userAnswersWithWarehouseSameSdilRef.addWarehouse(ukAddress, tradingName, sdilId)
          res mustBe expectedUserAnswers
        }
      }
    }

    "when there is alfResponseForLookupState in the userAnswers" - {
      "should remove the alfResponseForLookupState, create a warehouse from the ukAddress" - {
        "and add it to the warehouses" - {
          "when no warehouses currently exist" in {
            val expectedUserAnswers = userAnswers.copy(
              warehouseList = Map(sdilId -> Warehouse(tradingName, ukAddress)))
            val res = userAnswersWithALFState.addWarehouse(ukAddress, tradingName, sdilId)
            res mustBe expectedUserAnswers
          }

          "when warehouses currently exist but the sdilId is different" in {
            val userAnswersWithWarehouseDiffSdilRef = userAnswersWithALFState.copy(
              warehouseList = Map("234567" -> warehouse1))
            val expectedUserAnswers = userAnswers.copy(warehouseList = Map("234567" -> warehouse1, sdilId -> Warehouse(tradingName, ukAddress)))
            val res = userAnswersWithWarehouseDiffSdilRef.addWarehouse(ukAddress, tradingName, sdilId)
            res mustBe expectedUserAnswers
          }
        }

        "and replace the warehouse that has the same sdilId" in {
          val userAnswersWithWarehouseSameSdilRef = userAnswersWithALFState.copy(
            warehouseList = Map(sdilId -> warehouse1))
          val expectedUserAnswers = userAnswers.copy(warehouseList = Map(sdilId -> Warehouse(tradingName, ukAddress)))
          val res = userAnswersWithWarehouseSameSdilRef.addWarehouse(ukAddress, tradingName, sdilId)
          res mustBe expectedUserAnswers
        }
      }
    }
  }

  "setBusinessAddress" - {
    "should set the address to the provided business address" - {
      "when no address currently exists" in {
        val expectedUserAnswers = userAnswers.copy(address = Some(ukAddress))
        val res = userAnswers.setBusinessAddress(ukAddress)
        res mustBe expectedUserAnswers
      }
      "when an address already exists" in {
        val userAnswersWithAddress = userAnswers.copy(address = Some(diffAddress))
        val expectedUserAnswers = userAnswers.copy(address = Some(ukAddress))
        val res = userAnswersWithAddress.setBusinessAddress(ukAddress)
        res mustBe expectedUserAnswers
      }
    }
  }

}
