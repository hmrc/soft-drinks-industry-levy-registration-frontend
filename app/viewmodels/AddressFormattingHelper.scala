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

package viewmodels

import models.backend.UkAddress
import play.twirl.api.{ Html, HtmlFormat }
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

object AddressFormattingHelper {

  def formatBusinessAddress(ukAddress: UkAddress, tradingName: Option[String]): HtmlContent = {
    HtmlContent(tradingName.fold("")(tradingName => tradingName + "<br/>") +
      ukAddress.lines.map(line => if (line.isEmpty) { "" } else { HtmlFormat.escape(line).toString() + "<br/>" }).mkString +
      s"""<span class="nowrap" style="white-space: nowrap;">${ukAddress.postCode}</span>""")
  }

  def addressFormatting(address: UkAddress, tradingName: String): Html = {
    val addressFormat = determineAddressFormat(address)
    val commaFormattedSiteAddress = address.lines.map(line => { if (line.isEmpty) "" else line + ", " })
    val htmlSiteAddress = HtmlFormat.escape(commaFormattedSiteAddress.mkString(""))
    val htmlPostcode = Html(s"""<span class="nowrap" style="white-space: nowrap;">${address.postCode}</span>""")
    val htmlTradingName = HtmlFormat.escape(tradingName)
    val breakLine = Html("<br>")

    addressFormat match {
      case AddressWithTradingName => HtmlFormat.fill(Seq(
        htmlTradingName,
        breakLine,
        htmlSiteAddress,
        htmlPostcode))
      case SeparatePostCodeAddressWithTradingName => HtmlFormat.fill(Seq(
        htmlTradingName,
        breakLine,
        htmlSiteAddress,
        breakLine,
        htmlPostcode))
    }
  }

  private def determineAddressFormat(address: UkAddress): AddressMatching = {
    val addressLength = address.lines.toString().length
    if ((addressLength > 44 && addressLength < 50) || (addressLength > 97 && addressLength < 104)) {
      SeparatePostCodeAddressWithTradingName
    } else {
      AddressWithTradingName
    }
  }
}

sealed trait AddressMatching
case object SeparatePostCodeAddressWithTradingName extends AddressMatching
case object AddressWithTradingName extends AddressMatching

