/*
 * Copyright 2026 HM Revenue & Customs
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

import models.RegisterState
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.{FakeRequest, WsTestClient}

class AlreadyRegisteredControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/application-already-registered"

  "GET " + normalRoutePath - {
    "should return OK and render the AlreadyRegistered page" in {
      build.authorisedWithBothSDILandUTRInEnrolmentsAndHasROSM

      setAnswers(emptyUserAnswers.copy(registerState = RegisterState.AlreadyRegistered))

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)

          given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
          given messages: Messages       = messagesApi.preferred(FakeRequest())

          page.title must include(messages("alreadyRegistered.heading.title"))
          page.getElementsByClass("govuk-heading-l").text() mustEqual Messages("alreadyRegistered.heading.title")
          page
            .getElementById("subheader")
            .text() mustEqual s"These are the details we hold for Unique Taxpayer Reference (UTR) 0000001611:"
          page.getElementById("utrField").text() mustEqual "0000001611:"
          page
            .getElementById("addressForUTR")
            .text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove Guildford GU14 8NL"
          page
            .getElementById("account-redirect")
            .text() mustBe "To view your registration details, go to your Soft Drinks Industry Levy account."
          page.getElementById("account-link").attr("href") mustBe frontendAppConfig.sdilHomeUrl
        }
      }
    }

    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }
}
