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

package pages

import models.{CheckMode, NormalMode}
import pages.behaviours.PageBehaviours

class AskSecondaryWarehousesPageSpec extends PageBehaviours {

  "AskSecondaryWarehousesPage" - {

    beRetrievable[Boolean](AskSecondaryWarehousesPage)

    beSettable[Boolean](AskSecondaryWarehousesPage)

    beRemovable[Boolean](AskSecondaryWarehousesPage)

    s"url should be correct for $NormalMode" in {
      AskSecondaryWarehousesPage.url(NormalMode) mustBe controllers.routes.AskSecondaryWarehousesController.onPageLoad(NormalMode).url
    }
    s"url should be correct for $CheckMode" in {
      AskSecondaryWarehousesPage.url(CheckMode) mustBe controllers.routes.AskSecondaryWarehousesController.onPageLoad(CheckMode).url
    }
  }
}
