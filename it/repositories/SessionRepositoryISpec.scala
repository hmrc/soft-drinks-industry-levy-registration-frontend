package repositories

import models.alf.AddressResponseForLookupState
import models.backend.{Site, UkAddress}
import models.{RegisterState, UserAnswers, Warehouse}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{Format, JsObject, Json, Reads}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import services.AddressLookupState.PackingDetails
import services.Encryption
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.crypto.json.CryptoFormats

import java.time.{Instant, LocalDate}
import java.util.concurrent.TimeUnit
import org.mongodb.scala.{SingleObservableFuture , ObservableFuture}

class SessionRepositoryISpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with OptionValues with GuiceOneAppPerSuite with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach {

  val repository: SessionRepository = app.injector.instanceOf[SessionRepository]
  val encryption: Encryption = app.injector.instanceOf[Encryption]
  implicit val cryptEncryptedValueFormats: Format[EncryptedValue]  = CryptoFormats.encryptedValueFormat
  val alfAddress = UkAddress(List("foo"), "TF1 3XX", None)


  override def beforeEach(): Unit = {
    await(repository.collection.deleteMany(BsonDocument()).toFuture())
    super.beforeEach()
  }

  "indexes" - {
    "are correct" in {
      repository.indexes.toList.toString() mustBe List(IndexModel(
        Indexes.ascending("lastUpdated"),
        IndexOptions()
          .name("lastUpdatedIdx")
          .expireAfter(900, TimeUnit.SECONDS)
      )).toString()
    }
  }

  ".set" - {
    "must set the last updated time on the supplied user answers to `now`, and save them" in {
      val alfId: String = "bar"
      val userAnswersBefore = UserAnswers(
        "id", RegisterState.RegisterWithAuthUTR, Json.obj("foo" -> "bar"),
        Some(UkAddress(List("line 1", "line 2", "line 3", "line 4"), "aa1 1aa", alfId = Some(alfId))),
        lastUpdated = Instant.ofEpochSecond(1)
      )
      val timeBeforeTest = Instant.now()
      val setResult     = await(repository.set(userAnswersBefore))
      val updatedRecord = await(repository.get(userAnswersBefore.id)).get
      lazy val timeAfterTest = Instant.now()

      setResult mustEqual true
      assert(updatedRecord.lastUpdated.toEpochMilli > timeBeforeTest.toEpochMilli || updatedRecord.lastUpdated.toEpochMilli == timeBeforeTest.toEpochMilli)
      assert(updatedRecord.lastUpdated.toEpochMilli < timeAfterTest.toEpochMilli || updatedRecord.lastUpdated.toEpochMilli == timeAfterTest.toEpochMilli)

      updatedRecord.id mustBe userAnswersBefore.id
      updatedRecord.submittedOn mustBe userAnswersBefore.submittedOn
      updatedRecord.data mustBe userAnswersBefore.data
      updatedRecord.address mustBe userAnswersBefore.address
      updatedRecord.warehouseList mustBe userAnswersBefore.warehouseList
      updatedRecord.packagingSiteList mustBe userAnswersBefore.packagingSiteList
    }

    "correctly encrypt the records data" in {
      val alfId: String = "bar"
      val userAnswersBefore = UserAnswers("id", RegisterState.RegisterWithAuthUTR,
        Json.obj("foo" -> "bar"),
        Some(UkAddress(List("Line 1", "Line 2", "Line 3", "Line 4"),"aa1 1aa", alfId = Some(alfId))),
        Map("foo" -> Site(UkAddress(List("foo"),"foo", Some("foo")),Some("foo"), "foo",Some(LocalDate.now()))),
        Map("foo" -> Warehouse("foo",UkAddress(List("foo"),"foo", Some("foo")))),
        Some(AddressResponseForLookupState(alfAddress, PackingDetails, "123456")),
        None,
        Instant.ofEpochSecond(1))
      val setResult = await(repository.set(userAnswersBefore))
      setResult mustBe true
      val updatedRecord = await(repository.collection.find[BsonDocument](BsonDocument()).toFuture()).head
      val resultParsedToJson = Json.parse(updatedRecord.toJson).as[JsObject]
      val dataDecrypted = {
        Json.parse(encryption.crypto.decrypt((resultParsedToJson \ "data").as[EncryptedValue],userAnswersBefore.id)).as[JsObject]
      }
      val businessAddressDecrypted = {
        Json.fromJson[Option[UkAddress]](Json.parse(encryption.crypto.decrypt((resultParsedToJson \ "address").as[EncryptedValue],userAnswersBefore.id)))(Reads.optionWithNull[UkAddress]).get
      }
      val packagingSiteListDecrypted = {
        val json = (resultParsedToJson \ "packagingSiteList").as[Map[String, EncryptedValue]]
        json.map(site => site._1 -> Json.parse(encryption.crypto.decrypt(site._2, userAnswersBefore.id)).as[Site])
      }
      val warehouseListDecrypted = {
        val json = (resultParsedToJson \ "warehouseList").as[Map[String, EncryptedValue]]
        json.map(warehouse => warehouse._1 -> Json.parse(encryption.crypto.decrypt(warehouse._2, userAnswersBefore.id)).as[Warehouse])
      }

      val addressResponseForLookupStateDecrypted = {
        val json = (resultParsedToJson \ "alfResponseForLookupState").as[EncryptedValue]
        Json.parse(encryption.crypto.decrypt(json, userAnswersBefore.id)).as[AddressResponseForLookupState]
      }

      dataDecrypted mustBe userAnswersBefore.data
      businessAddressDecrypted mustBe userAnswersBefore.address
      packagingSiteListDecrypted mustBe userAnswersBefore.packagingSiteList
      warehouseListDecrypted mustBe userAnswersBefore.warehouseList
      addressResponseForLookupStateDecrypted mustBe userAnswersBefore.alfResponseForLookupState.get
      (resultParsedToJson \ "submittedOn").get.asOpt[Instant] mustBe userAnswersBefore.submittedOn
    }
  }

  ".get" - {

    "when there is a record for this id" - {

      "must update the lastUpdated time and get the record" in {
        val alfId: String = "bar"
        val userAnswersBefore = UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("foo" -> "bar"),
          Some(UkAddress(List("Line 1", "Line 2", "Line 3", "Line 4"),"aa1 1aa", alfId = Some(alfId))),
          lastUpdated = Instant.ofEpochSecond(1)
        )
        await(repository.set(userAnswersBefore))

        val timeBeforeTest = Instant.now()
        val updatedRecord = await(repository.get(userAnswersBefore.id)).get
        lazy val timeAfterTest = Instant.now()

        assert(updatedRecord.lastUpdated.toEpochMilli > timeBeforeTest.toEpochMilli || updatedRecord.lastUpdated.toEpochMilli == timeBeforeTest.toEpochMilli)
        assert(updatedRecord.lastUpdated.toEpochMilli < timeAfterTest.toEpochMilli || updatedRecord.lastUpdated.toEpochMilli == timeAfterTest.toEpochMilli)

        updatedRecord.id mustBe userAnswersBefore.id
        updatedRecord.submittedOn mustBe userAnswersBefore.submittedOn
        updatedRecord.data mustBe userAnswersBefore.data
        updatedRecord.address mustBe userAnswersBefore.address
        updatedRecord.warehouseList mustBe userAnswersBefore.warehouseList
        updatedRecord.packagingSiteList mustBe userAnswersBefore.packagingSiteList
      }
    }

    "when there is no record for this id" - {

      "must return None" in {

        repository.get("id that does not exist").futureValue must not be defined
      }
    }
  }

  ".clear" - {

    "must remove a record" in {
      val alfId: String = "bar"
      val userAnswersBefore = UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("foo" -> "bar"),
        Some(UkAddress(List("Line 1", "Line 2", "Line 3", "Line 4"),"aa1 1aa", alfId = Some(alfId))),
        lastUpdated = Instant.ofEpochSecond(1)
      )
      repository.set(userAnswersBefore).futureValue

      val result = repository.clear(userAnswersBefore.id).futureValue

      result mustEqual true
      repository.get(userAnswersBefore.id).futureValue must not be defined
    }

    "must return true when there is no record to remove" in {
      val result = repository.clear("id that does not exist").futureValue

      result mustEqual true
    }
  }

  ".keepAlive" - {

    "when there is a record for this id" - {

      "must update its lastUpdated to `now` and return true" in {
        val alfId: String = "bar"
        val userAnswersBefore = UserAnswers("id", RegisterState.RegisterWithAuthUTR, Json.obj("foo" -> "bar"),
          Some(UkAddress(List("Line 1", "Line 2", "Line 3", "Line 4"),"aa1 1aa", alfId = Some(alfId))),
          lastUpdated = Instant.ofEpochSecond(1)
        )
        await(repository.set(userAnswersBefore))
        val timeBeforeTest = Instant.now()
        val result = await(repository.keepAlive(userAnswersBefore.id))
        lazy val timeAfterTest = Instant.now()
        result mustEqual true
        val updatedRecord = await(repository.collection.find(BsonDocument()).headOption()).get

        assert(updatedRecord.lastUpdated.toEpochMilli > timeBeforeTest.toEpochMilli || updatedRecord.lastUpdated.toEpochMilli == timeBeforeTest.toEpochMilli)
        assert(updatedRecord.lastUpdated.toEpochMilli < timeAfterTest.toEpochMilli || updatedRecord.lastUpdated.toEpochMilli == timeAfterTest.toEpochMilli)

        updatedRecord.id mustBe userAnswersBefore.id
        updatedRecord.submittedOn mustBe userAnswersBefore.submittedOn
        updatedRecord.data mustBe userAnswersBefore.data
        updatedRecord.address mustBe userAnswersBefore.address
        updatedRecord.warehouseList mustBe userAnswersBefore.warehouseList
        updatedRecord.packagingSiteList mustBe userAnswersBefore.packagingSiteList
      }
    }

    "when there is no record for this id" - {

      "must return true" in {

        await(repository.keepAlive("id that does not exist")) mustEqual true
      }
    }
  }
}
