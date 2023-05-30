/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.test.sdil.pages.registration

import uk.gov.hmrc.test.sdil.conf.TestConfiguration
import uk.gov.hmrc.test.sdil.pages.generic.BasePage

object Registrations_HowManyOperatePackagingSitesPage extends BasePage {

  override val url: String = TestConfiguration.url("registration-frontend") + "/own-brands-next-12-months"
  override val title = "How many litres of our own brands will you package in the next 12 months?"

  override def expectedPageErrorTitle: Option[String] = Some("")

  override def expectedPageTitle: Option[String] = Some(
    "How many litres of our own brands will you package in the next 12 months? - Soft Drinks Industry Levy - GOV.UK"
  )

  override def expectedPageHeader: Option[String] = Some(
    "How many litres of our own brands will you package in the next 12 months?"
  )

}
