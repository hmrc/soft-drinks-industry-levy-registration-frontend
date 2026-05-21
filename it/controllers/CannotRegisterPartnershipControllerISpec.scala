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

import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import pages.StartDatePage
import play.api.i18n.{Messages, MessagesApi, MessagesProvider}
import play.api.test.{FakeRequest, WsTestClient}

class CannotRegisterPartnershipControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/cannot-register-partnership"

  "GET " + normalRoutePath - {
    "should return OK and render the CannotRegisterPartnership page" in {
      build.commonPrecondition

      val userAnswers = emptyUserAnswers.set(StartDatePage, date).success.value
      setAnswers(userAnswers)

      given messagesApi: MessagesApi           = app.injector.instanceOf[MessagesApi]
      given request: FakeRequest[?]            = FakeRequest()
      given messagesProvider: MessagesProvider = messagesApi.preferred(request)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(Messages("cannotRegisterPartnership" + ".title"))
          page.getElementsByClass("govuk-body").text() mustBe Messages(
            "cannotRegisterPartnership.subText",
            "0300 200 1000"
          )
        }
      }
    }
    testOtherSuccessUserTypes(
      baseUrl + normalRoutePath,
      Messages("cannotRegisterPartnership" + ".title")(using messagesProvider)
    )
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)

  }
}
