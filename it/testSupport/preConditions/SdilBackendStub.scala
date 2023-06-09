package testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock._
import models.backend.{Site, UkAddress}
import models.{Contact, IndividualDetails, OrganisationDetails, RetrievedActivity, RetrievedSubscription, RosmRegistration}
import play.api.http.Status.ACCEPTED
import play.api.libs.json.Json

import java.time.LocalDate

case class SdilBackendStub()
                          (implicit builder: PreconditionBuilder)
{
  val aSubscription = RetrievedSubscription(
    utr = "0000001611",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
    productionSites = List(
      Site(
        UkAddress(List("33 Rhes Priordy", "East London"), "E73 2RP"),
        Some("88"),
        Some("Wild Lemonade Group"),
        Some(LocalDate.of(2018, 2, 26))),
      Site(
        UkAddress(List("117 Jerusalem Court", "St Albans"), "AL10 3UJ"),
        Some("87"),
        Some("Highly Addictive Drinks Plc"),
        Some(LocalDate.of(2019, 8, 19))),
      Site(
        UkAddress(List("87B North Liddle Street", "Guildford"), "GU34 7CM"),
        Some("94"),
        Some("Monster Bottle Ltd"),
        Some(LocalDate.of(2017, 9, 23))),
      Site(
        UkAddress(List("122 Dinsdale Crescent", "Romford"), "RM95 8FQ"),
        Some("27"),
        Some("Super Lemonade Group"),
        Some(LocalDate.of(2017, 4, 23))),
      Site(
        UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL"),
        Some("96"),
        Some("Star Products Ltd"),
        Some(LocalDate.of(2017, 2, 11)))
    ),
    warehouseSites = List(),
    contact = Contact(Some("Ava Adams"), Some("Chief Infrastructure Agent"), "04495 206189", "Adeline.Greene@gmail.com"),
    deregDate = None
  )

  val rosmRegistration = RosmRegistration(
    safeId = "safeid",
    organisation = Some(OrganisationDetails(organisationName = "Super Lemonade Plc")),
    individual = Some(IndividualDetails(firstName = "Ava" , lastName = "Adams")),
    address = UkAddress(List("105B Godfrey Marchant Grove", "Guildford"), "GU14 8NL")
  )

  val aSubscriptionWithDeRegDate = aSubscription.copy(
    deregDate = Some(LocalDate.of(2022, 2, 11)))

  def retrieveSubscription(identifier: String, refNum: String) = {
    stubFor(
      get(
        urlPathMatching(s"/subscription/$identifier/$refNum"))
        .willReturn(
          ok(Json.toJson(aSubscription).toString())))
    builder
  }
  def checkPendingQueueDoesntExist(utr: String) = {
    stubFor(
      get(
      urlPathMatching(s"/check-enrolment-status/$utr"))
      .willReturn(notFound())
    )
    builder
  }
  def checkPendingQueuePending(utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/check-enrolment-status/$utr"))
        .willReturn(status(ACCEPTED))
    )
    builder
  }
  def checkPendingQueueRegistered(utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/check-enrolment-status/$utr"))
        .willReturn(ok())
    )
    builder
  }

  def retrieveRosm(utr: String, rosmReg: RosmRegistration = rosmRegistration)= {
    stubFor(
      get(
        urlPathMatching(s"/rosm-registration/lookup/$utr"))
        .willReturn(
          ok(Json.toJson(rosmReg).toString())))
    builder
  }

  def retrieveRosmNone(utr: String)= {
    stubFor(
      get(
        urlPathMatching(s"/rosm-registration/lookup/$utr"))
        .willReturn(
          notFound()))
    builder
  }

  def retrieveSubscriptionWithDeRegDate(identifier: String, refNum: String) = {
    stubFor(
      get(
        urlEqualTo(s"/subscription/$identifier/$refNum"))
        .willReturn(
          ok(Json.toJson(aSubscriptionWithDeRegDate).toString())))
    builder
  }

  def retrieveSubscriptionNone(identifier: String, refNum: String) = {
    stubFor(
      get(
        urlPathMatching(s"/subscription/$identifier/$refNum"))
        .willReturn(
          notFound()))
    builder
  }

}

