/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package testSupport.preConditions

trait PreconditionHelpers {
  implicit val builder: PreconditionBuilder

  // valid user preconditions
  def commonPrecondition =
    builder.user.isAuthorisedAndEnrolled.sdilBackend
      .retrieveRosm("0000001611")
      .sdilBackend
      .retrieveSubscriptionNone("utr", "0000001611")
      .sdilBackend
      .checkPendingQueueDoesntExist("0000001611")

  def authorisedWithSdilSubscriptionIncDeRegDatePrecondition               =
    builder.user.isAuthorisedAndEnrolled.sdilBackend
      .retrieveSubscriptionWithDeRegDate("utr", "0000001611")
      .sdilBackend
      .retrieveRosm("0000001611")
      .sdilBackend
      .checkPendingQueueDoesntExist("0000001611")
  def authorisedWithoutSdilSubscriptionPendingQueueContainsRecordOfPending =
    builder.user.isAuthorisedAndEnrolled.sdilBackend
      .retrieveSubscriptionNone("utr", "0000001611")
      .sdilBackend
      .retrieveRosm("0000001611")
      .sdilBackend
      .checkPendingQueuePending("0000001611")

  def authorisedWithBothSDILandUTRInEnrolmentsAndHasROSM =
    builder.user.isAuthorisedAndEnrolledBoth.sdilBackend
      .retrieveSubscription("utr", "0000001611")
      .sdilBackend
      .retrieveRosm("0000001611")

  def authorisedButNoEnrolmentsPrecondition =
    builder.user.isAuthorisedButNotEnrolled()

  // invalid user preconditions

  def authorisedWithSdilSubscriptionNoDeRegDatePrecondition =
    builder.user.isAuthorisedAndEnrolled.sdilBackend
      .retrieveSubscription("utr", "0000001611")
      .sdilBackend
      .checkPendingQueueDoesntExist("utr")
      .sdilBackend
      .retrieveRosm("0000001611")

  def authorisedWithSdilSubscriptionNoRosm =
    builder.user.isAuthorisedAndEnrolled.sdilBackend
      .retrieveSubscription("utr", "0000001611")
      .sdilBackend
      .checkPendingQueueDoesntExist("utr")
      .sdilBackend
      .retrieveRosmNone("0000001611")

  def authorisedWithInvalidRolePrecondition =
    builder.user.isAuthorisedWithInvalidRole

  def authorisedWithInvalidAffinityPrecondition =
    builder.user.isAuthorisedButInvalidAffinity

  def unauthorisedPrecondition =
    builder.user.isNotAuthorised()

  def authorisedButInternalIdPrecondition =
    builder.user.isAuthorisedWithMissingInternalId
}
