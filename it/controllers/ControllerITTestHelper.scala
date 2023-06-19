package controllers

import connectors.{Pending, Registered}
import models.{Identify, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.IdentifyPage
import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSResponse}
import play.api.test.WsTestClient
import testSupport.{ITCoreTestData, Specifications, TestConfiguration}

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

  def testOtherSuccessUserTypes(url: String, expectedPageTitle: String, userAnswers: UserAnswers = emptyUserAnswers): Unit = {
    "the user is authenticated, has a sdil subscription with a deregDate" - {
      s"render the $expectedPageTitle page" in {
        given.authorisedWithSdilSubscriptionIncDeRegDatePrecondition

        setAnswers(userAnswers)

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

      "the user is authenticated by Identify page with no enrolments, has a sdil subscription with a deregDate no pending sub" - {
        s"render the $expectedPageTitle page" in {
          given.authorisedButNoEnrolmentsPrecondition
          given.sdilBackend.retrieveRosm("1")
          given.sdilBackend.checkPendingQueueDoesntExist("1")

          setAnswers(userAnswers.set(IdentifyPage,Identify(utr = "1", postCode = "fakepostcode")).success.value)

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
    s"the user is authenticated by Identify page with no enrolments, has a sdil subscription with a deregDate $Pending sub" - {
      s"redirect to journey stopper" in {
        given.authorisedButNoEnrolmentsPrecondition
        given.sdilBackend.retrieveRosm("1")
        given.sdilBackend.checkPendingQueuePending("1")

        setAnswers(userAnswers.set(IdentifyPage,Identify(utr = "1", postCode = "fakepostcode")).success.value)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, url)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad().url)
          }
        }
      }
    }
    s"the user is authenticated by Identify page with no enrolments, has a sdil subscription with a deregDate $Registered sub" - {
      s"redirect to journey stopper" in {
        given.authorisedButNoEnrolmentsPrecondition
        given.sdilBackend.retrieveRosm("1")
        given.sdilBackend.checkPendingQueueRegistered("1")

        setAnswers(userAnswers.set(IdentifyPage,Identify(utr = "1", postCode = "fakepostcode")).success.value)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, url)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad().url)
          }
        }
      }
    }

    "the user is authenticated with no enrolments and has not entered utr from identify page" - {
      s"redirect the user to identify page" in {
        given.authorisedButNoEnrolmentsPrecondition

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, url)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.IndexController.onPageLoad().url)
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
    s"the user is authed, no sub, $Pending sub in queue" - {
      "redirect to journey stopper" in {
        given.authorisedWithoutSdilSubscriptionPendingQueueContainsRecordOfPending
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
    s"the user is authed, no sub, $Registered sub in queue" - {
      "redirect to journey stopper" in {
        given.authorisedWithoutSdilSubscriptionQueueContainsRecordOfRegistered

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
            page.title() mustBe "Sorry, we are experiencing technical difficulties - 500 - Soft Drinks Industry Levy - GOV.UK"
          }
        }
      }
    }
  }

  def testAuthenticatedUserButNoUserAnswers(url: String, optJson: Option[JsValue] = None): Unit = {
    "the user is authenticated but has no user answers" - {
      "redirect to journey recover controller" in {
        given.commonPrecondition

        remove(identifier)

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/there-is-a-problem")
          }
        }
      }
    }
  }
}

