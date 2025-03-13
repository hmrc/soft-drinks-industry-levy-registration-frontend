/*
 * Copyright 2025 HM Revenue & Customs
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

import models.backend.UkAddress
import org.scalatest.funsuite.AnyFunSuite
import play.api.libs.json.{JsSuccess, Json, OFormat}

import java.time.LocalDate

class OptRetrievedSubscriptionSpec extends AnyFunSuite {

  test("OptRetrievedSubscription should serialize to JSON correctly when the option is Some") {
    val retrievedSubscription = RetrievedSubscription(
      utr = "1234567890",
      sdilRef = "XASD1234567890",
      orgName = "Test Organization",
      address = UkAddress(List("Line 1", "Line 2"), "Postcode"),
      activity = RetrievedActivity(smallProducer = false, largeProducer = false, contractPacker = false, importer = false, voluntaryRegistration = false),
      liabilityDate = LocalDate.of(2023, 3, 7),
      contact = Contact(Some("name"), Some("positionInCompany"), "phoneNumber", "email"),
      deregDate = Some(LocalDate.of(2024, 3, 7))
    )
    val optRetrievedSubscription = OptRetrievedSubscription(Some(retrievedSubscription))
    val json = Json.toJson(optRetrievedSubscription)

    assert((json \ "optRetrievedSubscription").as[RetrievedSubscription] == retrievedSubscription)
  }

  test("OptRetrievedSubscription should serialize to JSON correctly when the option is None") {
    val optRetrievedSubscription = OptRetrievedSubscription(None)
    val json = Json.toJson(optRetrievedSubscription)

    assert((json \ "optRetrievedSubscription").asOpt[RetrievedSubscription].isEmpty)
  }

  test("OptRetrievedSubscription should deserialize from JSON correctly when the option is None") {
    val json = Json.parse(
      """
        |{
        |  "optRetrievedSubscription": null
        |}
      """.stripMargin)

    val expected = OptRetrievedSubscription(None)
    assert(json.validate[OptRetrievedSubscription] == JsSuccess(expected))
  }
}
