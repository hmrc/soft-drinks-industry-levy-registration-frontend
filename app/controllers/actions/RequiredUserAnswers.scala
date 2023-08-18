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
import models.{ContactDetails, HowManyLitresGlobally, LitresInBands, NormalMode, OrganisationType, Producer, UserAnswers, Verify}
import pages._
import play.api.libs.json.Reads
import play.api.mvc.Result
import play.api.mvc.Results.{Redirect, contentDispositionHeader}
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

  private[controllers] def returnMissingAnswers[A: ClassTag, B: ClassTag](list: List[RequiredPage[_,_,_]])
                                                                         (implicit request: DataRequest[_]): List[RequiredPage[_,_,_]] = {
    list.filterNot { listItem =>
      val currentPage: Option[A] = request.userAnswers.get(listItem.pageRequired.asInstanceOf[QuestionPage[A]])(listItem.reads.asInstanceOf[Reads[A]])
      (currentPage.isDefined, listItem.basedOnPreviousPages.nonEmpty) match {
        case (false, true) =>
           val userAnswersMatched:List[Boolean] = listItem.basedOnPreviousPages.map { previousListItem =>
            val previousPage: PreviousPage[QuestionPage[B], B] = previousListItem.asInstanceOf[PreviousPage[QuestionPage[B], B]]
            val previousPageAnswer: Option[B] = request.userAnswers.get(previousPage.page)(previousPage.reads)
            !previousPageAnswer.exists(i => previousPage.previousPageAnswerRequired.contains(i))
          }
          if(userAnswersMatched.contains(true) && userAnswersMatched.contains(false)) {
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
    List(
      RequiredPage(VerifyPage, List.empty)(implicitly[Reads[Verify]]),
      RequiredPage(OrganisationTypePage, List.empty)(implicitly[Reads[OrganisationType]]),
      RequiredPage(HowManyLitresGloballyPage, List.empty)(implicitly[Reads[HowManyLitresGlobally]]),
      RequiredPage(ThirdPartyPackagersPage,
        List(PreviousPage(HowManyLitresGloballyPage,
          List(HowManyLitresGlobally.enumerable.withName("small").get))(implicitly[Reads[HowManyLitresGlobally]])))(implicitly[Reads[Boolean]]),
      RequiredPage(OperatePackagingSitesPage,
        List(PreviousPage(HowManyLitresGloballyPage,
          List(HowManyLitresGlobally.enumerable.withName("small").get,
            HowManyLitresGlobally.enumerable.withName("large").get))(implicitly[Reads[HowManyLitresGlobally]])))(implicitly[Reads[Boolean]]),
      RequiredPage(HowManyOperatePackagingSitesPage,
        List(PreviousPage(OperatePackagingSitesPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      RequiredPage(HowManyContractPackingPage,
        List(PreviousPage(ContractPackingPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      RequiredPage(ImportsPage, List.empty)(implicitly[Reads[Boolean]]),
      RequiredPage(HowManyImportsPage,
        List(PreviousPage(ImportsPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      RequiredPage(StartDatePage, List.empty)(implicitly[Reads[LocalDate]]),
//      RequiredPage(PackAtBusinessAddressPage, List.empty)(implicitly[Reads[Boolean]]),
      RequiredPage(PackAtBusinessAddressPage,
        List(PreviousPage(HowManyLitresGloballyPage,
          List(HowManyLitresGlobally.enumerable.withName("small").get, HowManyLitresGlobally.enumerable.withName("xnot").get))(implicitly[Reads[HowManyLitresGlobally]]),
            PreviousPage(HowManyOperatePackagingSitesPage, List(true))(implicitly[Reads[Boolean]]),
            PreviousPage(ContractPackingPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[Boolean]]),

        RequiredPage(PackagingSiteDetailsPage, List.empty)(implicitly[Reads[Boolean]]),
      RequiredPage(AskSecondaryWarehousesPage, List.empty)(implicitly[Reads[Boolean]]),
      RequiredPage(WarehouseDetailsPage,
        List(PreviousPage(AskSecondaryWarehousesPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[Boolean]]),
      RequiredPage(ContactDetailsPage, List.empty)(implicitly[Reads[ContactDetails]])
    )
  }
}

case class RequiredPage[+A >: QuestionPage[C], +B >: PreviousPage[_, _], C](pageRequired: A, basedOnPreviousPages: List[B])(val reads: Reads[C])
case class PreviousPage[+B >: QuestionPage[C],C](page: B, previousPageAnswerRequired: List[C])(val reads: Reads[C])
//
//Large producer
//  Show packaging site
//OperatePackagingSitesPage = false && ContractPackingPage = true
//or
//OperatePackagingSitesPage = true && ContractPackingPage = false
//or
//OperatePackagingSitesPage = true && ContractPackingPage = true
//
//RequiredPage(PackAtBusinessAddressPage,
//List(PreviousPage(HowManyOperatePackagingSitesPage, List(false))(implicitly[Reads[Boolean]]), PreviousPage(ContractPackingPage, List(true))(implicitly[Reads[Boolean]])))
//
//RequiredPage(PackAtBusinessAddressPage,
//List(PreviousPage(HowManyOperatePackagingSitesPage, List(true))(implicitly[Reads[Boolean]]), PreviousPage(ContractPackingPage, List(false))(implicitly[Reads[Boolean]])))
//
//RequiredPage(PackAtBusinessAddressPage,
//  List(PreviousPage(HowManyLitresGloballyPage,
//    List(HowManyLitresGlobally.enumerable.withName("small").get))(implicitly[Reads[HowManyLitresGlobally]]), PreviousPage(HowManyOperatePackagingSitesPage, List(true))(implicitly[Reads[Boolean]]), PreviousPage(ContractPackingPage, List(true))(implicitly[Reads[Boolean]])))
//
//Small Producer & NonProducer
//  Show packaging site
//ContractPackingPage = true
//  RequiredPage(PackAtBusinessAddressPage,
//  List(PreviousPage(ContractPackingPage, List(true))(implicitly[Reads[Boolean]])))