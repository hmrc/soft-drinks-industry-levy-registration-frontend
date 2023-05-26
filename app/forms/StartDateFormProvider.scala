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

import java.time.LocalDate

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class StartDateFormProvider @Inject() extends Mappings {

  def today: LocalDate = LocalDate.now()
  def sdilFoundingDate: LocalDate = LocalDate.of(2018,4,5)

  def apply(): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = "startDate.error.invalid",
        allRequiredKey = "startDate.error.required.all",
        twoRequiredKey = "startDate.error.required.two",
        requiredKey    = "startDate.error.required"
      ).verifying(
        minDate(sdilFoundingDate, "startDate.minimumDate"),
        maxDate(today, "startDate.error.maximumDate")
      )
    )
}
