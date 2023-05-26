/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.sdil.pages.returns

import uk.gov.hmrc.test.sdil.conf.TestConfiguration
import uk.gov.hmrc.test.sdil.pages.generic.BasePage

object Registrations_OperatePackagingSitesPage extends BasePage {

  override val url: String = TestConfiguration.url("registration-frontend") + "/operate-packaging-sites"
  override val title = "Do you operate any packaging sites in the UK to package liable drinks for the brands you own?"

  override def expectedPageErrorTitle: Option[String] = Some("")

  override def expectedPageTitle: Option[String] = Some(
    "Do you operate any packaging sites in the UK to package liable drinks for the brands you own? - Soft Drinks Industry Levy - GOV.UK"
  )

  override def expectedPageHeader: Option[String] = Some(
    "Do you operate any packaging sites in the UK to package liable drinks for the brands you own?"
  )

}
