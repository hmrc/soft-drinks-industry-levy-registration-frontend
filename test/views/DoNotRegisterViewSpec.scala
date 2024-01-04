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

import models.NormalMode
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.DoNotRegisterView

class DoNotRegisterViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[DoNotRegisterView]
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-l"
    val body = "govuk-body"
    val li = "li"
  }

  "View" - {
    val html = view()(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include("You do not need to register")
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual "You do not need to register"
    }
    "should have the expected content" in {
      document.getElementsByClass(Selectors.body).get(0).text() mustEqual "Based on your answers you do not need to register. You will need to register within 30 days of the:"

      document.getElementsByTag(Selectors.li).get(0).text() mustEqual "end of the month if you have produced over 1 million litres of liable drinks in the past 12 months"
      document.getElementsByTag(Selectors.li).get(1).text() mustEqual "date you know that you will produce over 1 million litres of liable drinks in the next 30 days"
      document.getElementsByTag(Selectors.li).get(2).text() mustEqual "end of the month in which you packaged liable drinks for someone else"
      document.getElementsByTag(Selectors.li).get(3).text() mustEqual "date you know you are going to package liable drinks for someone else"
      document.getElementsByTag(Selectors.li).get(4).text() mustEqual "end of the month in which you brought liable drinks into the UK"
      document.getElementsByTag(Selectors.li).get(5).text() mustEqual "date you know you are going to bring liable drinks into the UK"

      document.getElementsByClass(Selectors.body).get(1).text() mustEqual "If this is not right, you need to go back and check your answers"
      document.getElementById("goBackCheckAnswers").attr("href") mustBe controllers.routes.HowManyLitresGloballyController.onPageLoad(NormalMode).url
      document.getElementById("goBackCheckAnswers").text() mustEqual "go back and check your answers"
    }

    testBackLink(document)
    validateTimeoutDialog(document)

    validateAccessibilityStatementLinkPresent(document)
  }

}
