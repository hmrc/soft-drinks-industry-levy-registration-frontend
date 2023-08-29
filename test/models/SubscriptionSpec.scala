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

import base.RegistrationSubscriptionHelper
import models.backend.Subscription
import pages.{ContactDetailsPage, OrganisationTypePage}
import play.api.libs.json.{JsBoolean, JsNumber, JsString, Json}

class SubscriptionSpec extends RegistrationSubscriptionHelper {

  def litreageJson(litres: Litreage) = Json.obj(("lower", JsNumber(litres.lower)),("upper", JsNumber(litres.upper)))

  val addressJson = Json.obj(
    ("lines", Json.arr(
      (JsString("105B Godfrey Marchant Grove")),
      (JsString("Guildford")))
    ),
    ("postCode", JsString("GU14 8NL")))

  val packagingSiteJson = Json.obj(
    ("address", Json.obj(
      ("lines", Json.arr(
        (JsString("33 Rhes Priordy")),
        (JsString("East London")))
      ),
      ("postCode", JsString("E73 2RP")))),
    ("tradingName", JsString("Wild Lemonade Group"))
  )

  val warehouseJson = Json.obj(
    ("address", Json.obj(
      ("lines", Json.arr(
        (JsString("33 Rhes Priordy")),
        (JsString("East London")))
      ),
      ("postCode", JsString("WR53 7CX")))),
    ("tradingName", JsString("ABC Ltd"))
  )

  val activityNoLitresJson = Json.obj(("isLarge", JsBoolean(false)))
  val activityWithLitresJson = Json.obj(
    ("ProducedOwnBrand", litreageJson(litreage)),
    ("Imported", litreageJson(litreage)),
    ("CopackerAll", litreageJson(litreage)),
    ("Copackee", litreageJson(Litreage(1, 1))),
    ("isLarge", JsBoolean(true))
  )

  val contactJson = Json.obj(("name", JsString("foo")), ("positionInCompany", JsString("bar")), ("phoneNumber", JsString("wizz")), ("email", JsString("bang")))
  val contactNoNameJobJson = Json.obj(("phoneNumber", JsString("wizz")), ("email", JsString("bang")))

  val subscriptionOnlyRequiredFieldsJson = Json.obj(
    ("utr", JsString(utr)),
    ("orgName", JsString(orgName)),
    ("orgType", JsString("1")),
    ("address", addressJson),
    ("activity", activityNoLitresJson),
    ("liabilityDate", JsString("2023-06-01")),
    ("productionSites", Json.arr()),
    ("warehouseSites", Json.arr()),
    ("contact", contactNoNameJobJson)
  )

  val subscriptionAllFieldsJson = Json.obj(
    ("utr", JsString(utr)),
    ("orgName", JsString(orgName)),
    ("orgType", JsString("1")),
    ("address", addressJson),
    ("activity", activityWithLitresJson),
    ("liabilityDate", JsString("2023-06-01")),
    ("productionSites", Json.arr(packagingSiteJson)),
    ("warehouseSites", Json.arr(warehouseJson)),
    ("contact", contactJson)
  )

  "model should read correctly from JSON" - {
    "when all fields present" in {
      val subscriptionFromJson = subscriptionAllFieldsJson.as[Subscription]

      subscriptionFromJson mustBe subscriptionAllFieldsPresent
    }

    "when all required fields present" in {
      val subscriptionFromJson = subscriptionOnlyRequiredFieldsJson.as[Subscription]

      subscriptionFromJson mustBe subscriptionOnlyRequiredFields
    }
  }
  "model should write to JSON correctly" - {
    "when all fields present" in {
      val subscriptionFromModel = Json.toJson(subscriptionAllFieldsPresent)

      subscriptionFromModel mustBe subscriptionAllFieldsJson
    }

    "when all required fields present" in {
      val subscriptionFromModel = Json.toJson(subscriptionOnlyRequiredFields)

      subscriptionFromModel mustBe subscriptionOnlyRequiredFieldsJson
    }
  }

  "Subscription.generate" - {
    "should generate the expected subscription model" - {
      OrganisationType.valuesWithST.filterNot(_ == OrganisationType.Partnership).foreach { orgType =>
        HowManyLitresGlobally.values.foreach { litresGlobally =>
          "when the user answers contains the required pages" - {
            s"for a ${orgType.toString} that is a ${litresGlobally.toString} producer" - {
              "that has all litres pages populated, warehouses and packagaing site" in {
                val userAnswers = getCompletedUserAnswers(orgType, litresGlobally, true)
                val expectedSubscription = generateSubscription(orgType, litresGlobally, true)

                Subscription.generate(userAnswers, rosmRegistration) mustBe expectedSubscription
              }
              "that has no litres pages populated" in {
                val userAnswers = getCompletedUserAnswers(orgType, litresGlobally, false)
                val expectedSubscription = generateSubscription(orgType, litresGlobally, false)

                Subscription.generate(userAnswers, rosmRegistration) mustBe expectedSubscription

              }
            }
          }
        }
      }
    }

    "should throw an expection" - {
      "when the user answers doesn't include organisation type" in {
        val userAnswers = getCompletedUserAnswers(OrganisationType.LimitedCompany, HowManyLitresGlobally.Large, false)
          .remove(OrganisationTypePage).success.value

        intercept[Exception](
          Subscription.generate(userAnswers, rosmRegistration)
        )
      }

      "when the user answers doesn't include contact details" in {
        val userAnswers = getCompletedUserAnswers(OrganisationType.LimitedCompany, HowManyLitresGlobally.Large, false)
          .remove(ContactDetailsPage).success.value

        intercept[Exception](
          Subscription.generate(userAnswers, rosmRegistration)
        )
      }
    }
  }

}
