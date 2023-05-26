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

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryImportsUserAnswersEntry: Arbitrary[(ImportsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ImportsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryOperatePackagingSitesUserAnswersEntry: Arbitrary[(OperatePackagingSitesPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[OperatePackagingSitesPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPackagingSiteDetailsUserAnswersEntry: Arbitrary[(PackagingSiteDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[PackagingSiteDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHowManyLitresGloballyUserAnswersEntry: Arbitrary[(HowManyLitresGloballyPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HowManyLitresGloballyPage.type]
        value <- arbitrary[HowManyLitresGlobally].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAskSecondaryWarehousesUserAnswersEntry: Arbitrary[(AskSecondaryWarehousesPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AskSecondaryWarehousesPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryThirdPartyPackagersUserAnswersEntry: Arbitrary[(ThirdPartyPackagersPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ThirdPartyPackagersPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryStartDateUserAnswersEntry: Arbitrary[(StartDatePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[StartDatePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryOrganisationTypeUserAnswersEntry: Arbitrary[(OrganisationTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[OrganisationTypePage.type]
        value <- arbitrary[OrganisationType].map(Json.toJson(_))
      } yield (page, value)
    }
}
