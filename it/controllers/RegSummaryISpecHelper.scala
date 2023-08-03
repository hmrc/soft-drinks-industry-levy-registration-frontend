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

import models.HowManyLitresGlobally.{Large, Small}
import models.OrganisationType.LimitedCompany
import models.Verify.YesRegister
import models.backend.{Site, UkAddress}
import models.{CheckMode, ContactDetails, LitresInBands, Verify}
import org.jsoup.nodes.{Document, Element}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages._

import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter


trait RegSummaryISpecHelper extends ControllerITTestHelper {

  object Selectors {
    val heading = "govuk-heading-l"
    val body = "govuk-body"
    val govukFormGroup = "govuk-form-group"
    val label = "govuk-label"
    val button = "govuk-button"
    val form = "form"
  }

  val rosmAddress = UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL")
  val newAddress = UkAddress(List("10 Linden Close", "Langly"), "LA16 3KL")

  val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  val submittedDate = LocalDateTime.of(2023, 7, 10, 14, 30).toInstant(ZoneOffset.UTC)


  val operatePackagingSiteLitres = LitresInBands(1000, 2000)
  val contractPackingLitres = LitresInBands(3000, 4000)
  val importsLitres = LitresInBands(5000, 6000)
  val startDate = LocalDate.of(2022, 6, 1)

  val userAnswersWithLitres = emptyUserAnswers
    .copy(packagingSiteList = packagingSiteListWith3, warehouseList = warehouseListWith1)
    .set(VerifyPage, Verify.No).success.value
    .set(OrganisationTypePage, LimitedCompany).success.value
    .set(HowManyLitresGloballyPage, Large).success.value
    .set(OperatePackagingSitesPage, true).success.value
    .set(HowManyOperatePackagingSitesPage, operatePackagingSiteLitres).success.value
    .set(ContractPackingPage, true).success.value
    .set(HowManyContractPackingPage, contractPackingLitres).success.value
    .set(ImportsPage, true).success.value
    .set(HowManyImportsPage, importsLitres).success.value
    .set(StartDatePage, startDate).success.value
    .set(PackAtBusinessAddressPage, true).success.value
    .set(PackagingSiteDetailsPage, true).success.value
    .set(AskSecondaryWarehousesPage, true).success.value
    .set(WarehouseDetailsPage, true).success.value
    .set(ContactDetailsPage, contactDetails).success.value

  val userAnswersWithAllNo = emptyUserAnswers
    .copy(address = Some(newAddress), packagingSiteList = packagingSiteListWith3, warehouseList = warehouseListWith1)
    .set(VerifyPage, YesRegister).success.value
    .set(OrganisationTypePage, LimitedCompany).success.value
    .set(HowManyLitresGloballyPage, Small).success.value
    .set(ThirdPartyPackagersPage, true).success.value
    .set(OperatePackagingSitesPage, false).success.value
    .set(ContractPackingPage, false).success.value
    .set(ImportsPage, false).success.value
    .set(StartDatePage, startDate).success.value
    .set(PackAtBusinessAddressPage, true).success.value
    .set(PackagingSiteDetailsPage, true).success.value
    .set(AskSecondaryWarehousesPage, true).success.value
    .set(WarehouseDetailsPage, true).success.value
    .set(ContactDetailsPage, contactDetails).success.value

  def validatePanel(panel: Element) = {
    panel.getElementsByClass("govuk-panel__title").text() mustEqual "Application complete"
    panel.getElementsByClass("govuk-panel__body").text() mustEqual s"We have received your application to register Super Lemonade Plc for the Soft Drinks Industry Levy"
  }

  def validateSummaryContent(document: Document) = {
    val printPageElements = document.getElementById("printPage")
    val link = printPageElements.getElementsByClass("govuk-link")
    link.text() mustEqual "Print this page"
    link.attr("href") mustEqual "javascript:window.print()"

    val applicationSentAt = document.getElementById("applicationSentAt")
    applicationSentAt.text() mustBe "Your application to register for the Soft Drinks Industry Levy was sent on 10 July 2023 at 2:30pm."

    val applicationSentEmailed = document.getElementById("applicationSentEmailed")
    applicationSentEmailed.text() mustBe s"We have sent a registration confirmation email to ${contactDetails.email}."

    document.getElementById("whatNextHeader").text() mustEqual "What happens next"
    document.getElementById("whatNextTextP1").text() mustEqual "You do not need to do anything at this time."
    document.getElementById("whatNextTextP2").text() mustEqual s"We will send your Soft Drinks Industry Levy reference number to ${contactDetails.email} within 24 hours."

    document.getElementById("needHelp").text() mustEqual "Help using this service"
    document.getElementById("needHelpP1").text() mustEqual "Call the Soft Drinks Industry Helpline on 0300 200 1000 if you:"
    val listItems = document.getElementById("helpList").getElementsByTag("li")
    listItems.size() mustBe 2
    listItems.get(0).text() mustBe "do not receive your reference number"
    listItems.get(1).text() mustBe "need to make a change to your application"
  }

  def validateBusinessDetailsSummaryList(summaryList: Element,
                                         utr: String,
                                         address: UkAddress,
                                         numberOfLitresGloballyValue: String,
                                         isCheckAnswers: Boolean = true) = {
    val summaryRows = summaryList.getElementsByClass("govuk-summary-list__row")
    summaryRows.size mustBe 4

    val utrRow = summaryRows.first()
    utrRow.getElementsByClass("govuk-summary-list__key").text() mustBe "Unique Taxpayer Reference (UTR)"
    utrRow.getElementsByClass("govuk-summary-list__value").text() mustBe utr

    val nameRow = summaryRows.get(1)
    nameRow.getElementsByClass("govuk-summary-list__key").text() mustBe "Business name"
    nameRow.getElementsByClass("govuk-summary-list__value").text() mustBe "Super Lemonade Plc"

    val addressRow = summaryRows.get(2)
    addressRow.getElementsByClass("govuk-summary-list__key").text() mustBe "Business address"
    addressRow.getElementsByClass("govuk-summary-list__value").text() mustBe s"${address.lines.mkString(" ")} ${address.postCode}"
    if (isCheckAnswers) {
      val addressAction = addressRow.getElementsByClass("govuk-summary-list__actions").first()
      addressAction.text() mustBe "Change business address"
      addressAction.getElementById("change-businessAddress").attr("href") mustBe "/soft-drinks-industry-levy-registration/change-verify"
    } else {
      addressRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
    val globalLitresRow = summaryRows.get(3)
    globalLitresRow.getElementsByClass("govuk-summary-list__key").text() mustBe "Litres of your own brands of liable drinks packaged globally in the past 12 months"
    globalLitresRow.getElementsByClass("govuk-summary-list__value").text() mustBe numberOfLitresGloballyValue
    if (isCheckAnswers) {
      val globalLitresAction = globalLitresRow.getElementsByClass("govuk-summary-list__actions").first()
      globalLitresAction.text() mustBe "Change litres of your own brands of liable drinks packaged globally in the past 12 months"
      globalLitresAction.getElementById("change-howManyLitresGlobally").attr("href") mustBe "/soft-drinks-industry-levy-registration/change-how-many-litres-globally"
    } else {
      globalLitresRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
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

  def validateOperatePackagingSitesWithNoLitresSummaryList(operatePackagingSites: Element,
                                                         isCheckYourAnswers: Boolean) = {
    val rows = operatePackagingSites.getElementsByClass("govuk-summary-list__row")
    rows.size() mustBe 1
    val yesNoRow = rows.get(0)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
    if (isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change do you operate any packaging sites in the UK to package liable drinks for the brands you own?"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "do you operate any packaging sites in the UK to package liable drinks for the brands you own?"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.OperatePackagingSitesController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
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

  def validateContractPackingWithNoLitresSummaryList(operatePackagingSites: Element,
                                                   isCheckYourAnswers: Boolean) = {
    val rows = operatePackagingSites.getElementsByClass("govuk-summary-list__row")
    rows.size() mustBe 1
    val yesNoRow = rows.get(0)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
    if (isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change do you operate any packaging sites in the UK to package liable drinks as a third party or contract packer?"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "do you operate any packaging sites in the UK to package liable drinks as a third party or contract packer?"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.ContractPackingController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
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

  def validateImportsWithNoLitresSummaryList(imports: Element,
                                           isCheckYourAnswers: Boolean) = {
    val rows = imports.getElementsByClass("govuk-summary-list__row")
    rows.size() mustBe 1
    val yesNoRow = rows.get(0)
    yesNoRow.getElementsByClass("govuk-summary-list__value").first().text() mustBe "No"
    if (isCheckYourAnswers) {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change do you bring liable drinks into the UK from anywhere outside of the UK?"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "do you bring liable drinks into the UK from anywhere outside of the UK?"
      yesNoRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.ImportsController.onPageLoad(CheckMode).url
    } else {
      yesNoRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateStartDateSummaryList(summaryList: Element,
                                   date: LocalDate,
                                   isCheckAnswers: Boolean = true) = {
    val summaryRows = summaryList.getElementsByClass("govuk-summary-list__row")
    summaryRows.size mustBe 1

    val startDateRow = summaryRows.first()
    startDateRow.getElementsByClass("govuk-summary-list__key").text() mustBe "Date liable from"
    startDateRow.getElementsByClass("govuk-summary-list__value").text() mustBe date.format(dateFormatter)
    if (isCheckAnswers) {
      val fullNameAction = startDateRow.getElementsByClass("govuk-summary-list__actions").first()
      fullNameAction.text() mustBe "Change liability date"
      fullNameAction.getElementById("change-startDate").attr("href") mustBe routes.StartDateController.onPageLoad(CheckMode).url
    } else {
      startDateRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }


  def validatePackingSiteDetailsSummary(summaryList: Element,
                                        isCheckAnswers: Boolean = true) = {
    val rows = summaryList.getElementsByClass("govuk-summary-list__row")
    rows.size() mustBe 2
    val packingRow = rows.get(0)
    val warehouseRow = rows.get(1)
    if (isCheckAnswers) {
      packingRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change the UK packaging site that you operate to produce liable drinks"
      packingRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "the UK packaging site that you operate to produce liable drinks"
      packingRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url

      warehouseRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change the UK warehouses you use to store liable drinks"
      warehouseRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "the UK warehouses you use to store liable drinks"
      warehouseRow.getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.AskSecondaryWarehousesController.onPageLoad(CheckMode).url
    } else {
      packingRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }

  def validateContactDetailsSummaryList(summaryList: Element,
                                        contactDetails: ContactDetails,
                                        isCheckAnswers: Boolean = true) = {
    val summaryRows = summaryList.getElementsByClass("govuk-summary-list__row")
    summaryRows.size mustBe 4

    val fullNameRow = summaryRows.first()
    fullNameRow.getElementsByClass("govuk-summary-list__key").text() mustBe "Full name"
    fullNameRow.getElementsByClass("govuk-summary-list__value").text() mustBe contactDetails.fullName
    if(isCheckAnswers) {
      val fullNameAction = fullNameRow.getElementsByClass("govuk-summary-list__actions").first()
      fullNameAction.text() mustBe "Change full name"
      fullNameAction.getElementById("change-fullName").attr("href") mustBe "/soft-drinks-industry-levy-registration/change-contact-details"
    } else {
      fullNameRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    val positionRow = summaryRows.get(1)
    positionRow.getElementsByClass("govuk-summary-list__key").text() mustBe "Job title"
    positionRow.getElementsByClass("govuk-summary-list__value").text() mustBe contactDetails.position
    if(isCheckAnswers) {
      val positionAction = positionRow.getElementsByClass("govuk-summary-list__actions").first()
      positionAction.text() mustBe "Change job title"
      positionAction.getElementById("change-position").attr("href") mustBe "/soft-drinks-industry-levy-registration/change-contact-details"
    } else {
      positionRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }

    val phoneRow = summaryRows.get(2)
    phoneRow.getElementsByClass("govuk-summary-list__key").text() mustBe "Telephone number"
    phoneRow.getElementsByClass("govuk-summary-list__value").text() mustBe contactDetails.phoneNumber
    if(isCheckAnswers) {
      val phoneAction = phoneRow.getElementsByClass("govuk-summary-list__actions").first()
      phoneAction.text() mustBe "Change telephone number"
      phoneAction.getElementById("change-phoneNumber").attr("href") mustBe "/soft-drinks-industry-levy-registration/change-contact-details"
    } else {
      phoneRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
    val emailRow = summaryRows.get(3)
    emailRow.getElementsByClass("govuk-summary-list__key").text() mustBe "Email address"
    emailRow.getElementsByClass("govuk-summary-list__value").text() mustBe contactDetails.email
    if(isCheckAnswers) {
      val emailAction = emailRow.getElementsByClass("govuk-summary-list__actions").first()
      emailAction.text() mustBe "Change email"
      emailAction.getElementById("change-email").attr("href") mustBe "/soft-drinks-industry-levy-registration/change-contact-details"
    } else {
      emailRow.getElementsByClass("govuk-summary-list__actions").size() mustBe 0
    }
  }
}
