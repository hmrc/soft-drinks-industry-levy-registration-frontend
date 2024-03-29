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

import models.{CheckMode, LitresInBands, NormalMode}
import pages.behaviours.PageBehaviours

class HowManyImportsPageSpec extends PageBehaviours {

  "HowManyImportsPage" - {

    beRetrievable[LitresInBands](HowManyImportsPage)

    beSettable[LitresInBands](HowManyImportsPage)

    beRemovable[LitresInBands](HowManyImportsPage)

    s"url should be correct for $NormalMode" in {
      HowManyImportsPage.url(NormalMode) mustBe controllers.routes.HowManyImportsController.onPageLoad(NormalMode).url
    }
    s"url should be correct for $CheckMode" in {
      HowManyImportsPage.url(CheckMode) mustBe controllers.routes.HowManyImportsController.onPageLoad(CheckMode).url
    }
  }
}