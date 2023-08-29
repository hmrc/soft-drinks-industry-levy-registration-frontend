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
import org.scalatestplus.mockito.MockitoSugar
import pages.{HowManyContractPackingPage, HowManyImportsPage, HowManyLitresGloballyPage, HowManyOperatePackagingSitesPage, ThirdPartyPackagersPage}
import play.api.libs.json.Json


class UserTypeCheckModelSpec extends SpecBase with MockitoSugar with DataHelper {
  val largeProducerUserAnswersNoContractPacking: UserAnswers = UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "large",
    "howManyImports" -> Json.obj("lowBand" -> 1,"highBand" -> 1)))
  val smallProducerUserAnswersNoOperatePackagingSites: UserAnswers = UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "small",
    "howManyContractPacking" -> Json.obj("lowBand" -> 1,"highBand" -> 1), "thirdPartyPackagers" -> true,
    "howManyImports" -> Json.obj("lowBand" -> 1,"highBand" -> 1)))
  val notAProducerUserAnswersNoThirdPartyPackagers: UserAnswers = UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "xnot",
    "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1,"highBand" -> 1), "howManyContractPacking" -> Json.obj("lowBand" -> 1,"highBand" -> 1)))
  val answersNoImports: UserAnswers = UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("howManyLitresGlobally" -> "xnot",
    "howManyOperatePackagingSites" -> Json.obj("lowBand" -> 1,"highBand" -> 1), "thirdPartyPackagers" -> false))

  "UserTypeCheckModel" - {
    s"isLarge returns true if $HowManyLitresGloballyPage was answered with > 1 million" in {
      val data = UserTypeCheck.isLarge(largeProducerUserAnswersNoContractPacking)
      data mustBe true
    }

    s"isLarge returns false if $HowManyLitresGloballyPage was answered with < 1 million" in {
        val data = UserTypeCheck.isLarge(smallProducerUserAnswersNoOperatePackagingSites)
        data mustBe false
    }

    s"isLarge returns false if $HowManyLitresGloballyPage was answered with None" in {
      val data = UserTypeCheck.isLarge(notAProducerUserAnswersNoThirdPartyPackagers)
      data mustBe false
    }

    s"isSmall returns true if $HowManyLitresGloballyPage was answered with < 1 million" in {
      val data = UserTypeCheck.isSmall(smallProducerUserAnswersNoOperatePackagingSites)
      data mustBe true
    }

    s"isSmall returns false if $HowManyLitresGloballyPage was answered with > 1 million" in {
      val data = UserTypeCheck.isSmall(largeProducerUserAnswersNoContractPacking)
      data mustBe false
    }

    s"isSmall returns false if $HowManyLitresGloballyPage was answered with None" in {
      val data = UserTypeCheck.isSmall(notAProducerUserAnswersNoThirdPartyPackagers)
      data mustBe false
    }

    s"notAProducer returns true if $HowManyLitresGloballyPage was answered with None" in {
      val data = UserTypeCheck.notAProducer(notAProducerUserAnswersNoThirdPartyPackagers)
      data mustBe true
    }

    s"notAProducer returns false if $HowManyLitresGloballyPage was answered with > 1 million" in {
      val data = UserTypeCheck.notAProducer(largeProducerUserAnswersNoContractPacking)
      data mustBe false
    }

    s"notAProducer returns false if $HowManyLitresGloballyPage was answered with < 1 million" in {
      val data = UserTypeCheck.notAProducer(smallProducerUserAnswersNoOperatePackagingSites)
      data mustBe false
    }

    s"operatesPackagingSite returns true if $HowManyOperatePackagingSitesPage was answered and litres > 1" in {
      val data = UserTypeCheck.operatesPackagingSite(answersNoImports)
      data mustBe true
      val data1 = UserTypeCheck.operatesPackagingSite(notAProducerUserAnswersNoThirdPartyPackagers)
      data1 mustBe true
    }

    s"operatesPackagingSite returns false if $HowManyOperatePackagingSitesPage was not answered or litres < 1" in {
      val data = UserTypeCheck.operatesPackagingSite(smallProducerUserAnswersNoOperatePackagingSites)
      data mustBe false
      val data1 = UserTypeCheck.operatesPackagingSite(largeProducerUserAnswersNoContractPacking)
      data1 mustBe false

    }

    s"copackerAll returns true if $HowManyContractPackingPage was answered and litres > 1" in {
      val data = UserTypeCheck.copackerAll(smallProducerUserAnswersNoOperatePackagingSites)
      data mustBe true
      val data1 = UserTypeCheck.copackerAll(notAProducerUserAnswersNoThirdPartyPackagers)
      data1 mustBe true
    }

    s"copackerAll returns false if $HowManyContractPackingPage was not answered or litres < 1" in {
      val data = UserTypeCheck.copackerAll(largeProducerUserAnswersNoContractPacking)
      data mustBe false
      val data1 = UserTypeCheck.copackerAll(answersNoImports)
      data1 mustBe false
    }

    s"copackee returns true if $ThirdPartyPackagersPage was answered yes" in {
      val data = UserTypeCheck.copackee(smallProducerUserAnswersNoOperatePackagingSites)
      data mustBe true
    }

    s"copackee returns false if $ThirdPartyPackagersPage was not answered or answered no" in {
      val data = UserTypeCheck.copackee(notAProducerUserAnswersNoThirdPartyPackagers)
      data mustBe false
    }

    s"copackee returns false if $ThirdPartyPackagersPage was answered no" in {
      val data = UserTypeCheck.copackee(answersNoImports)
      data mustBe false
    }

   s"importer returns true if $HowManyImportsPage was answered and litres > 1" in {
      val data = UserTypeCheck.importer(largeProducerUserAnswersNoContractPacking)
      data mustBe true
      val data1 = UserTypeCheck.importer(smallProducerUserAnswersNoOperatePackagingSites)
      data1 mustBe true
    }

    s"importer returns false if $HowManyImportsPage was not answered or litres < 1" in {
      val data = UserTypeCheck.importer(answersNoImports)
      data mustBe false
      val data1 = UserTypeCheck.importer(notAProducerUserAnswersNoThirdPartyPackagers)
      data1 mustBe false
    }
  }
}
