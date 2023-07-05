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

package controllers

import models.{CheckMode, ContactDetails, LitresInBands, UserAnswers}
import org.jsoup.nodes.{Document, Element}
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}


trait RegSummaryISpecHelper extends ControllerITTestHelper {

  object Selectors {
    val heading = "govuk-heading-m"
    val body = "govuk-body"
    val govukFormGroup = "govuk-form-group"
    val label = "govuk-label"
    val button = "govuk-button"
    val form = "form"
  }

  def validateOperatePackagingSitesWithLitresSummaryList(operatePackagingSites: Element,
                                                         litresInBands: LitresInBands,
                                                         isCheckYourAnswers: Boolean) = {
    val rows = operatePackagingSites.getElementsByClass("govuk-summary-list__row")
    val yesNoRow = rows.get(0)
    val lowBandRow = rows.get(1)
    val highBandRow = rows.get(2)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"
    if(isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change do you operate any packaging sites in the UK to package liable drinks for the brands you own?"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "do you operate any packaging sites in the UK to package liable drinks for the brands you own?"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.OperatePackagingSitesController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    lowBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.lowBand)
    if (isCheckYourAnswers) {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change amount of litres in low band for own brands packaged at your own site"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "amount of litres in low band for own brands packaged at your own site"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyOperatePackagingSitesController.onPageLoad(CheckMode).url
    } else {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    highBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.highBand)
    if (isCheckYourAnswers) {
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change amount of litres in high band for own brands packaged at your own site"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "amount of litres in high band for own brands packaged at your own site"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyOperatePackagingSitesController.onPageLoad(CheckMode).url
    } else {
      highBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

  }

  def validateContractPackingWithLitresSummaryList(operatePackagingSites: Element,
                                                         litresInBands: LitresInBands,
                                                         isCheckYourAnswers: Boolean) = {
    val rows = operatePackagingSites.getElementsByClass("govuk-summary-list__row")
    val yesNoRow = rows.get(0)
    val lowBandRow = rows.get(1)
    val highBandRow = rows.get(2)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"
    if (isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change do you operate any packaging sites in the UK to package liable drinks as a third party or contract packer?"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "do you operate any packaging sites in the UK to package liable drinks as a third party or contract packer?"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.ContractPackingController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    lowBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.lowBand)
    if (isCheckYourAnswers) {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change amount of litres in low band for contract packed at your own site"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "amount of litres in low band for contract packed at your own site"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyContractPackingController.onPageLoad(CheckMode).url
    } else {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    highBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.highBand)
    if (isCheckYourAnswers) {
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change amount of litres in high band for contract packed at your own site"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "amount of litres in high band for contract packed at your own site"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyContractPackingController.onPageLoad(CheckMode).url
    } else {
      highBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

  }

  def validateImportsWithLitresSummaryList(imports: Element,
                                                         litresInBands: LitresInBands,
                                                         isCheckYourAnswers: Boolean) = {
    val rows = imports.getElementsByClass("govuk-summary-list__row")
    val yesNoRow = rows.get(0)
    val lowBandRow = rows.get(1)
    val highBandRow = rows.get(2)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "Yes"
    if (isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change do you bring liable drinks into the UK from anywhere outside of the UK?"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "do you bring liable drinks into the UK from anywhere outside of the UK?"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.ImportsController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    lowBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.lowBand)
    if (isCheckYourAnswers) {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change amount of litres in low band for brought into the UK"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "amount of litres in low band for brought into the UK"
      lowBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyImportsController.onPageLoad(CheckMode).url
    } else {
      lowBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    highBandRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe java.text.NumberFormat.getInstance.format(litresInBands.highBand)
    if (isCheckYourAnswers) {
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change amount of litres in high band for brought into the UK"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "amount of litres in high band for brought into the UK"
      highBandRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.HowManyImportsController.onPageLoad(CheckMode).url
    } else {
      highBandRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

  }

  def validateContactDetailsSummaryList(summaryList: Element,
                                        contactDetails: ContactDetails,
                                        isCheckAnswers: Boolean = true) = {
    val summaryRows = summaryList.getElementsByClass("govuk-summary-list__row")
    summaryRows.size mustBe 4
    val fullNameRow = summaryRows.first()
    fullNameRow.getElementsByClass("govuk-summary-list__key") mustBe "Full name"
    fullNameRow.getElementsByClass("govuk-summary-list__value") mustBe contactDetails.fullName
    fullNameRow.getElementsByClass("govuk-summary-list__actions") mustBe 1
    if(isCheckAnswers) {
      val fullNameAction = fullNameRow.getElementsByClass("govuk-summary-list__actions").first()
      fullNameAction.text() mustBe "Change"
      fullNameAction.getElementById("change-fullName").attr("href") mustBe "/soft-drinks-industry-levy-registration/change-contact-details"
    } else {
      fullNameRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
    val positionRow = summaryRows.get(1)
    positionRow.getElementsByClass("govuk-summary-list__key") mustBe "Full name"
    positionRow.getElementsByClass("govuk-summary-list__value") mustBe contactDetails.position
    positionRow.getElementsByClass("govuk-summary-list__actions") mustBe 1
    if(isCheckAnswers) {
      val positionAction = positionRow.getElementsByClass("govuk-summary-list__actions").first()
      positionAction.text() mustBe "Change"
      positionAction.getElementById("change-position").attr("href") mustBe "/soft-drinks-industry-levy-registration/change-contact-details"
    } else {
      positionRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
    val phoneRow = summaryRows.get(2)
    phoneRow.getElementsByClass("govuk-summary-list__key") mustBe "Telephone number"
    phoneRow.getElementsByClass("govuk-summary-list__value") mustBe contactDetails.phoneNumber
    phoneRow.getElementsByClass("govuk-summary-list__actions") mustBe 1

    if(isCheckAnswers) {
      val phoneAction = phoneRow.getElementsByClass("govuk-summary-list__actions").first()
      phoneAction.text() mustBe "Change"
      phoneAction.getElementById("change-phoneNumber").attr("href") mustBe "/soft-drinks-industry-levy-registration/change-contact-details"
    } else {
      phoneRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
    val emailRow = summaryRows.get(3)
    emailRow.getElementsByClass("govuk-summary-list__key") mustBe "Email address"
    emailRow.getElementsByClass("govuk-summary-list__value") mustBe contactDetails.email
    emailRow.getElementsByClass("govuk-summary-list__actions") mustBe 1
    if(isCheckAnswers) {
      val emailAction = emailRow.getElementsByClass("govuk-summary-list__actions").first()
      emailAction.text() mustBe "Change"
      emailAction.getElementById("change-email").attr("href") mustBe "/soft-drinks-industry-levy-registration/change-contact-details"
    } else {
      emailRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }
}
