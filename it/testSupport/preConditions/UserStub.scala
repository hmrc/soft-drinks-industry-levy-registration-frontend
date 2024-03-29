package testSupport.preConditions

import com.github.tomakehurst.wiremock.client.WireMock._

case class UserStub
()
(implicit builder: PreconditionBuilder) {


  def isAuthorisedButNotEnrolled() = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "some-id",
               |  "email": "test@test.com",
               |  "allEnrolments": [],
               |  "credentialRole": "user",
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isAuthorisedAndEnrolled = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "some-id",
               |  "email": "test@test.com",
               |  "allEnrolments": [{
               |     "key": "IR-CT",
               |     "identifiers": [{
               |       "key":"UTR",
               |       "value": "0000001611"
               |     }]
               |  }],
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }



  def isAuthorisedAndEnrolledSdilEnrolment = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "some-id",
               |  "email": "test@test.com",
               |  "allEnrolments": [{
               |     "key": "HMRC-OBTDS-ORG",
               |     "identifiers": [{
               |       "key":"EtmpRegistrationNumber",
               |       "value": "XKSDIL000000022"
               |     }]
               |  }],
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isAuthorisedAndEnrolledBoth = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "some-id",
               |  "email": "test@test.com",
               |  "allEnrolments": [{
               |     "key": "IR-CT",
               |     "identifiers": [{
               |       "key":"UTR",
               |       "value": "0000001611"
               |     }]
               |  }, {
               |     "key": "HMRC-OBTDS-ORG",
               |     "identifiers": [{
               |       "key":"EtmpRegistrationNumber",
               |       "value": "XKSDIL000000022"
               |     }]
               |  }],
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isAuthorisedWithMissingInternalId = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "email": "test@test.com",
               |  "allEnrolments": [],
               |  "credentialRole": "user",
               |  "credentialRole": "Assistant",
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isAuthorisedWithInvalidRole = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "some-id",
               |  "email": "test@test.com",
               |  "allEnrolments": [],
               |  "credentialRole": "user",
               |  "credentialRole": "Assistant",
               |  "affinityGroup" : "Organisation",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isAuthorisedButInvalidAffinity = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "some-id",
               |  "email": "test@test.com",
               |  "allEnrolments": [],
               |  "credentialRole": "user",
               |  "affinityGroup" : "Agent",
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isNotAuthorised(reason: String = "MissingBearerToken") = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(unauthorized().withHeader("WWW-Authenticate", s"""MDTP detail="$reason"""")))

    builder
  }

}
