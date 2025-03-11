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

package forms.mappings

import models.Enumerable
import play.api.data.Forms.of
import play.api.data.format.Formats.stringFormat
import play.api.data.validation.{ Constraint, Invalid, Valid, ValidationError }
import play.api.data.{ FieldMapping, Mapping }

import java.time.LocalDate

trait Mappings extends Formatters with Constraints {

  val postcodeCharactersRegex = "^[a-zA-Z0-9 ]+$"

  val postcodeRegex = "^[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}$|BFPO\\s?[0-9]{1,5}$"

  def addSpaceBeforeLastThreeCharsToPostCode(postCode: String) = {
    val trimmedPostCode = postCode.replace(" ", "").toUpperCase
    if (trimmedPostCode.startsWith("BFPO")) trimmedPostCode else s"${trimmedPostCode.dropRight(3)} ${trimmedPostCode.takeRight(3)}"
  }

  val trimmedUppercaseText: Mapping[String] = of[String].transform(addSpaceBeforeLastThreeCharsToPostCode, identity)

  def validPostcode(invalidFormatFailure: String, emptyFailure: String, invalidCharactersFailure: String): Constraint[String] =
    Constraint[String] { (input: String) =>
      if (input.isEmpty) Invalid(ValidationError(emptyFailure))
      else if (!input.matches(postcodeCharactersRegex)) Invalid(ValidationError(invalidCharactersFailure))
      else if (!input.matches(postcodeRegex)) Invalid(ValidationError(invalidFormatFailure))
      else Valid
    }

  def postcode: Mapping[String] =
    trimmedUppercaseText.verifying(
      validPostcode(
        invalidFormatFailure = "enterBusinessDetails.postcode.invalid",
        emptyFailure = "enterBusinessDetails.postcode.required",
        invalidCharactersFailure = "enterBusinessDetails.postcode.special"))

  protected def text(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(stringFormatter(errorKey, args))

  protected def int(
    requiredKey: String = "error.required",
    wholeNumberKey: String = "error.wholeNumber",
    nonNumericKey: String = "error.nonNumeric",
    invalidLength: String = "error.length",
    args: Seq[String] = Seq.empty): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey, invalidLength, args))

  protected def litres(
    band: String,
    args: Seq[String] = Seq.empty): Mapping[Long] =
    of(litresFormatter(band, args))
      .verifying(maximumValueNotEqual(100000000000000L, s"litres.error.$band.outOfMaxVal"))

  protected def boolean(
    requiredKey: String = "error.required",
    invalidKey: String = "error.boolean",
    args: Seq[String] = Seq.empty): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey, args))

  protected def enumerable[A](
    requiredKey: String = "error.required",
    invalidKey: String = "error.invalid",
    args: Seq[String] = Seq.empty)(implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey, args))

  protected def localDate(
    invalidKey: String,
    allRequiredKey: String,
    twoRequiredKey: String,
    requiredKey: String,
    invalidDay: String,
    invalidDayLength: String,
    invalidMonth: String,
    invalidMonthLength: String,
    invalidYear: String,
    invalidYearLength: String,
    args: Seq[String] = Seq.empty): FieldMapping[LocalDate] =
    of(new LocalDateFormatter(
      invalidKey,
      allRequiredKey,
      twoRequiredKey,
      requiredKey,
      invalidDay,
      invalidDayLength,
      invalidMonth,
      invalidMonthLength,
      invalidYear,
      invalidYearLength,
      args))
}
