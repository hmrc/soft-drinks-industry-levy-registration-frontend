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

package generators

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryRemovePackagingSiteDetailsPage: Arbitrary[RemovePackagingSiteDetailsPage.type] =
    Arbitrary(RemovePackagingSiteDetailsPage)

  implicit lazy val arbitraryWarehouseDetailsPage: Arbitrary[WarehouseDetailsPage.type] =
    Arbitrary(WarehouseDetailsPage)

  implicit lazy val arbitraryContactDetailsPage: Arbitrary[ContactDetailsPage.type] =
    Arbitrary(ContactDetailsPage)

  implicit lazy val arbitraryPackAtBusinessAddressPage: Arbitrary[PackAtBusinessAddressPage.type] =
    Arbitrary(PackAtBusinessAddressPage)

  implicit lazy val arbitraryContractPackingPage: Arbitrary[ContractPackingPage.type] =
    Arbitrary(ContractPackingPage)

  implicit lazy val arbitraryImportsPage: Arbitrary[ImportsPage.type] =
    Arbitrary(ImportsPage)

  implicit lazy val arbitraryOperatePackagingSitesPage: Arbitrary[OperatePackagingSitesPage.type] =
    Arbitrary(OperatePackagingSitesPage)

  implicit lazy val arbitraryPackagingSiteDetailsPage: Arbitrary[PackagingSiteDetailsPage.type] =
    Arbitrary(PackagingSiteDetailsPage)

  implicit lazy val arbitraryHowManyLitresGloballyPage: Arbitrary[HowManyLitresGloballyPage.type] =
    Arbitrary(HowManyLitresGloballyPage)

  implicit lazy val arbitraryAskSecondaryWarehousesPage: Arbitrary[AskSecondaryWarehousesPage.type] =
    Arbitrary(AskSecondaryWarehousesPage)

  implicit lazy val arbitraryThirdPartyPackagersPage: Arbitrary[ThirdPartyPackagersPage.type] =
    Arbitrary(ThirdPartyPackagersPage)

  implicit lazy val arbitraryStartDatePage: Arbitrary[StartDatePage.type] =
    Arbitrary(StartDatePage)

  implicit lazy val arbitraryOrganisationTypePage: Arbitrary[OrganisationTypePage.type] =
    Arbitrary(OrganisationTypePage)
}
