package controllers

import connectors._
import models.RegisterState._
import models.{NormalMode, RegisterState}
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, defined, empty, include}
import play.api.http.HeaderNames
import play.api.test.WsTestClient

class RegistrationControllerISpec extends ControllerITTestHelper {

  val path = "/start"

  val expectedRedirectUrlForSubscriptionStatus = Map(
      Pending -> routes.RegistrationPendingController.onPageLoad.url,
      Registered -> routes.ApplicationAlreadySubmittedController.onPageLoad.url,
      DoesNotExist -> routes.VerifyController.onPageLoad(NormalMode).url
    )

  val expectedRegisteredStateForSubscriptionStatus = Map(
    Pending -> RegistrationPending,
    Registered -> RegisterApplicationAccepted,
    DoesNotExist -> RegisterWithAuthUTR
  )

  s"GET $path" - {
    "when the user has an auth session that includes a utr and no sdilRef," - {
      "has no subscription," - {
        "has rosmData associated with the utr" - {
          expectedRedirectUrlForSubscriptionStatus.foreach { case (subscriptionState, expectedUrl) =>
            s"and has a subscription status of $subscriptionState" - {
              s"should create new user answers including the registered state and redirect to $expectedUrl" - {
                "when there is no user answers" in {
                  given
                    .user.isAuthorisedAndEnrolled
                    .sdilBackend.retrieveSubscriptionNone("utr", "0000001611")
                    .sdilBackend.retrieveRosm("0000001611")
                    .sdilBackend.checkPendingQueue("0000001611", subscriptionState)

                  WsTestClient.withClient { client =>
                    val result1 = createClientRequestGet(client, baseUrl + path)

                    whenReady(result1) { res =>
                      res.status mustBe 303
                      res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                      val userAnswers = getAnswers(identifier)
                      userAnswers mustBe defined
                      userAnswers.get.registerState mustBe expectedRegisteredStateForSubscriptionStatus(subscriptionState)
                    }
                  }
                }

                "when there is already user answers" in {
                  given
                    .user.isAuthorisedAndEnrolled
                    .sdilBackend.retrieveSubscriptionNone("utr", "0000001611")
                    .sdilBackend.retrieveRosm("0000001611")
                    .sdilBackend.checkPendingQueue("0000001611", subscriptionState)

                  setAnswers(emptyUserAnswers.copy(registerState = RegisterWithOtherUTR))

                  WsTestClient.withClient { client =>
                    val result1 = createClientRequestGet(client, baseUrl + path)

                    whenReady(result1) { res =>
                      res.status mustBe 303
                      res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                      val userAnswers = getAnswers(identifier)
                      userAnswers mustBe defined
                      userAnswers.get.registerState mustBe expectedRegisteredStateForSubscriptionStatus(subscriptionState)
                    }
                  }
                }
              }
            }
          }
        }

        "has no rosm data associated with the utr" - {
          "should create new useranswers and redirect the EnterBusinessDetails" in {
            given
              .user.isAuthorisedAndEnrolled
              .sdilBackend.retrieveSubscriptionNone("utr", "0000001611")
              .sdilBackend.retrieveRosmNone("0000001611")

            WsTestClient.withClient { client =>
              val result1 = createClientRequestGet(client, baseUrl + path)

              whenReady(result1) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.EnterBusinessDetailsController.onPageLoad.url)
                val userAnswers = getAnswers(identifier)
                userAnswers mustBe defined
                userAnswers.get.registerState mustBe RegisterState.RequiresBusinessDetails
              }
            }
          }
        }
      }

      "has a subscription that is deregistered," - {
        "has rosmData associated with the utr" - {
          expectedRedirectUrlForSubscriptionStatus.foreach { case (subscriptionState, expectedUrl) =>
            s"and has a subscription status of $subscriptionState" - {
              s"should create new user answers including the registered state and redirect to $expectedUrl" - {
                "when there is no user answers" in {
                  given
                    .user.isAuthorisedAndEnrolled
                    .sdilBackend.retrieveSubscriptionWithDeRegDate("utr", "0000001611")
                    .sdilBackend.retrieveRosm("0000001611")
                    .sdilBackend.checkPendingQueue("0000001611", subscriptionState)

                  WsTestClient.withClient { client =>
                    val result1 = createClientRequestGet(client, baseUrl + path)

                    whenReady(result1) { res =>
                      res.status mustBe 303
                      res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                      val userAnswers = getAnswers(identifier)
                      userAnswers mustBe defined
                      userAnswers.get.registerState mustBe expectedRegisteredStateForSubscriptionStatus(subscriptionState)
                    }
                  }
                }

                "when there is already user answers" in {
                  given
                    .user.isAuthorisedAndEnrolled
                    .sdilBackend.retrieveSubscriptionWithDeRegDate("utr", "0000001611")
                    .sdilBackend.retrieveRosm("0000001611")
                    .sdilBackend.checkPendingQueue("0000001611", subscriptionState)

                  setAnswers(emptyUserAnswers.copy(registerState = RegisterWithOtherUTR))

                  WsTestClient.withClient { client =>
                    val result1 = createClientRequestGet(client, baseUrl + path)

                    whenReady(result1) { res =>
                      res.status mustBe 303
                      res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                      val userAnswers = getAnswers(identifier)
                      userAnswers mustBe defined
                      userAnswers.get.registerState mustBe expectedRegisteredStateForSubscriptionStatus(subscriptionState)
                    }
                  }
                }
              }
            }
          }
        }

        "has no rosm data associated with the utr" - {
          "should create new useranswers and redirect the EnterBusinessDetails" in {
            given
              .user.isAuthorisedAndEnrolled
              .sdilBackend.retrieveSubscriptionWithDeRegDate("utr", "0000001611")
              .sdilBackend.retrieveRosmNone("0000001611")

            WsTestClient.withClient { client =>
              val result1 = createClientRequestGet(client, baseUrl + path)

              whenReady(result1) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.EnterBusinessDetailsController.onPageLoad.url)
                val userAnswers = getAnswers(identifier)
                userAnswers mustBe defined
                userAnswers.get.registerState mustBe RegisterState.RequiresBusinessDetails
              }
            }
          }
        }
      }

      "has a subscription with no deregistered date" - {
        "should redirect to sdilHome" in {
          given
            .user.isAuthorisedAndEnrolled
            .sdilBackend.retrieveSubscription("utr", "0000001611")

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).get must include("/soft-drinks-industry-levy")
              val userAnswers = getAnswers(identifier)
              userAnswers mustBe empty
            }
          }
        }
      }
    }

    "when the user has a utr and sdilRef in the useranswers" - {
      "has no subscription," - {
        "has rosmData associated with the utr" - {
          expectedRedirectUrlForSubscriptionStatus.foreach { case (subscriptionState, expectedUrl) =>
            s"and has a subscription status of $subscriptionState" - {
              s"should create new user answers including the registered state and redirect to $expectedUrl" - {
                "when there is no user answers" in {
                  given
                    .user.isAuthorisedAndEnrolledBoth
                    .sdilBackend.retrieveSubscriptionNone("utr", "0000001611")
                    .sdilBackend.retrieveRosm("0000001611")
                    .sdilBackend.checkPendingQueue("0000001611", subscriptionState)

                  WsTestClient.withClient { client =>
                    val result1 = createClientRequestGet(client, baseUrl + path)

                    whenReady(result1) { res =>
                      res.status mustBe 303
                      res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                      val userAnswers = getAnswers(identifier)
                      userAnswers mustBe defined
                      userAnswers.get.registerState mustBe expectedRegisteredStateForSubscriptionStatus(subscriptionState)
                    }
                  }
                }

                "when there is already user answers" in {
                  given
                    .user.isAuthorisedAndEnrolledBoth
                    .sdilBackend.retrieveSubscriptionNone("utr", "0000001611")
                    .sdilBackend.retrieveRosm("0000001611")
                    .sdilBackend.checkPendingQueue("0000001611", subscriptionState)

                  setAnswers(emptyUserAnswers.copy(registerState = RegisterWithOtherUTR))

                  WsTestClient.withClient { client =>
                    val result1 = createClientRequestGet(client, baseUrl + path)

                    whenReady(result1) { res =>
                      res.status mustBe 303
                      res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                      val userAnswers = getAnswers(identifier)
                      userAnswers mustBe defined
                      userAnswers.get.registerState mustBe expectedRegisteredStateForSubscriptionStatus(subscriptionState)
                    }
                  }
                }
              }
            }
          }
        }

        "has no rosm data associated with the utr" - {
          "should create new useranswers and redirect the EnterBusinessDetails" in {
            given
              .user.isAuthorisedAndEnrolledBoth
              .sdilBackend.retrieveSubscriptionNone("utr", "0000001611")
              .sdilBackend.retrieveRosmNone("0000001611")

            WsTestClient.withClient { client =>
              val result1 = createClientRequestGet(client, baseUrl + path)

              whenReady(result1) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.EnterBusinessDetailsController.onPageLoad.url)
                val userAnswers = getAnswers(identifier)
                userAnswers mustBe defined
                userAnswers.get.registerState mustBe RegisterState.RequiresBusinessDetails
              }
            }
          }
        }
      }

      "has a subscription that is deregistered," - {
        "has rosmData associated with the utr" - {
          expectedRedirectUrlForSubscriptionStatus.foreach { case (subscriptionState, expectedUrl) =>
            s"and has a subscription status of $subscriptionState" - {
              s"should create new user answers including the registered state and redirect to $expectedUrl" - {
                "when there is no user answers" in {
                  given
                    .user.isAuthorisedAndEnrolledBoth
                    .sdilBackend.retrieveSubscriptionWithDeRegDate("utr", "0000001611")
                    .sdilBackend.retrieveRosm("0000001611")
                    .sdilBackend.checkPendingQueue("0000001611", subscriptionState)

                  WsTestClient.withClient { client =>
                    val result1 = createClientRequestGet(client, baseUrl + path)

                    whenReady(result1) { res =>
                      res.status mustBe 303
                      res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                      val userAnswers = getAnswers(identifier)
                      userAnswers mustBe defined
                      userAnswers.get.registerState mustBe expectedRegisteredStateForSubscriptionStatus(subscriptionState)
                    }
                  }
                }

                "when there is already user answers" in {
                  given
                    .user.isAuthorisedAndEnrolledBoth
                    .sdilBackend.retrieveSubscriptionWithDeRegDate("utr", "0000001611")
                    .sdilBackend.retrieveRosm("0000001611")
                    .sdilBackend.checkPendingQueue("0000001611", subscriptionState)

                  setAnswers(emptyUserAnswers.copy(registerState = RegisterWithOtherUTR))

                  WsTestClient.withClient { client =>
                    val result1 = createClientRequestGet(client, baseUrl + path)

                    whenReady(result1) { res =>
                      res.status mustBe 303
                      res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                      val userAnswers = getAnswers(identifier)
                      userAnswers mustBe defined
                      userAnswers.get.registerState mustBe expectedRegisteredStateForSubscriptionStatus(subscriptionState)
                    }
                  }
                }
              }
            }
          }
        }

        "has no rosm data associated with the utr" - {
          "should create new useranswers and redirect the EnterBusinessDetails" in {
            given
              .user.isAuthorisedAndEnrolledBoth
              .sdilBackend.retrieveSubscriptionWithDeRegDate("utr", "0000001611")
              .sdilBackend.retrieveRosmNone("0000001611")

            WsTestClient.withClient { client =>
              val result1 = createClientRequestGet(client, baseUrl + path)

              whenReady(result1) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.EnterBusinessDetailsController.onPageLoad.url)
                val userAnswers = getAnswers(identifier)
                userAnswers mustBe defined
                userAnswers.get.registerState mustBe RegisterState.RequiresBusinessDetails
              }
            }
          }
        }
      }

      "has a subscription with no deregistered date" - {
        "and has rosm data associated with the utr" - {
          "should create new useranswers and redirect the AlreadyRegistered page" in {
            given
              .user.isAuthorisedAndEnrolledBoth
              .sdilBackend.retrieveSubscription("utr", "0000001611")
              .sdilBackend.retrieveRosm("0000001611")

            WsTestClient.withClient { client =>
              val result1 = createClientRequestGet(client, baseUrl + path)

              whenReady(result1) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.AlreadyRegisteredController.onPageLoad.url)
                val userAnswers = getAnswers(identifier)
                userAnswers mustBe defined
                userAnswers.get.registerState mustBe RegisterState.AlreadyRegistered
              }
            }
          }
        }

        "and has no rosm data associated with the utr" - {
          "should redirect the user to gg signin" in {
            given
              .user.isAuthorisedAndEnrolledBoth
              .sdilBackend.retrieveSubscription("utr", "0000001611")
              .sdilBackend.retrieveRosmNone("0000001611")

            WsTestClient.withClient { client =>
              val result1 = createClientRequestGet(client, baseUrl + path)

              whenReady(result1) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION).get must include("bas-gateway/sign-in")
                val userAnswers = getAnswers(identifier)
                userAnswers mustBe empty
              }
            }
          }
        }
      }
    }

    "when a user has a sdilRef but no utr in the auth session," - {
      "should redirect to sdilHome" - {
        "when the user has a subscription" in {
          given
            .user.isAuthorisedAndEnrolledSdilEnrolment
            .sdilBackend.retrieveSubscription("sdil", "XKSDIL000000022")

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).get must include("/soft-drinks-industry-levy")
              val userAnswers = getAnswers(identifier)
              userAnswers mustBe empty
            }
          }
        }
      }

      "should redirect to enter business details" - {
        "when the user has a subscription with a deregistered date" in {
          given
            .user.isAuthorisedAndEnrolledSdilEnrolment
            .sdilBackend.retrieveSubscriptionWithDeRegDate("sdil", "XKSDIL000000022")

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.EnterBusinessDetailsController.onPageLoad.url)
              val userAnswers = getAnswers(identifier)
              userAnswers mustBe defined
              userAnswers.get.registerState mustBe RegisterState.RequiresBusinessDetails
            }
          }
        }

        "when the user has no subscription" in {
          given
            .user.isAuthorisedAndEnrolledSdilEnrolment
            .sdilBackend.retrieveSubscriptionNone("sdil", "XKSDIL000000022")

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.EnterBusinessDetailsController.onPageLoad.url)
              val userAnswers = getAnswers(identifier)
              userAnswers mustBe defined
              userAnswers.get.registerState mustBe RegisterState.RequiresBusinessDetails
            }
          }
        }
      }
    }

    "when the user has no utr or sdilRef in auth session" - {
      "should redirect to enter business details" in {
        given
          .user.isAuthorisedButNotEnrolled()

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + path)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.EnterBusinessDetailsController.onPageLoad.url)
            val userAnswers = getAnswers(identifier)
            userAnswers mustBe defined
            userAnswers.get.registerState mustBe RegisterState.RequiresBusinessDetails
          }
        }
      }
    }
  }
}
