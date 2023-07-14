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

  private[controllers] def returnMissingAnswers[A: ClassTag, B: ClassTag](list: List[RequiredPage[_,_,_]])(implicit request: DataRequest[_]): List[RequiredPage[_,_,_]] = {
    list.filterNot { listItem =>
      val currentPage: Option[A] = request.userAnswers.get(listItem.pageRequired.asInstanceOf[QuestionPage[A]])(listItem.reads.asInstanceOf[Reads[A]])
      (currentPage.isDefined, listItem.basedOnPreviousPage.isDefined) match {
        case (false, true) =>
            val previousPage: PreviousPage[QuestionPage[B], B] = listItem.basedOnPreviousPage.get.asInstanceOf[PreviousPage[QuestionPage[B], B]]
            val previousPageAnswer: Option[B] = request.userAnswers.get(previousPage.page)(previousPage.reads)
          !previousPageAnswer.exists(i => previousPage.previousPageAnswerRequired.contains(i))
        case (false, _) => false
        case _ => true
        }
    }
  }

  private[controllers] def journey: List[RequiredPage[_,_,_]] = {
    List(
      RequiredPage(VerifyPage, Option.empty)(implicitly[Reads[Verify]]),
      RequiredPage(OrganisationTypePage, Option.empty)(implicitly[Reads[OrganisationType]]),
      RequiredPage(HowManyLitresGloballyPage, Option.empty)(implicitly[Reads[HowManyLitresGlobally]]),
      RequiredPage(ThirdPartyPackagersPage,
        Some(PreviousPage(HowManyLitresGloballyPage, List(HowManyLitresGlobally.enumerable.withName("small").get))(implicitly[Reads[HowManyLitresGlobally]])))(implicitly[Reads[Boolean]]),
      RequiredPage(OperatePackagingSitesPage,
        Some(PreviousPage(HowManyLitresGloballyPage,
          List(HowManyLitresGlobally.enumerable.withName("small").get,
            HowManyLitresGlobally.enumerable.withName("large").get))(implicitly[Reads[HowManyLitresGlobally]])))(implicitly[Reads[Boolean]]),
      RequiredPage(HowManyOperatePackagingSitesPage,
        Some(PreviousPage(OperatePackagingSitesPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      RequiredPage(ContractPackingPage, Option.empty)(implicitly[Reads[Boolean]]),
      RequiredPage(HowManyContractPackingPage,
        Some(PreviousPage(ContractPackingPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      RequiredPage(ImportsPage, Option.empty)(implicitly[Reads[Boolean]]),
      RequiredPage(HowManyImportsPage,
        Some(PreviousPage(ImportsPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[LitresInBands]]),
      RequiredPage(StartDatePage, Option.empty)(implicitly[Reads[LocalDate]]),
      RequiredPage(PackAtBusinessAddressPage, Option.empty)(implicitly[Reads[Boolean]]),
      RequiredPage(PackagingSiteDetailsPage, Option.empty)(implicitly[Reads[Boolean]]),
      RequiredPage(AskSecondaryWarehousesPage, Option.empty)(implicitly[Reads[Boolean]]),
      RequiredPage(WarehouseDetailsPage,
        Some(PreviousPage(AskSecondaryWarehousesPage, List(true))(implicitly[Reads[Boolean]])))(implicitly[Reads[Boolean]]),
      RequiredPage(ContactDetailsPage, Option.empty)(implicitly[Reads[ContactDetails]])
    )
  }
}

case class RequiredPage[+A >: QuestionPage[C], +B >: PreviousPage[_, _], C](pageRequired: A, basedOnPreviousPage: Option[B])(val reads: Reads[C])
case class PreviousPage[+B >: QuestionPage[C],C](page: B, previousPageAnswerRequired: List[C])(val reads: Reads[C])
