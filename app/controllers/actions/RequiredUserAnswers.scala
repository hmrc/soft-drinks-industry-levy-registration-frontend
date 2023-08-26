/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.actions

import models.HowManyLitresGlobally._
import models.requests.DataRequest
import models.{ContactDetails, HowManyLitresGlobally, LitresInBands, NormalMode, OrganisationType, Verify}
import pages._
import play.api.libs.json.Reads
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import utilities.GenericLogger

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class RequiredUserAnswers @Inject()(genericLogger: GenericLogger)(implicit val executionContext: ExecutionContext) extends ActionHelpers {

  def requireData(page: Page)(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    page match {
      case CheckYourAnswersPage => checkYourAnswersRequiredData(action)
      case _ => action
    }
  }

  private[controllers] def checkYourAnswersRequiredData(action: => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    val userAnswersMissing: List[RequiredPage[_,_,_]] = returnMissingAnswers(journey)
    if (userAnswersMissing.nonEmpty) {
      genericLogger.logger.warn(s"${request.userAnswers.id} has hit CYA and is missing $userAnswersMissing")
      Future.successful(Redirect(controllers.routes.VerifyController.onPageLoad(NormalMode)))
    } else {
      action
    }
  }

  private[controllers] def returnMissingAnswers[A: ClassTag, B: ClassTag](list: List[RequiredPage[_, _, _]])
                                                                         (implicit request: DataRequest[_]): List[RequiredPage[_, _, _]] = {
    list.filterNot { listItem =>
      val currentPage: Option[A] = request.userAnswers.get(listItem.pageRequired.asInstanceOf[QuestionPage[A]])(listItem.reads.asInstanceOf[Reads[A]])
      (currentPage.isDefined, listItem.basedOnPreviousPages.nonEmpty) match {
        case (false, true) =>
          val userAnswersMatched: List[Boolean] = listItem.basedOnPreviousPages.map { previousListItem =>
            val previousPage: PreviousPage[QuestionPage[B], B] = previousListItem.asInstanceOf[PreviousPage[QuestionPage[B], B]]
            val previousPageAnswer: Option[B] = request.userAnswers.get(previousPage.page)(previousPage.reads)
            previousPage.previousPageAnswerRequired match {
              case Nil => !previousPageAnswer.isDefined
              case _ => !previousPageAnswer.exists(i => previousPage.previousPageAnswerRequired.contains(i))
            }
          }
          if (userAnswersMatched.contains(true) && userAnswersMatched.contains(false)) {
            true
          } else {
            !userAnswersMatched.contains(false)
          }
        case (false, _) => false
        case _ => true
      }
    }
  }

  private[controllers] def journey: List[RequiredPage[_,_,_]] = {
    val previousPageSmallAndNonProducer = PreviousPage(HowManyLitresGloballyPage, List(HowManyLitresGlobally.enumerable.withName("small").get,
      HowManyLitresGlobally.enumerable.withName("xnot").get))(implicitly[Reads[HowManyLitresGlobally]])
    val largeGlobal = HowManyLitresGlobally.enumerable.withName("large").get
    val smallGlobal = HowManyLitresGlobally.enumerable.withName("small").get
    val implicitBands = implicitly[Reads[LitresInBands]]
    val implicitGlobally = implicitly[Reads[HowManyLitresGlobally]]
    val implicitBoolean = implicitly[Reads[Boolean]]
    val implicitDate = implicitly[Reads[LocalDate]]
    List(
      RequiredPage(VerifyPage, List.empty)(implicitly[Reads[Verify]]),
      RequiredPage(OrganisationTypePage, List.empty)(implicitly[Reads[OrganisationType]]),
      RequiredPage(HowManyLitresGloballyPage, List.empty)(implicitGlobally),
      RequiredPage(ThirdPartyPackagersPage, List(PreviousPage(HowManyLitresGloballyPage, List(smallGlobal))(implicitGlobally)))(implicitBoolean),
      RequiredPage(OperatePackagingSitesPage, List(PreviousPage(HowManyLitresGloballyPage, List(smallGlobal, largeGlobal))(implicitGlobally)))(implicitBoolean),
      RequiredPage(HowManyOperatePackagingSitesPage, List(PreviousPage(OperatePackagingSitesPage, List(true))(implicitBoolean)))(implicitBands),
      RequiredPage(ContractPackingPage, List.empty)(implicitBoolean),
      RequiredPage(HowManyContractPackingPage, List(PreviousPage(ContractPackingPage, List(true))(implicitBoolean)))(implicitBands),
      RequiredPage(ImportsPage, List.empty)(implicitBoolean),
      RequiredPage(HowManyImportsPage, List(PreviousPage(ImportsPage, List(true))(implicitBoolean)))(implicitBands),
      RequiredPage(StartDatePage, List(PreviousPage(HowManyLitresGloballyPage, List(largeGlobal))(implicitGlobally)))(implicitDate),
      RequiredPage(StartDatePage, List(previousPageSmallAndNonProducer, PreviousPage(ContractPackingPage, List(true))(implicitBoolean)))(implicitDate),
      RequiredPage(StartDatePage, List(previousPageSmallAndNonProducer, PreviousPage(ImportsPage, List(true))(implicitBoolean)))(implicitDate),
      RequiredPage(PackAtBusinessAddressPage, List(previousPageSmallAndNonProducer, PreviousPage(ContractPackingPage, List(true))
        (implicitBoolean)))(implicitBoolean),
      RequiredPage(PackAtBusinessAddressPage, List(PreviousPage(HowManyLitresGloballyPage, List(largeGlobal))(implicitGlobally),
        PreviousPage(OperatePackagingSitesPage, List(false))(implicitBoolean), PreviousPage(ContractPackingPage, List(true))
        (implicitBoolean)))(implicitBoolean),
      RequiredPage(PackAtBusinessAddressPage, List(PreviousPage(HowManyLitresGloballyPage, List(largeGlobal))(implicitGlobally),
        PreviousPage(OperatePackagingSitesPage, List(true))(implicitBoolean), PreviousPage(ContractPackingPage, List(true, false))
        (implicitBoolean)))(implicitBoolean),
      RequiredPage(PackagingSiteDetailsPage, List(PreviousPage(PackAtBusinessAddressPage, List(true, false))(implicitBoolean)))(implicitBoolean),
      RequiredPage(AskSecondaryWarehousesPage, List(PreviousPage(StartDatePage, List.empty)(implicitDate)))(implicitBoolean),
      RequiredPage(WarehouseDetailsPage, List(PreviousPage(AskSecondaryWarehousesPage, List(true))(implicitBoolean)))(implicitBoolean),
      RequiredPage(ContactDetailsPage, List(PreviousPage(HowManyLitresGloballyPage, List(smallGlobal))(implicitGlobally),
        PreviousPage(ThirdPartyPackagersPage, List(true))(implicitBoolean), PreviousPage(OperatePackagingSitesPage, List(true, false))(implicitBoolean),
        PreviousPage(ContractPackingPage, List(false))(implicitBoolean), PreviousPage(ImportsPage, List(false))(implicitBoolean)))
          (implicitly[Reads[ContactDetails]]),
      RequiredPage(ContactDetailsPage, List(PreviousPage(AskSecondaryWarehousesPage, List(true, false))(implicitBoolean)))(implicitly[Reads[ContactDetails]])
    )
  }
}

case class RequiredPage[+A >: QuestionPage[C], +B >: PreviousPage[_, _], C](pageRequired: A, basedOnPreviousPages: List[B])(val reads: Reads[C])
case class PreviousPage[+B >: QuestionPage[C],C](page: B, previousPageAnswerRequired: List[C])(val reads: Reads[C])
