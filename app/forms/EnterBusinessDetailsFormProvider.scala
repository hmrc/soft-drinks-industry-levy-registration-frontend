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
import models.Identify
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{ Constraint, Invalid, Valid }

import javax.inject.Inject

class EnterBusinessDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[Identify] =
    Form(
      mapping(
        "utr" -> text("enterBusinessDetails.required.utr").verifying(Constraint { x: String =>
          x.replace(" ", "") match {
            case "" => Invalid("enterBusinessDetails.required.utr")
            case utr if utr.exists(!_.isDigit) => Invalid("enterBusinessDetails.invalid.utr")
            case utr if utr.length != 10 => Invalid("enterBusinessDetails.invalid.utr.length")
            case _ => Valid
          }
        }),
        "postcode" -> postcode)(Identify.apply)(Identify.unapply))

}
