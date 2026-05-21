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
import org.scalatest.matchers.must.Matchers._
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.{FakeRequest, WsTestClient}

class DoNotRegisterControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/do-not-register"

  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages       = messagesApi.preferred(FakeRequest())

  "GET " + normalRoutePath - {
    "should return OK and render the DoNotRegister page" in {
      build.commonPrecondition

      setAnswers(emptyUserAnswers)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(messages("doNotRegister" + ".title"))
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, messages("doNotRegister" + ".title"))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testUserWhoIsUnableToRegister(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }
}
