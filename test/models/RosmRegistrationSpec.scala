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
import models.backend.UkAddress
import play.api.libs.json.Json

class RosmRegistrationSpec extends SpecBase {

  "model should read correctly from JSON" in {
    Json.fromJson[RosmRegistration](Json.parse(
      """{"safeId":"foo","organisation":{"organisationName":"wizzbang"},"individual":{"firstName":"bar","lastName":"wizz"},"address":{"addressLine1":"1","addressLine2":"2","addressLine3":"3","addressLine4":"4","postalCode":"posty"}}""")).get mustBe RosmRegistration(
      "foo",
      Some(OrganisationDetails("wizzbang")),
      Some(IndividualDetails("bar", "wizz")),
      UkAddress(List("1", "2", "3", "4"), "posty", None))
  }
  "model should write to JSON correctly" in {
    Json.toJson(RosmRegistration(
      "foo",
      Some(OrganisationDetails("wizzbang")),
      Some(IndividualDetails("bar", "wizz")),
      UkAddress(List("1", "2", "3", "4"), "posty", Some("alf")))) mustBe Json.parse(
      """{"safeId":"foo","organisation":{"organisationName":"wizzbang"},"individual":{"firstName":"bar","lastName":"wizz"},"address":{"addressLine1":"1","addressLine2":"2","addressLine3":"3","addressLine4":"4","postalCode":"posty"}}""")
  }

  "convertToUsableUkAddress" - {
    "return a UK address with organisation name added to lines" in {
      val rosmRegistration = RosmRegistration(
        "foo",
        Some(OrganisationDetails("wizzbang")),
        Some(IndividualDetails("bar", "wizz")),
        UkAddress(List("1", "2", "3", "4"), "posty", Some("alf")))
      RosmRegistration.convertToUsableUkAddress(rosmRegistration) mustBe UkAddress(List("wizzbang", "1", "2", "3", "4"), "posty", Some("alf"))
    }
    "return a UK address with individual details added to lines" in {
      val rosmRegistration = RosmRegistration(
        "foo",
        None,
        Some(IndividualDetails("bar", "wizz")),
        UkAddress(List("1", "2", "3", "4"), "posty", Some("alf")))
      RosmRegistration.convertToUsableUkAddress(rosmRegistration) mustBe UkAddress(List("bar wizz", "1", "2", "3", "4"), "posty", Some("alf"))
    }
    "return a UK address with nothing additional added to lines" in {
      val rosmRegistration = RosmRegistration(
        "foo",
        None,
        None,
        UkAddress(List("1", "2", "3", "4"), "posty", Some("alf")))
      RosmRegistration.convertToUsableUkAddress(rosmRegistration) mustBe UkAddress(List("1", "2", "3", "4"), "posty", Some("alf"))
    }
  }

}
