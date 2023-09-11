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

package base

import models.Verify.YesRegister
import models.backend.{Activity, Site, Subscription, UkAddress}
import models.{Contact, ContactDetails, HowManyLitresGlobally, Litreage, LitresInBands, OrganisationType, RegisterState, UserAnswers, Warehouse}
import pages._
import play.api.libs.json.Json

import java.time.LocalDate

trait RegistrationSubscriptionHelper extends SpecBase {

  val address = UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL")
  val userAnswerDate: LocalDate = LocalDate.of(2023, 6, 1)
  val PackagingSite1: Site = Site(
    UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
    None,
    "Wild Lemonade Group",
    None)

  lazy val packagingSiteListWith1: Map[String, Site] = Map(("78941132", PackagingSite1))
  val warehouse = Warehouse("ABC Ltd", UkAddress(List("33 Rhes Priordy", "East London"), "WR53 7CX"))
  val warehouses: Map[String, Warehouse] = Map(
    "1" -> warehouse,
  )
  val orgName = "test company"

  val litresInBands = LitresInBands(1000, 2000)
  val litreage = Litreage(1000, 2000)

  val contactDetails = ContactDetails("foo", "bar", "wizz", "bang")
  val contact = Contact(Some("foo"), Some("bar"), "wizz", "bang")
  val contactNoNameOrJob = Contact(None, None, "wizz", "bang")

  val activityNoLitres = Activity(None, None, None, None, false)
  val activityWithLitres = Activity(Some(litreage), Some(litreage), Some(litreage), Some(Litreage(1, 1)), true)

  val subscriptionOnlyRequiredFields = Subscription(
    utr, orgName, "1", address, activityNoLitres, userAnswerDate, Seq.empty, Seq.empty, contactNoNameOrJob
  )

  val subscriptionAllFieldsPresent = Subscription(
    utr, orgName, "1", address, activityWithLitres, userAnswerDate, Seq(PackagingSite1), Seq(Site.fromWarehouse(warehouse)), contact
  )

  val userAnswerTwoWarehouses: UserAnswers = UserAnswers(sdilNumber, RegisterState.RegisterWithAuthUTR, Json.obj(), warehouseList = warehouses)

  def userAnswersWithPagesAcrossAllProducerTypes(orgType: OrganisationType,
                                     litresGlobally: HowManyLitresGlobally,
                                     litresPopulated: Boolean = false): UserAnswers = {
    emptyUserAnswers.copy(
      address = Some(address))
      .set(VerifyPage, YesRegister).success.value
      .set(OrganisationTypePage, orgType).success.value
      .set(HowManyLitresGloballyPage, litresGlobally).success.value
      .set(ContractPackingPage, litresPopulated).success.value
      .set(ImportsPage, litresPopulated).success.value
      .set(StartDatePage, userAnswerDate).success.value
      .set(ContactDetailsPage, contactDetails).success.value
  }

  def userAnswersWithPagesAcrossAllProducerTypesWithLitres(orgType: OrganisationType,
                                                           litresGlobally: HowManyLitresGlobally): UserAnswers = {
    userAnswersWithPagesAcrossAllProducerTypes(orgType, litresGlobally, true)
      .copy(packagingSiteList = packagingSiteListWith1, warehouseList = warehouses)
      .set(HowManyContractPackingPage, litresInBands).success.value
      .set(HowManyImportsPage, litresInBands).success.value
  }

  def userAnswersLargeProducerNotIncludingLitres(orgType: OrganisationType): UserAnswers =
    userAnswersWithPagesAcrossAllProducerTypes(orgType, HowManyLitresGlobally.Large, false)
      .set(OperatePackagingSitesPage, false).success.value

  def userAnswersLargeProducerIncludingLitres(orgType: OrganisationType): UserAnswers =
    userAnswersWithPagesAcrossAllProducerTypesWithLitres(orgType, HowManyLitresGlobally.Large)
      .set(OperatePackagingSitesPage, true).success.value
      .set(HowManyOperatePackagingSitesPage, litresInBands).success.value

  def userAnswersSmallProducerNotIncludingLitres(orgType: OrganisationType): UserAnswers =
    userAnswersWithPagesAcrossAllProducerTypes(orgType, HowManyLitresGlobally.Small, false)
      .set(ThirdPartyPackagersPage, false).success.value
      .set(OperatePackagingSitesPage, false).success.value

  def userAnswersSmallProducerIncludingLitres(orgType: OrganisationType): UserAnswers =
    userAnswersWithPagesAcrossAllProducerTypesWithLitres(orgType, HowManyLitresGlobally.Small)
      .set(ThirdPartyPackagersPage, true).success.value
      .set(OperatePackagingSitesPage, true).success.value
      .set(HowManyOperatePackagingSitesPage, litresInBands).success.value

  def userAnswersNoneProducerNotIncludingLitres(orgType: OrganisationType): UserAnswers =
    userAnswersWithPagesAcrossAllProducerTypes(orgType, HowManyLitresGlobally.None, false)

  def userAnswersNoneProducerIncludingLitres(orgType: OrganisationType): UserAnswers =
    userAnswersWithPagesAcrossAllProducerTypesWithLitres(orgType, HowManyLitresGlobally.None)

  def getCompletedUserAnswers(orgType: OrganisationType,
                              litresGlobally: HowManyLitresGlobally,
                              litresPopulated: Boolean): UserAnswers = litresGlobally match {
    case HowManyLitresGlobally.Large if litresPopulated => userAnswersLargeProducerIncludingLitres(orgType)
    case HowManyLitresGlobally.Large => userAnswersLargeProducerNotIncludingLitres(orgType)
    case HowManyLitresGlobally.Small if litresPopulated => userAnswersSmallProducerIncludingLitres(orgType)
    case HowManyLitresGlobally.Small => userAnswersSmallProducerNotIncludingLitres(orgType)
    case _ if litresPopulated => userAnswersNoneProducerIncludingLitres(orgType)
    case _ => userAnswersNoneProducerNotIncludingLitres(orgType)
  }

  def activityWithLitres(litresGlobally: HowManyLitresGlobally): Activity = {
    val producedOwnBrand = if(litresGlobally != HowManyLitresGlobally.None) {
      Some(litreage)
    } else {
      None
    }
    Activity(
      producedOwnBrand,
      Some(litreage),
      Some(litreage),
      if(litresGlobally == HowManyLitresGlobally.Small) {Some(Litreage(1, 1))} else {
        None
      },
      litresGlobally == HowManyLitresGlobally.Large
    )
  }

  def activityWithNoLitres(litresGlobally: HowManyLitresGlobally): Activity = {
    Activity(
      None,
      None,
      None,
      None,
      litresGlobally == HowManyLitresGlobally.Large
    )
  }

  val voluntaryActivity: Activity = {
    Activity(
      None,
      None,
      None,
      Some(Litreage(1, 1)),
      false
    )
  }

  def generateSubscription(orgType: OrganisationType = OrganisationType.LimitedCompany,
                           litresGlobally: HowManyLitresGlobally = HowManyLitresGlobally.Large,
                           allFieldsPopulated: Boolean): Subscription = {
    Subscription(
      utr,
      rosmRegistration.rosmRegistration.organisationName,
      orgType.enumNum,
      address,
      if(allFieldsPopulated) {activityWithLitres(litresGlobally)} else {activityWithNoLitres(litresGlobally)},
      userAnswerDate,
      if(allFieldsPopulated) {Seq(PackagingSite1)} else {Seq.empty},
      if(allFieldsPopulated) {Seq(Site.fromWarehouse(warehouse))} else {Seq.empty},
      contact
    )

  }

  val voluntarySubscription = Subscription(
    utr,
    rosmRegistration.rosmRegistration.organisationName,
    "1",
    address,
    voluntaryActivity,
    userAnswerDate,
    Seq.empty,
    Seq.empty,
    contact
  )
}
