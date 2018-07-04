/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.timetopaytaxpayer.taxpayer

import java.time.LocalDate

import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.timetopaytaxpayer.communication.preferences.CommunicationPreferences
import cats._
import cats.data._
import cats.implicits._

class TaxPayerJsonSpec extends UnitSpec {

  "a tax payer" should {
    "be serialised to json" in {

      implicit val writeTaxPayer: Writes[TaxPayer] = TaxPayer.writer

      val prefs = CommunicationPreferences(welshLanguageIndicator = false, audioIndicator = false,
        largePrintIndicator = false, brailleIndicator = false)

      val addresses = List(Address(
        "123 Fake Street".some,
        "Foo".some,
        "Bar".some,
        none,
        none,
        "BN3 2GH".some
      ))

      val debits = List(Debit(originCode = "POA2", amount = 250.52, dueDate = LocalDate.of(2016, 1, 31),
        interest = Some(Interest(Some(LocalDate.of(2016, 6, 1)), 42.32)), taxYearEnd = LocalDate.of(2017, 4, 5)))

      val taxPayer = TaxPayer(customerName = "Customer name", addresses = addresses,
        selfAssessment = SelfAssessmentDetails("1234567890", prefs, debits, Nil))

      val json: JsValue = Json.toJson(taxPayer)

      val expectedJson = Json.parse(
        """
          |{
          |   "customerName": "Customer name",
          |   "addresses": [
          |           {
          |             "addressLine1": "123 Fake Street",
          |             "addressLine2": "Foo",
          |             "addressLine3": "Bar",
          |             "postcode": "BN3 2GH"
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
          |          "originCode": "POA2",
          |          "amount": 250.52,
          |          "dueDate": "2016-01-31",
          |          "interest": {
          |             "calculationDate" : "2016-06-01",
          |             "amountAccrued" : 42.32
          |          },
          |          "taxYearEnd": "2017-04-05"
          |        }
          |      ],
          |      "returns" : [ ]
          |}
          }""".stripMargin)

      json shouldBe expectedJson

    }
  }

  "address" should {

    "parse json" in {
      implicit val format: Format[Address] = Json.format[Address]

      val addressJson = Json.parse(
        """
          |{
          |   "addressLine1": "123 Fake Street",
          |   "addressLine2": "Foo",
          |   "addressLine3": "Bar",
          |   "addressLine4": "Bar",
          |   "addressLine5": "Bar",
          |   "postcode": "BN3 2GH"
          |}""".stripMargin)

      format.reads(addressJson) match {
        case JsSuccess(address: Address, _) => "happy days"
        case JsError(e) => fail(s"Json does not parse: $e")
      }
    }

    "parse json with missing lines" in {
      implicit val format: Format[Address] = Json.format[Address]

      val addressJson = Json.parse(
        """
          |{
          |   "addressLine1": "123 Fake Street",
          |   "addressLine2": "Foo",
          |   "addressLine3": "Bar",
          |   "postcode": "BN3 2GH"
          |}""".stripMargin)

      format.reads(addressJson) match {
        case JsSuccess(address: Address, _) => "happy days"
        case JsError(e) => fail(s"Json does not parse: $e")
      }
    }

  }

}
