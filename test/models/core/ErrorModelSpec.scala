/*
 * Copyright 2025 HM Revenue & Customs
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

package models.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsSuccess, Json}

class ErrorModelSpec extends AnyFunSuite with Matchers {

  test("ErrorModel should serialize to JSON correctly") {
    val errorModel = ErrorModel(404, "Not Found")
    val json = Json.toJson(errorModel)
    val expectedJson = Json.parse(
      """
        |{
        |  "status": 404,
        |  "message": "Not Found"
        |}
      """.stripMargin)

    json mustBe expectedJson
  }

  test("ErrorModel should deserialize from JSON correctly") {
    val json = Json.parse(
      """
        |{
        |  "status": 404,
        |  "message": "Not Found"
        |}
      """.stripMargin)

    val expected = ErrorModel(404, "Not Found")
    json.validate[ErrorModel] mustBe JsSuccess(expected)
  }
}
