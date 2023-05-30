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

import forms.mappings.Mappings
import models.ContactDetails
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class ContactDetailsFormProvider @Inject() extends Mappings {

  private val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""
  private val nameRegex = """^[a-zA-Z &`\\-\\'\\.^]{1,40}$"""
  private val position = """^[a-zA-Z &`\\-\\'\\.^]{1,155}$"""
  private val phoneNumberRegex = """^[A-Z0-9 )/(\\#+*\-]{1,24}$"""

  def apply(): Form[ContactDetails] = Form(
     mapping(
      "fullName" -> text("contactDetails.error.fullName.required")
        .verifying(maxLength(40, "contactDetails.error.fullName.length"))
        .verifying(regexp(nameRegex, "contactDetails.error.fullName.invalid")),
      "position" -> text("contactDetails.error.jobTitle.required")
        .verifying(maxLength(155, "contactDetails.error.jobTitle.length"))
        .verifying(regexp(position, "contactDetails.error.jobTitle.invalid")),
      "phoneNumber" -> text("contactDetails.error.phoneNumber.required")
        .verifying(maxLength(24, "contactDetails.error.phoneNumber.length"))
        .verifying(regexp(phoneNumberRegex, "contactDetails.error.phoneNumber.invalid")),
      "email" -> text("contactDetails.error.email.required")
        .verifying(maxLength(132, "contactDetails.error.email.length"))
        .verifying(regexp(emailRegex, "contactDetails.error.email.invalid"))
    )(ContactDetails.apply)(ContactDetails.unapply)
   )
 }
