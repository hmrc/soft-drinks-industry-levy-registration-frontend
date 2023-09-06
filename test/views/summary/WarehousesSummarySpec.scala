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

package views.summary

import base.SpecBase
import controllers.routes
import models.backend.UkAddress
import models.{CheckMode, NormalMode, Warehouse}
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent

class WarehousesSummarySpec extends SpecBase {

  val addressWith3AddressLines = Warehouse(
    tradingName = None,
    address = UkAddress(List("The house", "The Road", "ugzhkxcajkcjfrqsgkjruzlmsxytwhg vdg"), "NW88 8II")
  )

  val address44Characters = Warehouse(
    tradingName = None,
    address = UkAddress(List("29 Station Rd", "The Railyard", "Cambridge"), "CB1 2FP"))

  val address45Characters = Warehouse(
    tradingName = None,
    address = UkAddress(List("29 Station Pl.", "The Railyard", "Cambridge"), "CB1 2FP"))

  val address47Characters = Warehouse(
    tradingName = None,
    address = UkAddress(List("29 Station Place", "The Railyard", "Cambridge"), "CB1 2FP"))


  val address49Characters = Warehouse(
    tradingName = None,
    address = UkAddress(List("29 Station PlaceDr", "The Railyard", "Cambridge"), "CB1 2FP"))

  val address50Characters = Warehouse(
    tradingName = None,
    address = UkAddress(List("29 Station Place Dr", "The Railyard", "Cambridge"), "CB1 2FP"))


  val WarehouseEvenLongerAddressNoTradeName = Warehouse(
    tradingName = None,
    address = UkAddress(List("29 Station Rd", "This address will auto wrap but not in postcode", "it is 4 lines 103 char", "Cambridge"), "CB1 2FP"))

  val WarehouseEvenLongerAddressWithTradeName = Warehouse(
    tradingName = None,
    address = UkAddress(List("29 Station Rd", "This address will auto wrap but not in postcode", "it is 4 lines 103 char", "Cambridge"), "CB1 2FP"))

  val WarehouseListWith3 = Map(("rieajnldkaljnk13", address45Characters), ("jfkladnlr12", address47Characters), ("jgklaj;ll;e;o", address49Characters))

  List(NormalMode, CheckMode).foreach { mode =>
    s"In ${mode.toString}" - {

      "row2" - {

        "should return an empty list of summaryListRows when no packaging site list is passed in" in {
          val WarehouseDetailsSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(Map.empty, mode)

          WarehouseDetailsSummaryRowList mustBe List()
        }

        "must return a remove action if only 1 packaging site is passed in" in {
          val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(warehouseListWith1, mode)

          warehouseSummaryRowList.mkString must include("Remove")
        }
        "must include Correct elements in list with 2 elements" in {
          val site1 = Warehouse(
            None,
            UkAddress(List("foo2", "bar2"), "wizz2"))
          val site2 = Warehouse(
            None,
            UkAddress(List("foo", "bar"), "wizz"))
          val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(Map("ref1" -> site1, "ref2" -> site2), mode)
          warehouseSummaryRowList.head.key.content.asHtml.toString() mustBe "foo2, bar2, <span class=\"nowrap\" style=\"white-space: nowrap;\">wizz2</span>"
          warehouseSummaryRowList.head.actions.toList.head.items.last.content.asHtml.toString() mustBe "Remove"
          warehouseSummaryRowList.head.actions.toList.head.items.last.href mustBe routes.RemoveWarehouseDetailsController.onPageLoad(mode, "ref1").url

          warehouseSummaryRowList.last.key.content.asHtml.toString() mustBe "foo, bar, <span class=\"nowrap\" style=\"white-space: nowrap;\">wizz</span>"
          warehouseSummaryRowList.last.actions.toList.head.items.last.content.asHtml.toString() mustBe "Remove"
          warehouseSummaryRowList.last.actions.toList.head.items.last.href mustBe routes.RemoveWarehouseDetailsController.onPageLoad(mode, "ref2").url
        }
        "address formatting within Row2" - {

          "should place a break after a trading name if a trading name is used" in {
            val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(Map(("90831480921", addressWith3AddressLines)), mode)
            val expectedAddressContent = HtmlContent("The house, The Road, ugzhkxcajkcjfrqsgkjruzlmsxytwhg vdg, <span class=\"nowrap\" style=\"white-space: nowrap;\">NW88 8II</span>")

            warehouseSummaryRowList.head.key.content mustBe expectedAddressContent
          }

          "should not place a break before the post code if the address line and post code length is 44 characters" in {
            val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(Map(("1208934391", address44Characters)), mode)
            val expectedAddressContent = HtmlContent("29 Station Rd, The Railyard, Cambridge, <span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")

            warehouseSummaryRowList.head.key.content mustBe expectedAddressContent
          }

          "should place a break before the post code if the address line and post code length is between 45 and 49 characters" in {
            val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(WarehouseListWith3, mode)
            val expectedAddressContent45 = HtmlContent("29 Station Pl., The Railyard, Cambridge, <br><span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")
            val expectedAddressContent47 = HtmlContent("29 Station Place, The Railyard, Cambridge, <br><span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")
            val expectedAddressContent49 = HtmlContent("29 Station PlaceDr, The Railyard, Cambridge, <br><span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")

            warehouseSummaryRowList.head.key.content mustBe expectedAddressContent45
            warehouseSummaryRowList.apply(1).key.content mustBe expectedAddressContent47
            warehouseSummaryRowList.last.key.content mustBe expectedAddressContent49
          }

          "should not place a break before the post code if the address line and post code length is 50 characters" in {
            val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(Map(("3489028394r", address50Characters)), mode)
            val expectedAddressContent = HtmlContent("29 Station Place Dr, The Railyard, Cambridge, <span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")

            warehouseSummaryRowList.head.key.content mustBe expectedAddressContent
          }
          "should autowrap and place a break before the post code if the address line and post code length is between 98 & 103 characters" in {
            val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(Map(("45641351", WarehouseEvenLongerAddressNoTradeName)), mode)
            val expectedAddressContent = HtmlContent("29 Station Rd, This address will auto wrap but not in postcode, it is 4 lines 103 char, Cambridge, <br><span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")

            warehouseSummaryRowList.head.key.content mustBe expectedAddressContent
          }

          "should place a break after a trading name AND autowrap and place a break before the post code if the address line " +
            "and post code length is between 98 & 103 characters" in {
            val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(Map(("56458678", WarehouseEvenLongerAddressWithTradeName)), mode)
            val expectedAddressContent = HtmlContent("29 Station Rd, This address will auto wrap but not " +
              "in postcode, it is 4 lines 103 char, Cambridge, <br><span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")

            warehouseSummaryRowList.head.key.content mustBe expectedAddressContent
          }
        }
      }

      "address formatting within Row2" - {

        "should place a break after a trading name if a trading name is used" in {
          val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(Map(("90831480921", addressWith3AddressLines)), mode)
          val expectedAddressContent = HtmlContent("The house, The Road, ugzhkxcajkcjfrqsgkjruzlmsxytwhg vdg, <span class=\"nowrap\" style=\"white-space: nowrap;\">NW88 8II</span>")

          warehouseSummaryRowList.head.key.content mustBe expectedAddressContent
        }

        "should not place a break before the post code if the address line and post code length is 44 characters" in {
          val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(Map(("1208934391", address44Characters)), mode)
          val expectedAddressContent = HtmlContent("29 Station Rd, The Railyard, Cambridge, <span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")

          warehouseSummaryRowList.head.key.content mustBe expectedAddressContent
        }

        "should place a break before the post code if the address line and post code length is between 45 and 49 characters" in {
          val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(WarehouseListWith3, mode)
          val expectedAddressContent45 = HtmlContent("29 Station Pl., The Railyard, Cambridge, <br><span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")
          val expectedAddressContent47 = HtmlContent("29 Station Place, The Railyard, Cambridge, <br><span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")
          val expectedAddressContent49 = HtmlContent("29 Station PlaceDr, The Railyard, Cambridge, <br><span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")

          warehouseSummaryRowList.head.key.content mustBe expectedAddressContent45
          warehouseSummaryRowList.apply(1).key.content mustBe expectedAddressContent47
          warehouseSummaryRowList.last.key.content mustBe expectedAddressContent49
        }

        "should not place a break before the post code if the address line and post code length is 50 characters" in {
          val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(Map(("3489028394r", address50Characters)), mode)
          val expectedAddressContent = HtmlContent("29 Station Place Dr, The Railyard, Cambridge, <span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")

          warehouseSummaryRowList.head.key.content mustBe expectedAddressContent
        }
        "should autowrap and place a break before the post code if the address line and post code length is between 98 & 103 characters" in {
          val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(Map(("45641351", WarehouseEvenLongerAddressNoTradeName)), mode)
          val expectedAddressContent = HtmlContent("29 Station Rd, This address will auto wrap but not in postcode, it is 4 lines 103 char, Cambridge," +
            " <br><span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")

          warehouseSummaryRowList.head.key.content mustBe expectedAddressContent
        }

        "should place a break after a trading name AND autowrap and place a break before the post code if the address line " +
          "and post code length is between 98 & 103 characters" in {
          val warehouseSummaryRowList = WarehouseDetailsSummary.warehouseDetailsRow(Map(("56458678", WarehouseEvenLongerAddressWithTradeName)), mode)
          val expectedAddressContent = HtmlContent("29 Station Rd, This address will auto wrap but not " +
            "in postcode, it is 4 lines 103 char, Cambridge, <br><span class=\"nowrap\" style=\"white-space: nowrap;\">CB1 2FP</span>")

          warehouseSummaryRowList.head.key.content mustBe expectedAddressContent
        }
      }
    }
  }
}
