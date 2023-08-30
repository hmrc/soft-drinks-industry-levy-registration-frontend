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

package models

import models.backend.{Site, UkAddress}

import java.time.LocalDate

trait DataHelper {

  def testUkAddress(lines: List[String] = List.empty): UkAddress = UkAddress(
    lines = lines,
    postCode = "AA111AA"
  )

  def testProducer(
                    isProducer: Boolean,
                    isLarge: Option[Boolean] = None
                  ): Producer = Producer(
    isProducer = isProducer,
    isLarge = isLarge
  )

  def testRetrievedActivity(
                             smallProducer: Boolean = false,
                             largeProducer: Boolean = false,
                             contractPacker: Boolean = false,
                             importer: Boolean = false,
                             voluntaryRegistration: Boolean = false,
                           ): RetrievedActivity = RetrievedActivity(
    smallProducer = smallProducer,
    largeProducer = largeProducer,
    contractPacker = contractPacker,
    importer = importer,
    voluntaryRegistration = voluntaryRegistration
  )

  def testSite(
                address: UkAddress,
                ref: Option[String] = None,
                tradingName: Option[String] = None,
                closureDate: Option[LocalDate] = None
              ): Site = Site(
    address = address,
    ref = ref,
    tradingName = tradingName,
    closureDate = closureDate
  )

  def testContact(
                   name: Option[String] = Some("test name"),
                   positionInCompany: Option[String] = Some("test position"),
                   phoneNumber: String,
                   email: String
                 ): Contact = Contact(
    name = name,
    positionInCompany = positionInCompany,
    phoneNumber = phoneNumber,
    email = email
  )

  def testContactDetails(
                          fullName: String = "test name",
                          position: String  = "test position",
                          phoneNumber: String = "testnumber",
                          email: String  = "test@email.test",
                        ): ContactDetails = ContactDetails(
    fullName = fullName,
    position = position,
    phoneNumber = phoneNumber,
    email = email
  )

  def testWarehouse(
                     tradingName: String = "test trading name",
                     address: UkAddress
                   ): Warehouse = Warehouse(
    tradingName = Some(tradingName),
    address = address
  )


  def testRetrievedSubscription(
                                 utr: String = "testutr",
                                 sdilRef: String = "testref",
                                 orgName: String = "test name",
                                 address: UkAddress,
                                 activity: RetrievedActivity,
                                 liabilityDate: LocalDate,
                                 productionSites: List[Site],
                                 warehouseSites: List[Site],
                                 contact: Contact,
                                 deregDate: Option[LocalDate] = None
                               ): RetrievedSubscription = RetrievedSubscription(
    utr = utr,
    sdilRef = sdilRef,
    orgName = orgName,
    address = address,
    activity = activity,
    liabilityDate = liabilityDate,
    productionSites = productionSites,
    warehouseSites = warehouseSites,
    contact = contact,
    deregDate = deregDate
  )


}
