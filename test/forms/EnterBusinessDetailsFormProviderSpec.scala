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

package forms

import forms.behaviours.{IntFieldBehaviours, StringFieldBehaviours}
import models.Identify
import org.scalatest.matchers.must.Matchers
import play.api.data.{Form, FormError}

class EnterBusinessDetailsFormProviderSpec extends  IntFieldBehaviours with Matchers with StringFieldBehaviours{

  val lengthKey = "enterBusinessDetails.error.length"
  val maxLength = 10

  val form: Form[Identify] = new EnterBusinessDetailsFormProvider().apply()

  ".utr" - {

    val fieldName = "utr"
    val requiredKey = "enterBusinessDetails.required.utr"
    val invalidKey = "enterBusinessDetails.invalid.utr"
    val maxLengthKey = "enterBusinessDetails.invalid.utr.length"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      "0000000437"
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, invalidKey)
    )


  }
}
