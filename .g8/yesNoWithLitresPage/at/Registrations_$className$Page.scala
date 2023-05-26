/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.sdil.pages.registration

import uk.gov.hmrc.test.sdil.conf.TestConfiguration
import uk.gov.hmrc.test.sdil.pages.generic.BasePage

object Registrations_$className$Page extends BasePage {

  override val url: String = TestConfiguration.url("registration-frontend") + "/$yesNoUrl$"
  override val title = "$yesNoTitle$"

  override def expectedPageErrorTitle: Option[String] = Some("")

  override def expectedPageTitle: Option[String] = Some(
    "$yesNoTitle$ - Soft Drinks Industry Levy - GOV.UK"
  )

  override def expectedPageHeader: Option[String] = Some(
    "$yesNoHeading$"
  )

}
