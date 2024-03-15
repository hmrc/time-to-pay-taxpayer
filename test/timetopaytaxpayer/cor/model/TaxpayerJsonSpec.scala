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

package timetopaytaxpayer.cor.model

import play.api.libs.json.Json.toJson
import play.api.libs.json._
import support.UnitSpec
import timetopaytaxpayer.cor.model._

import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.time.{Clock, LocalDate, LocalDateTime, ZoneId}

// todo - remove as part of OPS-4581
class TaxpayerJsonSpec extends UnitSpec {

  "TaxPayer" - {

    val prefs = CommunicationPreferences(
      welshLanguageIndicator = false, audioIndicator = false, largePrintIndicator = false, brailleIndicator = false
    )

    val addresses = List(Address(Some("123 Fake Street"), Some("Foo"), Some("Bar"), None, None, Some("BN3 2GH")))

    val debits =
      List(
        Debit(
          originCode = "POA2", amount = 250.52, dueDate = LocalDate.of(2016, 1, 31),
          interest   = Some(Interest(Some(LocalDate.of(2016, 6, 1)), 42.32)),
          taxYearEnd = LocalDate.of(2017, 4, 5)
        )
      )

    val saDetails = SelfAssessmentDetails(SaUtr("1234567890"), prefs, debits, Nil)

    val taxPayer = Taxpayer("Customer name", addresses, saDetails)

    "should have a format instance" in {
      val json: JsValue = toJson(taxPayer)

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
          |             "creationDate" : "2016-06-01",
          |             "amount" : 42.32
          |          },
          |          "taxYearEnd": "2017-04-05"
          |        }
          |      ],
          |      "returns" : [ ]
          |}
            }""".stripMargin
      )

      json shouldBe expectedJson
    }

    "have an obfuscate method" in {
      taxPayer.obfuscate shouldBe Taxpayer(
        "xxxxxxxx xxxx",
        List(
          Address(
            Some("123 xxxx xxxxxx"),
            Some("xxx"),
            Some("xxx"),
            None,
            None,
            Some("xx3 2xx")
          )
        ),
        saDetails.copy(utr = SaUtr("1234***"))
      )
    }
  }

}
