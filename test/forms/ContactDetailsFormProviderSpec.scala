package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ContactDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new ContactDetailsFormProvider()()

  ".Full name" - {

    val fieldName = "Full name"
    val requiredKey = "contactDetails.error.Full name.required"
    val lengthKey = "contactDetails.error.Full name.length"
    val maxLength = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".Job title" - {

    val fieldName = "Job title"
    val requiredKey = "contactDetails.error.Job title.required"
    val lengthKey = "contactDetails.error.Job title.length"
    val maxLength = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
