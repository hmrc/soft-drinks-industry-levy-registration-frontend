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

package views

import config.FrontendAppConfig
import controllers.routes
import models.{CheckMode, NormalMode}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.HowManyOperatePackagingSitesView

class HowManyOperatePackagingSitesViewSpec extends LitresSpecHelper {

  val howManyOperatePackagingSitesView: HowManyOperatePackagingSitesView = application.injector.instanceOf[HowManyOperatePackagingSitesView]

  implicit val request: Request[_] = FakeRequest()
  implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  "HowManyOperatePackagingSitesView" - {
    List(NormalMode, CheckMode).foreach { mode =>
      "when in " + mode +" mode" - {
        val html: HtmlFormat.Appendable = howManyOperatePackagingSitesView(form, mode)
        val document = doc(html)
        val htmlWithValidData: HtmlFormat.Appendable = howManyOperatePackagingSitesView(formWithHighAndLowBands, mode)
        val documentWithValidData = doc(htmlWithValidData)
        val htmlFormErrorsEmpty: HtmlFormat.Appendable = howManyOperatePackagingSitesView(emptyForm, mode)
        val documentFormErrorsEmpty = doc(htmlFormErrorsEmpty)
        val htmlFormErrorsNegative: HtmlFormat.Appendable = howManyOperatePackagingSitesView(formWithNegativeNumber, mode)
        val documentFormErrorsNegative = doc(htmlFormErrorsNegative)
        val htmlFormErrorsNoneNumeric: HtmlFormat.Appendable = howManyOperatePackagingSitesView(formWithNoNumeric, mode)
        val documentFormErrorsNoneNumeric = doc(htmlFormErrorsNoneNumeric)
        val htmlFormErrorsNotWhole: HtmlFormat.Appendable = howManyOperatePackagingSitesView(formWithDecimalNumber, mode)
        val documentFormErrorsNotWhole = doc(htmlFormErrorsNotWhole)
        val htmlFormErrorsOutOfRange: HtmlFormat.Appendable = howManyOperatePackagingSitesView(formWithOutOfRangeNumber, mode)
        val documentFormErrorsOutOfRange = doc(htmlFormErrorsOutOfRange)

        "should have the expected title" in {
          document.title() must include(Messages("howManyOperatePackagingSites.title"))
        }

        "should have the expected heading" in {
          document.getElementsByClass(Selectors.heading).text() mustBe Messages("howManyOperatePackagingSites.heading")
        }

        "should include a govuk body with the expected content" in {
          document.getElementsByClass(Selectors.body).text() mustBe Messages("howManyOperatePackagingSites.subtext")
        }

        testLitresInBandsNoPrepopulatedData(document)
        testLitresInBandsWithPrepopulatedData(documentWithValidData)
        testButton(document)
        testAction(document, routes.HowManyOperatePackagingSitesController.onSubmit(mode).url)

        "and the form has errors" - {
          val errorTitle = "Error: " + Messages("howManyOperatePackagingSites.title")
          testEmptyFormErrors(documentFormErrorsEmpty, errorTitle)
          testNoNumericFormErrors(documentFormErrorsNoneNumeric, errorTitle)
          testNegativeFormErrors(documentFormErrorsNegative, errorTitle)
          testDecimalFormErrors(documentFormErrorsNotWhole, errorTitle)
          testOutOfMaxValFormErrors(documentFormErrorsOutOfRange, errorTitle)
        }
      }
    }
  }
}
