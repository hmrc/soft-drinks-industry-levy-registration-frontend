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
import base.SpecBase.aTradingName
import models.alf.{AddressResponseForLookupState, AlfAddress, AlfResponse}
import models.backend.{Site, UkAddress}
import play.api.libs.json.{JsObject, Json, Reads}
import repositories.DatedCacheMap
import services.AddressLookupState.WarehouseDetails
import services.Encryption

import java.time.{Instant, LocalDate}

class ModelEncryptionSpec extends SpecBase {

  implicit val encryption: Encryption = application.injector.instanceOf[Encryption]
  val alfAddress = AlfResponse(AlfAddress(Some("foo"), List.empty, None, None))

  "encryptUserAnswers" - {
    "should encrypt userAnswers" in {
      val alfId: String = "bar"
      val userAnswers = UserAnswers("id", RegisterState.RegisterWithAuthUTR,
        Json.obj("foo" -> "bar"),
        Some(UkAddress(List("Line 1", "Line 2", "Line 3", "Line 4"),"aa1 1aa", alfId = Some(alfId))),
        Map("foo" -> Site(UkAddress(List("foo"),"foo", Some("foo")),Some("foo"), aTradingName, Some(LocalDate.now()))),
        Map("foo" -> Warehouse(aTradingName,UkAddress(List("foo"),"foo", Some("foo")))),
        Some(AddressResponseForLookupState(alfAddress, WarehouseDetails)),
        Some(Instant.ofEpochSecond(1)),
        Instant.ofEpochSecond(1))

      val result = ModelEncryption.encryptUserAnswers(userAnswers)
      result._1 mustBe userAnswers.id
      result._2 mustBe userAnswers.registerState
      Json.parse(encryption.crypto.decrypt(result._3, userAnswers.id)).as[JsObject] mustBe userAnswers.data
      Json.fromJson[Option[UkAddress]](Json.parse(encryption.crypto.decrypt(result._4, userAnswers.id)))(Reads.optionWithNull[UkAddress]).get mustBe userAnswers.address
      Json.parse(encryption.crypto.decrypt(result._5.head._2, userAnswers.id)).as[Site] mustBe userAnswers.packagingSiteList.head._2
      result._6.head._1 mustBe userAnswers.packagingSiteList.head._1
      Json.parse(encryption.crypto.decrypt(result._5.head._2, userAnswers.id)).as[Warehouse] mustBe userAnswers.warehouseList.head._2
      result._6.head._1 mustBe userAnswers.warehouseList.head._1
      result._7.map(encrytedVal => Json.parse(encryption.crypto.decrypt(encrytedVal, userAnswers.id)).as[AddressResponseForLookupState]) mustBe userAnswers.alfResponseForLookupState
      result._8 mustBe userAnswers.submittedOn
      result._9 mustBe userAnswers.lastUpdated
    }
  }
  "decryptUserAnswers" - {
    "should decrypt userAnswers in tuple form" in {
      val alfId: String = "bar"
      val userAnswers = UserAnswers("id", RegisterState.RegisterWithAuthUTR,
        Json.obj("foo" -> "bar"),
        Some(UkAddress(List("Line 1", "Line 2", "Line 3", "Line 4"),"aa1 1aa", alfId = Some(alfId))),
        Map("foo" -> Site(UkAddress(List("foo"),"foo", Some("foo")),Some("foo"), aTradingName,Some(LocalDate.now()))),
        Map("foo" -> Warehouse(aTradingName,UkAddress(List("foo"),"foo", Some("foo")))),
        Some(AddressResponseForLookupState(alfAddress, WarehouseDetails)),
        None,
        Instant.ofEpochSecond(1))

     val result = ModelEncryption.decryptUserAnswers(
        userAnswers.id, RegisterState.RegisterWithAuthUTR,
        encryption.crypto.encrypt(userAnswers.data.toString(), userAnswers.id),
        encryption.crypto.encrypt(Json.toJson(userAnswers.address).toString(), userAnswers.id),
        userAnswers.packagingSiteList.map(site => site._1 -> encryption.crypto.encrypt(Json.toJson(site._2).toString(), userAnswers.id)),
        userAnswers.warehouseList.map(warehouse => warehouse._1 -> encryption.crypto.encrypt(Json.toJson(warehouse._2).toString(), userAnswers.id)),
       userAnswers.alfResponseForLookupState.map(alfRespWithState => encryption.crypto.encrypt(Json.toJson(alfRespWithState).toString(), userAnswers.id)),
       userAnswers.submittedOn, userAnswers.lastUpdated
      )
      result mustBe userAnswers

    }
  }

  "encryptDatedCacheMap" - {
    "should encrypt correctly" in {
      val datedCacheMap: DatedCacheMap = DatedCacheMap(
        "foo",
        Map("string" -> Json.obj("foo" -> "bar")),
        Instant.now()
      )

      val result = ModelEncryption.encryptDatedCacheMap(datedCacheMap)
      result._1 mustBe datedCacheMap.id
      result._2.head._1 mustBe datedCacheMap.data.head._1
      Json.parse(encryption.crypto.decrypt(result._2.head._2, datedCacheMap.id)) mustBe datedCacheMap.data.head._2
      result._3 mustBe result._3
    }
  }

  "decryptDatedCacheMap" - {
    "should decrypt correctly" in {
      val datedCacheMap: DatedCacheMap = DatedCacheMap(
        "foo",
        Map("string" -> Json.obj("foo" -> "bar")),
        Instant.now()
      )

      val result = ModelEncryption.decryptDatedCacheMap(
        datedCacheMap.id,
        datedCacheMap.data.map(item => item._1 -> encryption.crypto.encrypt(item._2.toString(), datedCacheMap.id)),
        datedCacheMap.lastUpdated
      )
      result mustBe datedCacheMap
    }
  }
}
