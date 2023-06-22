package testSupport.preConditions

trait PreconditionHelpers {
  implicit val builder: PreconditionBuilder

  // valid user preconditions
  def commonPrecondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveRosm("0000001611")
      .sdilBackend.retrieveSubscriptionNone("utr", "0000001611")
      .sdilBackend.checkPendingQueueDoesntExist("0000001611")
  }

  def authorisedWithSdilSubscriptionIncDeRegDatePrecondition = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscriptionWithDeRegDate("utr", "0000001611")
      .sdilBackend.retrieveRosm("0000001611")
      .sdilBackend.checkPendingQueueDoesntExist("0000001611")
  }
  def authorisedWithoutSdilSubscriptionPendingQueueContainsRecordOfPending = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscriptionNone("utr", "0000001611")
      .sdilBackend.checkPendingQueuePending("0000001611")
  }

  def authorisedWithoutSdilSubscriptionQueueContainsRecordOfRegistered = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscriptionNone("utr", "0000001611")
      .sdilBackend.checkPendingQueueRegistered("0000001611")
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
      .sdilBackend.checkPendingQueueDoesntExist("utr")
      .sdilBackend.retrieveRosm("0000001611")
  }

  def authorisedWithSdilSubscriptionNoRosm = {
    builder
      .user.isAuthorisedAndEnrolled
      .sdilBackend.retrieveSubscription("utr", "0000001611")
      .sdilBackend.checkPendingQueueDoesntExist("utr")
      .sdilBackend.retrieveRosmNone("0000001611")
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
