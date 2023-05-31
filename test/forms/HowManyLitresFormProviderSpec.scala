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

import forms.behaviours.LongFieldBehaviour
import models.LitresInBands
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.{Form, FormError}
import play.api.libs.json.Json

class HowManyLitresFormProviderSpec extends LongFieldBehaviour with Matchers {

  val form = new HowManyLitresFormProvider().apply()

  ".lowBand" - {

    val fieldName = "lowBand"
    val requiredKey = "litres.error.lowBand.required"
    val numberKey = "litres.error.lowBand.nonNumeric"
    val negativeNumberKey = "litres.error.lowBand.negative"
    val maxValueKey = "litres.error.lowBand.outOfMaxVal"
    val wholeNumberKey = "litres.error.lowBand.wholeNumber"
    val maxValue = 100000000000000L
    val validDataGenerator = longInRangeWithCommas(0, maxValue)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like longField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, numberKey),
      negativeNumberError = FormError(fieldName, negativeNumberKey),
      wholeNumberError = FormError(fieldName, wholeNumberKey)
    )

    behave like longFieldWithMaximum(
      form,
      fieldName,
      maxValue,
      FormError(fieldName, maxValueKey, Seq(maxValue))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".highBand" - {

    val fieldName = "highBand"
    val requiredKey = "litres.error.highBand.required"
    val numberKey = "litres.error.highBand.nonNumeric"
    val negativeNumberKey = "litres.error.highBand.negative"
    val maxValueKey = "litres.error.highBand.outOfMaxVal"
    val wholeNumberKey = "litres.error.highBand.wholeNumber"
    val maxValue = 100000000000000L
    val validDataGenerator = longInRangeWithCommas(0, maxValue)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like longField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, numberKey),
      negativeNumberError = FormError(fieldName, negativeNumberKey),
      wholeNumberError = FormError(fieldName, wholeNumberKey)
    )

    behave like longFieldWithMaximum(
      form,
      fieldName,
      maxValue,
      FormError(fieldName, maxValueKey, Seq(maxValue))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "must return total litres less than 1" in {
    val result = form.bind(Map("lowBand" -> "0", "highBand" -> "0"))
    assert(result.errors.contains(FormError("lowBand",List("litres.error.minimum.total"))))
    assert(result.errors.contains(FormError("highBand",List("litres.error.minimum.total"))))
  }

  "must return not contain total less than 1 error if low band value is 0 and the high band is non-numeric" in {
    val result = form.bind(Map("lowBand" -> "0", "highBand" -> "foo"))
    result.errors must not contain(FormError("lowBand", List("litres.error.minimum.total")))
    assert(result.errors.contains(FormError("highBand", List("litres.error.highBand.nonNumeric"))))
  }

  "must return not contain total less than 1 error if high band value is 0 and the low band is non-numeric" in {
    val result = form.bind(Map("lowBand" -> "foo", "highBand" -> "0"))
    result.errors must not contain (FormError("highBand", List("litres.error.minimum.total")))
    assert(result.errors.contains(FormError("lowBand", List("litres.error.lowBand.nonNumeric"))))
  }
  
}
