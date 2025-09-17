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

import org.scalatest.funsuite.AnyFunSuite
import play.api.libs.json.{JsSuccess, Json}
import models.backend.{Activity, Site, Subscription, UkAddress}

import java.time.LocalDate

class CreatedSubscriptionAndAmountProducedGloballySpec extends AnyFunSuite {

  test("CreatedSubscriptionAndAmountProducedGlobally should serialize and deserialize correctly") {
    val address = UkAddress(List("line1", "line2"), "postcode")
    val activity = Activity(None, None, None, None, isLarge = false)
    val site = Site(address, Some("ref"), "tradingName", Some(LocalDate.now()))
    val contact = Contact(Some("name"), Some("positionInCompany"), "phoneNumber", "email")
    val subscription = Subscription(
      utr = "exampleUTR",
      orgName = "exampleOrgName",
      orgType = "exampleOrgType",
      address = address,
      activity = activity,
      liabilityDate = LocalDate.now(),
      productionSites = Seq(site),
      warehouseSites = Seq(site),
      contact = contact
    )
    val howManyLitresGlobally = HowManyLitresGlobally.Small
    val createdSubscriptionAndAmountProducedGlobally = CreatedSubscriptionAndAmountProducedGlobally(subscription, howManyLitresGlobally)

    val json = Json.toJson(createdSubscriptionAndAmountProducedGlobally)
    val deserialized = json.validate[CreatedSubscriptionAndAmountProducedGlobally]

    assert(deserialized == JsSuccess(createdSubscriptionAndAmountProducedGlobally))
  }
}
