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

package controllers

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CheckYourAnswersView
import views.summary.{ContractPackingSummary, ImportsSummary, OperatePackagingSitesSummary}

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val operatePackagingSites: Option[(String, SummaryList)] = {
        val list = OperatePackagingSitesSummary.summaryList(userAnswers = request.userAnswers, isCheckAnswers = true)
        list.rows.headOption.fold(Option.empty[(String, SummaryList)])(_ => Some("operatePackagingSites.checkYourAnswersLabel" -> list))
      }
      val contractPacking: Option[(String, SummaryList)] = {
        val list = ContractPackingSummary.summaryList(userAnswers = request.userAnswers, isCheckAnswers = true)
        list.rows.headOption.fold(Option.empty[(String, SummaryList)])(_ => Some("contractPacking.checkYourAnswersLabel" -> list))
      }
      val imports: Option[(String, SummaryList)] = {
        val list = ImportsSummary.summaryList(userAnswers = request.userAnswers, isCheckAnswers = true)
        list.rows.headOption.fold(Option.empty[(String, SummaryList)])(_ => Some("imports.checkYourAnswersLabel" -> list))
      }
      val summaryList: Seq[(String, SummaryList)] = Seq(operatePackagingSites, contractPacking, imports).flatten

      Ok(view(summaryList, routes.CheckYourAnswersController.onSubmit()))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    Redirect(controllers.routes.IndexController.onPageLoad().url)
  }
}


