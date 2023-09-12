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

import models.alf.AddressResponseForLookupState
import models.backend.{Site, UkAddress}
import play.api.libs.json._
import repositories.DatedCacheMap
import services.Encryption
import uk.gov.hmrc.crypto.EncryptedValue

import java.time.Instant

object ModelEncryption {
  def encryptDatedCacheMap(datedCacheMap: DatedCacheMap)(implicit encryption: Encryption): (String, Map[String, EncryptedValue], Instant) = {
    (
      datedCacheMap.id,
      datedCacheMap.data.map(item => item._1 -> encryption.crypto.encrypt(item._2.toString(), datedCacheMap.id)),
      datedCacheMap.lastUpdated
    )
  }
  def decryptDatedCacheMap(id: String,
                           data: Map[String, EncryptedValue],
                           lastUpdated: Instant)(implicit encryption: Encryption): DatedCacheMap = {
    DatedCacheMap(
      id = id,
      data = data.map(item => item._1 -> Json.parse(encryption.crypto.decrypt(item._2, id))),
      lastUpdated = lastUpdated
    )
  }

  def encryptUserAnswers(userAnswers: UserAnswers)(implicit encryption: Encryption):
  (String, RegisterState, EncryptedValue, EncryptedValue, Map[String, EncryptedValue], Map[String, EncryptedValue], Option[EncryptedValue], Option[Instant], Instant) = {
    ( userAnswers.id,
      userAnswers.registerState,
      encryption.crypto.encrypt(userAnswers.data.toString(), userAnswers.id),
      encryption.crypto.encrypt(Json.toJson(userAnswers.address).toString(), userAnswers.id),
      userAnswers.packagingSiteList.map(site => site._1 -> encryption.crypto.encrypt(Json.toJson(site._2).toString(), userAnswers.id)),
      userAnswers.warehouseList.map(warehouse => warehouse._1 -> encryption.crypto.encrypt(Json.toJson(warehouse._2).toString(), userAnswers.id)),
      userAnswers.alfResponseForLookupState.map(alfResponseForLookupState => encryption.crypto.encrypt(Json.toJson(alfResponseForLookupState).toString(), userAnswers.id)),
      userAnswers.submittedOn, userAnswers.lastUpdated
    )
  }

  def decryptUserAnswers(id: String,
                         registerState: RegisterState,
                         data: EncryptedValue,
                         address: EncryptedValue,
                         packagingSiteList: Map[String, EncryptedValue],
                         warehouseList: Map[String, EncryptedValue],
                         alfResponseForLookupState: Option[EncryptedValue],
                         submittedOn: Option[Instant],
                         lastUpdated: Instant)(implicit encryption: Encryption): UserAnswers = {
    UserAnswers(
      id = id,
      data = Json.parse(encryption.crypto.decrypt(data, id)).as[JsObject],
      registerState = registerState,
      address = Json.fromJson[Option[UkAddress]](Json.parse(encryption.crypto.decrypt(address, id)))(Reads.optionWithNull[UkAddress]).get,
      packagingSiteList = packagingSiteList.map(site => site._1 -> Json.parse(encryption.crypto.decrypt(site._2, id)).as[Site]),
      warehouseList = warehouseList.map(warehouse => warehouse._1 -> Json.parse(encryption.crypto.decrypt(warehouse._2, id)).as[Warehouse]),
      alfResponseForLookupState = alfResponseForLookupState
        .map(alfRespWithState => Json.parse(encryption.crypto.decrypt(alfRespWithState, id)).as[AddressResponseForLookupState]),
      submittedOn = submittedOn,
      lastUpdated = lastUpdated
    )
  }

}
