package uk.gov.hmrc.timetopayeligibility.taxpayer

import play.api.libs.json.{JsValue, Json, Writes}
import uk.gov.hmrc.play.test.UnitSpec

class TaxPayerJsonSpec extends UnitSpec {

  "an address" should {
    "be serialised to json" in {

      implicit val writeAddress: Writes[Address] = Address.writer

      val address: Address = Address(List("123 Fake Street", "Foo", "Bar", "Baz", "Waz"), "BN3 2GH")
      val json: JsValue = Json.toJson(address)


      val expectedJson = Json.parse(
        """{
          |  "addressLine1": "123 Fake Street",
          |  "addressLine2": "Foo",
          |  "addressLine3": "Bar",
          |  "addressLine4": "Baz",
          |  "addressLine5": "Waz",
          |  "postCode": "BN3 2GH"
          |}""".stripMargin)


      json shouldBe expectedJson
    }

    "serialise to json correctly with fewer line values" in {

      implicit val writeAddress: Writes[Address] = Address.writer

      val address: Address = Address(List("123 Fake Street", "Foo", "Bar"), "BN3 2GH")
      val json: JsValue = Json.toJson(address)


      val expectedJson = Json.parse(
        """{
          |  "addressLine1": "123 Fake Street",
          |  "addressLine2": "Foo",
          |  "addressLine3": "Bar",
          |  "postCode": "BN3 2GH"
          |}""".stripMargin)


      json shouldBe expectedJson
    }
  }


}
