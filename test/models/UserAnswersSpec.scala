package models

import base.SpecBase
import models.backend.UkAddress

class UserAnswersSpec extends SpecBase {

  val userAnswers = UserAnswers("testing", RegisterState.RegisterWithAuthUTR)
  val ukAddress = UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some("bar"))

  "setAlfResponse" - {
    "should add the alfResponseForLookupState when provided with a ukAddress and addressLookupState" in {

    }
  }

  "addPackagingSite" - {
    "should create a packaging site from the ukAddress" - {
      "and add it to the packagingSites" - {
        "when no packaging sites currently exist" in {

        }

        "when packaging sites currently exist but the sdilRef is different" in {

        }
      }

      "and replace the packaging site that has the same sdilRef" in {

      }
    }
  }

  "addWarehouse" - {
    "should create a warehouse from the ukAddress" - {
      "and add it to the warehouses" - {
        "when no warehouses currently exist" in {

        }

        "when warehouses currently exist but the sdilRef is different" in {

        }
      }

      "and replace the warehouse that has the same sdilRef" in {

      }
    }
  }

  "setBusinessAddress" - {
    "should set the address to the provided business address" - {
      "when no address currently exists" in {

      }
      "when an address already exists" in {

      }
    }
  }

}
