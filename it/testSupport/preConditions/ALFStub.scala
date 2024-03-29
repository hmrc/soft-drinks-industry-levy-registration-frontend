package testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock._
import models.alf.{AlfAddress, AlfResponse}
import play.api.http.Status
import play.api.libs.json.Json
import play.mvc.Http.HeaderNames


case class ALFStub()(implicit builder: PreconditionBuilder) {
  val aAddress = AlfResponse(address = AlfAddress(
    organisation = Some("soft drinks ltd"),
    List("line 1", "line 2", "line 3", "line 4"),
    postcode = Some("aa1 1aa"),
    countryCode = Some("UK")
  ))

  val aAddressNoOrg = AlfResponse(address = AlfAddress(
    organisation = None,
    List("line 1", "line 2", "line 3", "line 4"),
    postcode = Some("aa1 1aa"),
    countryCode = Some("UK")
  ))

  val BadAddress = (
    "Failed Address"
  )

  def getAddress(id : String, hasTradingName: Boolean = true) ={
    val address = if(hasTradingName) {
      aAddress
    } else {
      aAddressNoOrg
    }
    stubFor(
      get(
        urlPathMatching(s"/api/confirmed")
      ).withQueryParam("id",equalTo(id))
        .willReturn(
        ok(Json.toJson(address).toString())))
    builder
  }

  def getBadAddress(id : String) ={
    stubFor(
      get(
        urlPathMatching(s"/api/confirmed")
      ).withQueryParam("id",equalTo(id))
        .willReturn(
          ok(Json.toJson(BadAddress).toString())))
    builder
  }

  def getBadResponse(id : String) ={
    stubFor(
      get(
        urlPathMatching(s"/api/confirmed")
      ).withQueryParam("id",equalTo(id))
        .willReturn(
          notFound()))
    builder
  }

  def getSuccessResponseFromALFInit( locationHeaderReturned: String) = {
    stubFor(
      post(
        urlPathMatching("/api/init")
      )
        .willReturn(
        status(Status.ACCEPTED)
          .withHeader(HeaderNames.LOCATION, locationHeaderReturned)
      ))
  }
  def getFailResponseFromALFInit( statusReturned: Int) = {
    stubFor(
      post(
        urlPathMatching("/api/init")
      ).willReturn(
          status(statusReturned)
        ))
  }



}
