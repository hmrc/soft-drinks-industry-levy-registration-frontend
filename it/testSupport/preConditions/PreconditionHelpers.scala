package testSupport.preConditions

trait PreconditionHelpers {
  implicit val builder: PreconditionBuilder

  // valid user preconditions
  def commonPrecondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscriptionNone("utr", "0000001611")
  }

  def authorisedWithSdilSubscriptionIncDeRegDatePrecondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscriptionWithDeRegDate("utr", "0000001611")
  }

  def authorisedButNoEnrolmentsPrecondition = {
    builder
      .user.isAuthorisedButNotEnrolled()
  }

  //invalid user preconditions


  def authorisedWithSdilSubscriptionNoDeRegDatePrecondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscription("utr", "0000001611")
  }

  def authorisedWithInvalidRolePrecondition  = {
    builder
      .user.isAuthorisedWithInvalidRole
  }

  def authorisedWithInvalidAffinityPrecondition = {
    builder
      .user.isAuthorisedButInvalidAffinity
  }

  def unauthorisedPrecondition = {
    builder
      .user.isNotAuthorised()
  }

  def authorisedButInternalIdPrecondition = {
    builder
      .user.isAuthorisedWithMissingInternalId
  }
}
