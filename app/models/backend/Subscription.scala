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

import models.{ Contact, HowManyLitresGlobally, RosmWithUtr, UserAnswers }
import pages.{ ContactDetailsPage, HowManyLitresGloballyPage, OrganisationTypePage, StartDatePage }
import play.api.libs.json.{ Format, Json }

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
  contact: Contact) {
  def copacks = activity.Copackee.fold(false)(_.nonEmpty)
  def isVoluntary(producerType: HowManyLitresGlobally) = producerType == HowManyLitresGlobally.Small && copacks && activity.CopackerAll.isEmpty && activity.Imported.isEmpty
}

object Subscription {
  implicit val format: Format[Subscription] = Json.format[Subscription]

  def generate(userAnswers: UserAnswers, rosmWithUtr: RosmWithUtr): Subscription = {
    Subscription(
      utr = rosmWithUtr.utr,
      orgName = rosmWithUtr.rosmRegistration.organisationName,
      orgType = getOrganisationType(userAnswers),
      address = userAnswers.address.getOrElse(rosmWithUtr.rosmRegistration.address),
      activity = getActivity(userAnswers),
      liabilityDate = getLiabilityDate(userAnswers),
      productionSites = getProductionSites(userAnswers),
      warehouseSites = getWarehouses(userAnswers),
      contact = getContact(userAnswers))
  }

  private def getOrganisationType(answers: UserAnswers) = {
    answers.get(OrganisationTypePage).map(_.enumNum).getOrElse(throw new Exception("no organisation type in user answers"))
  }

  private def getActivity(answers: UserAnswers) = {
    Activity.fromUserAnswers(answers)
  }

  private def getLiabilityDate(answers: UserAnswers) = {
    answers.get(StartDatePage).getOrElse(LocalDate.now())
  }

  private def getProductionSites(answers: UserAnswers) = {
    if (requiresPackagingSite(answers))
      answers.packagingSiteList.values.toSeq
    else List.empty[Site]
  }

  private def requiresPackagingSite(answers: UserAnswers) = {
    val activity = getActivity(answers)
    if (largeProducerRequiringPackingSite(activity) ||
      smallProducerRequiringPackingSite(activity, answers) ||
      noneProducerRequiringPackingSite(activity, answers)) true else false
  }

  private def largeProducerRequiringPackingSite(activity: Activity) = {
    if (activity.isLarge && (activity.ProducedOwnBrand.isDefined || activity.CopackerAll.isDefined)) true else false
  }

  private def smallProducerRequiringPackingSite(activity: Activity, answers: UserAnswers) = {
    if (answers.get(HowManyLitresGloballyPage).exists(_ == HowManyLitresGlobally.Small)
      && activity.CopackerAll.isDefined) true else false
  }

  private def noneProducerRequiringPackingSite(activity: Activity, answers: UserAnswers) = {
    if (answers.get(HowManyLitresGloballyPage).exists(_ == HowManyLitresGlobally.None)
      && activity.CopackerAll.isDefined) true else false
  }

  private def getWarehouses(answers: UserAnswers) = {
    if (requiresWarehouses(answers)) {
      answers.warehouseList.foldLeft[Seq[Site]](Seq.empty) {
        case (list, (_, warehouse)) => list.+:(Site.fromWarehouse(warehouse))
      }
    } else List.empty[Site]
  }

  private def requiresWarehouses(answers: UserAnswers) = {
    val activity = getActivity(answers)
    if (activity.isLarge ||
      smallProducerRequiringWarehouse(activity, answers) ||
      noneProducerRequiringWarehouse(activity, answers)) true else false
  }

  private def smallProducerRequiringWarehouse(activity: Activity, answers: UserAnswers) = {
    if (answers.get(HowManyLitresGloballyPage).exists(_ == HowManyLitresGlobally.Small)
      && (activity.CopackerAll.isDefined || activity.Imported.isDefined)) true else false
  }

  private def noneProducerRequiringWarehouse(activity: Activity, answers: UserAnswers) = {
    if (answers.get(HowManyLitresGloballyPage).exists(_ == HowManyLitresGlobally.None)
      && (activity.CopackerAll.isDefined || activity.Imported.isDefined)) true else false
  }

  private def getContact(answers: UserAnswers) = {
    answers.get(ContactDetailsPage).map(Contact.fromContactDetails).getOrElse(throw new Exception("no contact details in user answers"))
  }

}
