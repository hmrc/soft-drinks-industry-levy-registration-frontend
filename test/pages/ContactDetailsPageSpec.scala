package pages

import models.ContactDetails
import pages.behaviours.PageBehaviours

class ContactDetailsPageSpec extends PageBehaviours {

  "ContactDetailsPage" - {

    beRetrievable[ContactDetails](ContactDetailsPage)

    beSettable[ContactDetails](ContactDetailsPage)

    beRemovable[ContactDetails](ContactDetailsPage)
  }
}
