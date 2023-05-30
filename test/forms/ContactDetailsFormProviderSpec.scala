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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class ContactDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new ContactDetailsFormProvider()()

  ".fullName" - {

    val fieldName = "fullName"
    val requiredKey = "contactDetails.error.fullName.required"
    val lengthKey = "contactDetails.error.fullName.length"
    val maxLength = 40

//    behave like fieldThatBindsValidData(
//      form,
//      fieldName,
//      stringsWithMaxLength(maxLength)
//    )
//
//    behave like fieldWithMaxLength(
//      form,
//      fieldName,
//      maxLength = maxLength,
//      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
//    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
//
//  ".position" - {
//
//    val fieldName = "position"
//    val requiredKey = "contactDetails.error.position.required"
//    val lengthKey = "contactDetails.error.position.length"
//    val maxLength = 155
//
//    behave like fieldThatBindsValidData(
//      form,
//      fieldName,
//      stringsWithMaxLength(maxLength)
//    )
//
//    behave like fieldWithMaxLength(
//      form,
//      fieldName,
//      maxLength = maxLength,
//      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
//    )
//
//    behave like mandatoryField(
//      form,
//      fieldName,
//      requiredError = FormError(fieldName, requiredKey)
//    )
//  }
//
//  ".phoneNumber" - {
//
//    val fieldName = "phoneNumber"
//    val requiredKey = "contactDetails.error.phoneNumber.required"
//    val lengthKey = "contactDetails.error.phoneNumber.length"
//    val maxLength = 24
//
//    behave like fieldThatBindsValidData(
//      form,
//      fieldName,
//      stringsWithMaxLength(maxLength)
//    )
//
//    behave like fieldWithMaxLength(
//      form,
//      fieldName,
//      maxLength = maxLength,
//      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
//    )
//
//    behave like mandatoryField(
//      form,
//      fieldName,
//      requiredError = FormError(fieldName, requiredKey)
//    )
//  }
//
  ".email" - {
    val validEmailList = List("name@example.com", "test@test.com", "LongTestNameForEmailExample@example.com",
      "LongTestNameForEmailExampleWith55Characters@example.com", "LongTestNameForEmailExampleWith74Characters@WithALongDomainNameExample.com",
      "LongNameWITHCAPITALSForEmailExampleWith82Characters@WithALongDomainNameExample.com", "a@a.a", "1@a.a", "1@1.1",
      "lettersdigitsoranyofthefollowingspecialcharacters.!#$%&'*+/=?^_{|}~-@example.com",
      "3232-21982.digits.are.allowed?Anywhere*within*the^text@example.com",
      "a@lettersordigitsfollowedbyoptionalhyphenandmorelettersdigits61.lettersordigitsfollowedbyoptionalhyphen-UpTo61CharsAreAllowed",
      "132Total@lettersordigitsfollowedbyoptionalhyphenandmorelettersdigits61.lettersordigitsfollowedbyoptionalhyphen-UpTo61CharsAreAllowed",
      "lettersdigitsoranyofthefollowingspecialcharacters.!#$%&'*+/=?^_{|}~-AreAllowedButOnly1HypenAfterThe.portionTheBackPortions61Each@a.a")
    val overMaxLengthEmailList = List(
      "lettersdigitsoranyofthefollowingspecialcharacters.!#$%&'*+/=?^_{|}~-AreAllowedButOnly1HypenAfterThe.portionTheBackPortions61Each@a.ab",
      "134IsTotal@lettersordigitsfollowedbyoptionalhyphenandmorelettersdigits61.lettersordigitsfollowedbyoptionalhyphen-UpTo61CharsAreAllowed",
      "aWayTooLong@lettersordigitsfollowedbyoptionalhyphenandmorelettersdigits61.lettersordigitsfollowedbyoptionalhyphen-UpTo61CharsAreAllowed",
      )
    val invalidEmailList = List("1.com", "commas,are,not,allowed@example.com",
      "a@lettersordigitsfollowedbyoptionalhyphenandmoreletters.lettersordigitsfollowedbyoptionalhyphen-UpTo61CharsAreAllowedTooManyHere",
      "invalid.IsNotAllowed@example.lettersordigitsfollowedby-optionalhyphen-UpTo61CharsAreAllowed@",
      "invalid.IsNotAllowed@example.lettersordigitsfollowedby-optionalhyphen-UpTo61CharsAreAllowed-")

    val fieldName = "email"
    val requiredKey = "contactDetails.error.email.required"
    val lengthKey = "contactDetails.error.email.length"
    val invalidKey = "contactDetails.error.email.invalid"
    val emailRegEx = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""

    "should bind successfully with valid data" in {
      validEmailList.foreach(email => form.bind(Map("fullName" -> "Jane Doe", "position" -> "CEO", "phoneNumber" -> "07700 09900", "email" -> email))
        .errors mustBe List.empty)
    }

    "should provide the correct Error key when the email address is over 132 characters" in {
      overMaxLengthEmailList.foreach(
        email => form.bind(Map("fullName" -> "Jane Doe", "position" -> "CEO", "phoneNumber" -> "07700 09900", "email" -> email))
        .errors mustBe List(FormError("email", List(lengthKey), ArraySeq(132))))
    }

    "should provide the correct Error key when the email address is invalid" in {
     invalidEmailList.foreach(email => {
        println(Console.YELLOW + "email " + email + Console.WHITE)
        form.bind(Map("fullName" -> "Jane Doe", "position" -> "CEO", "phoneNumber" -> "07700 09900", "email" -> email))
      }.errors mustEqual List(FormError("email", List(invalidKey), ArraySeq(emailRegEx))))
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
