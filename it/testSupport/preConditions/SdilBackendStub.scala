package testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.{Pending, Registered, SubscriptionStatus}
import models.backend.{Site, UkAddress}
import models.{Contact, IndividualDetails, OrganisationDetails, RetrievedActivity, RetrievedSubscription, RosmRegistration}
import play.api.http.Status.ACCEPTED
import play.api.libs.json.Json

import java.time.LocalDate

case class SdilBackendStub()
                          (implicit builder: PreconditionBuilder)
{
  val aTradingName = "Bouncy Drinks Ltd"
  val aSubscription = RetrievedSubscription(
    utr = "0000001611",
    sdilRef = "XKSDIL000000022",
    orgName = "Super Lemonade Plc",
    address = UkAddress(List("63 Clifton Roundabout", "Worcester"), "WR53 7CX"),
    activity = RetrievedActivity(smallProducer = false, largeProducer = true, contractPacker = false, importer = false, voluntaryRegistration = false),
    liabilityDate = LocalDate.of(2018, 4, 19),
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

  def checkPendingQueue(utr: String, subStatus: SubscriptionStatus) = {
    val response = subStatus match {
      case Pending => status(ACCEPTED)
      case Registered => ok()
      case _ => notFound()
    }

    stubFor(
      get(
        urlPathMatching(s"/check-enrolment-status/$utr"))
        .willReturn(response)
    )
    builder
  }

  def checkPendingQueueError(utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/check-enrolment-status/$utr"))
        .willReturn(serverError())
    )
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


  def retrieveRosmError(utr: String) = {
    stubFor(
      get(
        urlPathMatching(s"/rosm-registration/lookup/$utr"))
        .willReturn(
          serverError()))
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

  def createSubscription(utr: String) = {
    stubFor(
      post(
        urlEqualTo(s"/subscription/utr/$utr/safeid"))
        .willReturn(
          ok()))
    builder
  }

  def createSubscriptionError(utr: String) = {
    stubFor(
      post(
        urlEqualTo(s"/subscription/utr/$utr/safeid"))
        .willReturn(
          serverError()))
    builder
  }

}

