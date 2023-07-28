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

package models.backend

import models.{Contact, RosmWithUtr, UserAnswers}
import pages.{ContactDetailsPage, OrganisationTypePage, StartDatePage}
import play.api.libs.json.{Format, Json}

import java.time.LocalDate

case class Subscription(
  utr: String,
  orgName: String,
  orgType: String,
  address: UkAddress,
  activity: Activity,
  liabilityDate: LocalDate,
  productionSites: Seq[Site],
  warehouseSites: Seq[Site],
  contact: Contact)

object Subscription {
  implicit val format: Format[Subscription] = Json.format[Subscription]

  def generate(userAnswers: UserAnswers, rosmWithUtr: RosmWithUtr): Subscription = {

    val organisationType = userAnswers.get(OrganisationTypePage).map(_.enumNum).getOrElse(throw new Exception("no organisation type in user answers"))
    val activity = Activity.fromUserAnswers(userAnswers)
    val liabilityDate = userAnswers.get(StartDatePage).getOrElse(throw new Exception("no start date in user answers"))
    val productionSites = userAnswers.packagingSiteList.values.toSeq
    val warehouseSites = userAnswers.warehouseList.foldLeft[Seq[Site]](Seq.empty) {case (list, (_, warehouse)) =>
    list.+:(Site.fromWarehouse(warehouse))}
    val contact = userAnswers.get(ContactDetailsPage).map(Contact.fromContactDetails)
      .getOrElse(throw new Exception("no contact details in user answers"))

    Subscription(
      utr = rosmWithUtr.utr,
      orgName = rosmWithUtr.rosmRegistration.organisationName,
      orgType = organisationType,
      address = userAnswers.address.getOrElse(rosmWithUtr.rosmRegistration.address),
      activity = activity,
      liabilityDate = liabilityDate,
      productionSites = productionSites,
      warehouseSites = warehouseSites,
      contact = contact
    )
  }
}
