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
import models.backend.{Site, UkAddress}
import play.api.libs.json.{JsObject, Json, Reads}
import repositories.DatedCacheMap
import services.Encryption

import java.time.{Instant, LocalDate}

class ModelEncryptionSpec extends SpecBase {

  implicit val encryption: Encryption = application.injector.instanceOf[Encryption]

  "encryptUserAnswers" - {
    "should encrypt userAnswers" in {
      val alfId: String = "bar"
      val userAnswers = UserAnswers("id",
        Json.obj("foo" -> "bar"),
        Some(UkAddress(List("Line 1", "Line 2", "Line 3", "Line 4"),"aa1 1aa", alfId = Some(alfId))),
        List(SmallProducer("foo", "bar", (1,1))),
        Map("foo" -> Site(UkAddress(List("foo"),"foo", Some("foo")),Some("foo"), Some("foo"),Some(LocalDate.now()))),
        Map("foo" -> Warehouse(Some("foo"),UkAddress(List("foo"),"foo", Some("foo")))),
        false,
        Instant.ofEpochSecond(1))

      val result = ModelEncryption.encryptUserAnswers(userAnswers)
      result._1 mustBe userAnswers.id
      Json.parse(encryption.crypto.decrypt(result._2, userAnswers.id)).as[JsObject] mustBe userAnswers.data
      Json.fromJson[Option[UkAddress]](Json.parse(encryption.crypto.decrypt(result._3, userAnswers.id)))(Reads.optionWithNull[UkAddress]).get mustBe userAnswers.address
      Json.parse(encryption.crypto.decrypt(result._4, userAnswers.id)).as[List[SmallProducer]] mustBe userAnswers.smallProducerList
      Json.parse(encryption.crypto.decrypt(result._5.head._2, userAnswers.id)).as[Site] mustBe userAnswers.packagingSiteList.head._2
      result._5.head._1 mustBe userAnswers.packagingSiteList.head._1
      Json.parse(encryption.crypto.decrypt(result._6.head._2, userAnswers.id)).as[Warehouse] mustBe userAnswers.warehouseList.head._2
      result._6.head._1 mustBe userAnswers.warehouseList.head._1
      result._7 mustBe userAnswers.submitted
      result._8 mustBe userAnswers.lastUpdated
    }
  }
  "decryptUserAnswers" - {
    "should decrypt userAnswers in tuple form" in {
      val alfId: String = "bar"
      val userAnswers = UserAnswers("id",
        Json.obj("foo" -> "bar"),
        Some(UkAddress(List("Line 1", "Line 2", "Line 3", "Line 4"),"aa1 1aa", alfId = Some(alfId))),
        List(SmallProducer("foo", "bar", (1,1))),
        Map("foo" -> Site(UkAddress(List("foo"),"foo", Some("foo")),Some("foo"), Some("foo"),Some(LocalDate.now()))),
        Map("foo" -> Warehouse(Some("foo"),UkAddress(List("foo"),"foo", Some("foo")))),
        false,
        Instant.ofEpochSecond(1))

     val result = ModelEncryption.decryptUserAnswers(
        userAnswers.id,
        encryption.crypto.encrypt(userAnswers.data.toString(), userAnswers.id),
        encryption.crypto.encrypt(Json.toJson(userAnswers.address).toString(), userAnswers.id),
        encryption.crypto.encrypt(Json.toJson(userAnswers.smallProducerList).toString(), userAnswers.id),
        userAnswers.packagingSiteList.map(site => site._1 -> encryption.crypto.encrypt(Json.toJson(site._2).toString(), userAnswers.id)),
        userAnswers.warehouseList.map(warehouse => warehouse._1 -> encryption.crypto.encrypt(Json.toJson(warehouse._2).toString(), userAnswers.id)),
        userAnswers.submitted, userAnswers.lastUpdated
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
