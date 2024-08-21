package controllers

import models.{Identify, RegisterState, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.EnterBusinessDetailsPage
import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSResponse}
import play.api.test.WsTestClient
import testSupport.{ITCoreTestData, Specifications, TestConfiguration}

import java.time.Instant
import scala.concurrent.Future

trait ControllerITTestHelper extends Specifications with TestConfiguration with ITCoreTestData {

  def createClientRequestGet(client: WSClient, url: String): Future[WSResponse] = {
    client.url(url)
      .withFollowRedirects(false)
      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
      .get()
  }

  def createClientRequestPOST(client: WSClient, url: String, json: JsValue): Future[WSResponse] = {
    client.url(url)
      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
      .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
        "Csrf-Token" -> "nocheck")
      .withFollowRedirects(false)
      .post(json)
  }

  def testOtherSuccessUserTypes(url: String, expectedPageTitle: String, ua: UserAnswers = emptyUserAnswers): Unit = {
    "the user is authenticated, has a sdil subscription with a deregDate" - {
      s"render the $expectedPageTitle page" in {
        given.authorisedWithSdilSubscriptionIncDeRegDatePrecondition
        setAnswers(ua)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, url)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include(expectedPageTitle + " - Soft Drinks Industry Levy - GOV.UK")
          }
        }
      }
    }

    "the user is authenticated, has no sdilEnrolment but has entered utr" - {
      s"render the $expectedPageTitle page" in {
        given.user.isAuthorisedButNotEnrolled()
          .sdilBackend.retrieveRosm("0000001611")
          .sdilBackend.checkPendingQueueDoesntExist("0000001611")

        setAnswers(ua.copy(registerState = RegisterState.RegisterWithOtherUTR).set(EnterBusinessDetailsPage, Identify("0000001611", "GU14 8NL")).success.value)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, url)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include(expectedPageTitle + " - Soft Drinks Industry Levy - GOV.UK")
          }
        }
      }
    }
  }

  def testUnauthorisedUser(url: String, optJson: Option[JsValue] = None): Unit = {
    "the user is unauthenticated" - {
      "redirect to gg-signin" in {
        given.unauthorisedPrecondition

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/bas-gateway/sign-in")
          }
        }
      }
    }

    "the user is authorised and has a subscription with no dereg date" - {
      "redirect to sdil home" in {
        given.authorisedWithSdilSubscriptionNoDeRegDatePrecondition

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/soft-drinks-industry-levy")
          }
        }
      }
    }

    "the user is authorised but has an invalid role" - {
      "redirect to sdil home" in {
        given.authorisedWithInvalidRolePrecondition

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/soft-drinks-industry-levy")
          }
        }
      }
    }

    "the user is authorised but has an invalid rosm" - {
      "redirect to index page" in {
        given.authorisedWithSdilSubscriptionNoRosm

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/soft-drinks-industry-levy")
          }
        }
      }
    }

    "the user is authorised but has an invalid affinity group" - {
      "redirect to sdil home" in {
        given.authorisedWithInvalidAffinityPrecondition

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/soft-drinks-industry-levy")
          }
        }
      }
    }

    "the user is authorised but has no identifer" - {
      "render the error page" in {
        given.authorisedButInternalIdPrecondition

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 500
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Sorry, there is a problem with the service - 500 - Soft Drinks Industry Levy - GOV.UK"
          }
        }
      }
    }
  }

  def testUserWhoIsUnableToRegister(url: String, optJson: Option[JsValue] = None): Unit = {
    "when the user answers contain a submitted date" - {
      "should redirect to registration confirmation page" in {
        given.commonPrecondition
        setAnswers(emptyUserAnswers.copy(registerState = RegisterState.RegisterWithAuthUTR, submittedOn = Some(Instant.now)))
        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get mustBe routes.RegistrationConfirmationController.onPageLoad.url
          }
        }
      }
    }
    RegisterState.values.filterNot(state => RegisterState.canRegister(state)).foreach{registerState =>
      val expectedLocation = registerState match {
        case RegisterState.AlreadyRegistered => routes.AlreadyRegisteredController.onPageLoad.url
        case RegisterState.RegistrationPending => routes.RegistrationPendingController.onPageLoad.url
        case RegisterState.RequiresBusinessDetails => routes.EnterBusinessDetailsController.onPageLoad.url
        case _ => routes.ApplicationAlreadySubmittedController.onPageLoad.url
      }
      s"when the user has a register state of $registerState" - {
        s"should redirect to $expectedLocation" in {
          given.commonPrecondition
          setAnswers(emptyUserAnswers.copy(registerState = registerState))
          WsTestClient.withClient { client =>
            val result1 = optJson match {
              case Some(json) => createClientRequestPOST(client, url, json)
              case _ => createClientRequestGet(client, url)
            }

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).get mustBe expectedLocation
            }
          }
        }
      }
    }
  }

  def testAuthenticatedUserButNoUserAnswers(url: String, optJson: Option[JsValue] = None): Unit = {
    "the user is authenticated but has no user answers" - {
      "redirect to registration controller" in {
        given.commonPrecondition

        remove(identifier)

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/start")
          }
        }
      }
    }
  }
}
