package pages

import pages.behaviours.PageBehaviours

class OperatePackagingSitesPageSpec extends PageBehaviours {

  "OperatePackagingSitesPage" - {

    beRetrievable[Boolean](OperatePackagingSitesPage)

    beSettable[Boolean](OperatePackagingSitesPage)

    beRemovable[Boolean](OperatePackagingSitesPage)
  }
}
