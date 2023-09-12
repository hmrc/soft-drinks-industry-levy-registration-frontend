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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryWarehousesTradingName: Arbitrary[WarehousesTradingName] =
    Arbitrary {
      for {
        "WarehouseTradingName" <- arbitrary[String]
      } yield WarehousesTradingName("WarehouseTradingName")
    }

  implicit lazy val arbitraryPackagingSiteName: Arbitrary[PackagingSiteName] =
    Arbitrary {
      for {
        "PackagingSiteName" <- arbitrary[String]
      } yield PackagingSiteName("PackagingSiteName")
    }

  implicit lazy val arbitraryVerify: Arbitrary[Verify] =
    Arbitrary {
      Gen.oneOf(Verify.values.toSeq)
    }

  implicit lazy val arbitraryContactDetails: Arbitrary[ContactDetails] =
    Arbitrary {
      for {
        fullName <- arbitrary[String]
        position <- arbitrary[String]
        phoneNumber <- arbitrary[String]
        email <- arbitrary[String]
      } yield ContactDetails(fullName, position, phoneNumber, email)
    }

  implicit lazy val arbitraryIdentification: Arbitrary[Identify] =
    Arbitrary {
      for {
        utr <- arbitrary[String]
        postcode <- arbitrary[String]
      } yield Identify(utr, postcode)
    }

  implicit lazy val arbitraryHowManyLitresGlobally: Arbitrary[HowManyLitresGlobally] =
    Arbitrary {
      Gen.oneOf(HowManyLitresGlobally.values.toSeq)
    }

  implicit lazy val arbitraryOrganisationType: Arbitrary[OrganisationType] =
    Arbitrary {
      Gen.oneOf(OrganisationType.values.toSeq)
    }

  implicit lazy val arbitraryLitresInBands: Arbitrary[LitresInBands] =
    Arbitrary {
      for {
        lowBand <- arbitrary[Long]
        highBand <- arbitrary[Long]
      } yield LitresInBands(lowBand, highBand)
    }
}
