/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.timetopayeligibility.taxpayer

import java.time.LocalDate

import play.api.libs.json.{JsValue, Json, Writes}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences

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

  "a tax payer" should {
    "be serialised to json" in {

      implicit val writeTaxPayer: Writes[TaxPayer] = TaxPayer.writer

      val prefs = CommunicationPreferences(welshLanguageIndicator = false, audioIndicator = false,
        largePrintIndicator = false, brailleIndicator = false)

      val addresses = List(Address(List("123 Fake Street", "Foo", "Bar"), "BN3 2GH"))

      val debits = List(Debit("IN2", LocalDate.of(2014, 7, 31)))

      val taxPayer = TaxPayer(customerName = "Customer name", addresses = addresses,
        selfAssessment = SelfAssessmentDetails("1234567890", prefs, debits = debits))

      val json: JsValue = Json.toJson(taxPayer)

      val expectedJson = Json.parse("""
          |{
          |   "customerName": "Customer name",
          |   "addresses": [
          |           {
          |             "addressLine1": "123 Fake Street",
          |             "addressLine2": "Foo",
          |             "addressLine3": "Bar",
          |             "postCode": "BN3 2GH"
          |           }
          |         ],
          |    "selfAssessment": {
          |      "utr": "1234567890",
          |      "communicationPreferences": {
          |        "welshLanguageIndicator": false,
          |        "audioIndicator": false,
          |        "largePrintIndicator": false,
          |        "brailleIndicator": false
          |      },
          |      "debits": [
          |        {
          |          "originCode": "IN2",
          |          "dueDate": "2014-07-31"
          |        }
          |      ]
          |}
          }""".stripMargin)

      json shouldBe expectedJson

    }

  }

}
