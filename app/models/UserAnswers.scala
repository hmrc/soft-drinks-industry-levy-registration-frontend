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
import models.backend.{ Site, UkAddress }
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import queries.{ Gettable, Settable }
import services.{ AddressLookupState, Encryption }
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.crypto.json.CryptoFormats

import java.time.Instant
import scala.util.{ Failure, Success, Try }

case class UserAnswers(
  id: String,
  registerState: RegisterState,
  data: JsObject = Json.obj(),
  address: Option[UkAddress] = None,
  packagingSiteList: Map[String, Site] = Map.empty,
  warehouseList: Map[String, Warehouse] = Map.empty,
  alfResponseForLookupState: Option[AddressResponseForLookupState] = None,
  submittedOn: Option[Instant] = None,
  lastUpdated: Instant = Instant.now) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(Some(value), updatedAnswers)
    }
  }

  def setAndRemoveLitresIfReq(page: Settable[Boolean], litresPage: Settable[LitresInBands], value: Boolean)(implicit writes: Writes[Boolean]): Try[UserAnswers] = {

    set(page, value).map { updatedAnswers =>
      if (value) {
        updatedAnswers
      } else {
        removeLitres(litresPage, updatedAnswers.data)
      }
    }
  }

  def setAlfResponse(address: UkAddress, addressLookupState: AddressLookupState, sdilId: String): UserAnswers = {
    val addressResponseForLookupState = AddressResponseForLookupState(address, addressLookupState, sdilId)
    copy(alfResponseForLookupState = Some(addressResponseForLookupState))
  }

  def addPackagingSite(packagingSiteAddress: UkAddress, tradingName: String, sdilId: String): UserAnswers = {
    copy(
      alfResponseForLookupState = None,
      packagingSiteList = packagingSiteList.filterNot(_._1 == sdilId) ++ Map(sdilId -> Site(packagingSiteAddress, None, tradingName, None)))
  }

  def addWarehouse(warehouseAddress: UkAddress, tradingName: String, sdilId: String): UserAnswers = {
    copy(
      alfResponseForLookupState = None,
      warehouseList = warehouseList.filterNot(_._1 == sdilId) ++ Map(sdilId -> Warehouse(tradingName, warehouseAddress)))
  }

  def setBusinessAddress(businessAddress: UkAddress): UserAnswers = {
    copy(address = Some(businessAddress))
  }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(None, updatedAnswers)
    }
  }

  def removeLitres(page: Settable[LitresInBands], updatedData: JsObject): UserAnswers = {

    val dataWithNoLitres = updatedData.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        jsValue
      case JsError(_) =>
        updatedData
    }

    val updatedAnswers = copy(data = dataWithNoLitres)
    page.cleanup(None, updatedAnswers).get
  }
}

object UserAnswers {

  object MongoFormats {
    implicit val cryptEncryptedValueFormats: Format[EncryptedValue] = CryptoFormats.encryptedValueFormat
    import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits._

    def reads()(implicit encryption: Encryption): Reads[UserAnswers] = {
      (
        (__ \ "_id").read[String] and
        (__ \ "registerState").read[RegisterState] and
        (__ \ "data").read[EncryptedValue] and
        (__ \ "address").read[EncryptedValue] and
        (__ \ "packagingSiteList").read[Map[String, EncryptedValue]] and
        (__ \ "warehouseList").read[Map[String, EncryptedValue]] and
        (__ \ "alfResponseForLookupState").readNullable[EncryptedValue] and
        (__ \ "submittedOn").readNullable[Instant] and
        (__ \ "lastUpdated").read[Instant])(ModelEncryption.decryptUserAnswers _)
    }

    def writes(implicit encryption: Encryption): OWrites[UserAnswers] = new OWrites[UserAnswers] {
      override def writes(userAnswers: UserAnswers): JsObject = {
        val encryptedValue: (String, RegisterState, EncryptedValue, EncryptedValue, Map[String, EncryptedValue], Map[String, EncryptedValue], Option[EncryptedValue], Option[Instant], Instant) = {
          ModelEncryption.encryptUserAnswers(userAnswers)
        }
        Json.obj(
          "id" -> encryptedValue._1,
          "registerState" -> encryptedValue._2,
          "data" -> encryptedValue._3,
          "address" -> encryptedValue._4,
          "packagingSiteList" -> encryptedValue._5,
          "warehouseList" -> encryptedValue._6,
          "alfResponseForLookupState" -> encryptedValue._7,
          "submittedOn" -> encryptedValue._8,
          "lastUpdated" -> encryptedValue._9)
      }
    }

    def format(implicit encryption: Encryption): OFormat[UserAnswers] = OFormat(reads(), writes)
  }
}