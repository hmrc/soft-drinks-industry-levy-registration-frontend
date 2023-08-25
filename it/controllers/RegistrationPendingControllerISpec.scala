package controllers

import models.backend.UkAddress
import models.{Identify, IndividualDetails, OrganisationDetails, RegisterState, RosmRegistration}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.EnterBusinessDetailsPage
import play.api.i18n.Messages
import play.api.test.WsTestClient

class RegistrationPendingControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/application-already-sent"

  "GET " + normalRoutePath - {
    s"should return OK and render the RegistrationPending page when $EnterBusinessDetailsPage UTR IS NOT Populated" in {
      given
        .authorisedWithoutSdilSubscriptionPendingQueueContainsRecordOfPending

      setAnswers(emptyUserAnswers.copy(registerState = RegisterState.RegistrationPending))

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(Messages("registrationPending" + ".title"))
          page.getElementById("utrField").text() mustBe "0000001611:"
          page.getElementById("pendingUTRAddress").text() mustBe "Super Lemonade Plc 105B Godfrey Marchant Grove Guildford GU14 8NL"
          page.getElementById("subText2").text() mustBe "If you have not got your registration number within 24 hours you need to call the Soft Drinks Industry Levy Helpline on 0300 200 1000."
        }
      }
    }
    s"should return OK and render the RegistrationPending page when $EnterBusinessDetailsPage UTR IS Populated" in {
      val utr = "utrFoo"
      val rosmReg =  RosmRegistration(
        safeId = "safeid",
        organisation = Some(OrganisationDetails(organisationName = "foo")),
        individual = Some(IndividualDetails(firstName = "wizz" , lastName = "bar")),
        address = UkAddress(List("bang", "BANG2", "bang3", "bang4"), "wollop")
      )

      given
        .authorisedButNoEnrolmentsPrecondition
        .sdilBackend.checkPendingQueueDoesntExist(utr)
        .sdilBackend.retrieveRosm(utr, rosmReg)

      setAnswers(emptyUserAnswers.copy(registerState = RegisterState.RegistrationPending).set(EnterBusinessDetailsPage, Identify(utr, "posty")).success.value)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(Messages("registrationPending" + ".title"))
          page.getElementById("utrField").text() mustBe utr + ":"
          page.getElementById("pendingUTRAddress").text() mustBe "foo bang BANG2 bang3 bang4 wollop"
          page.getElementById("subText2").text() mustBe "If you have not got your registration number within 24 hours you need to call the Soft Drinks Industry Levy Helpline on 0300 200 1000."
        }
      }
    }
    testOtherSuccessUserTypes(baseUrl + normalRoutePath, Messages("registrationPending" + ".title"), ua = emptyUserAnswers.copy(registerState = RegisterState.RegistrationPending))
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }
}
