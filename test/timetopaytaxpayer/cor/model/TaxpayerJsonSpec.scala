/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.time.{Clock, LocalDate, LocalDateTime, ZoneId}

import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json.toJson
import play.api.libs.json._

// todo - remove as part of OPS-4581
class TaxpayerJsonSpec extends WordSpec with Matchers {
  "a tax payer" should {
    "be serialised to json" in {
      val prefs = CommunicationPreferences(
        welshLanguageIndicator = false, audioIndicator = false, largePrintIndicator = false, brailleIndicator = false
      )

      val addresses = List(Address(Some("123 Fake Street"), Some("Foo"), Some("Bar"), None, None, Some("BN3 2GH")))

      val debits =
        List(
          Debit(
            originCode = "POA2", amount = 250.52,
            dueDate    = Some(LocalDate.of(2016, 1, 31)),
            interest   = Some(Interest(Some(LocalDate.of(2016, 6, 1)), 42.32)),
            taxYearEnd = LocalDate.of(2017, 4, 5)
          )
        )

      val taxPayer =
        Taxpayer(
          customerName   = "Customer name",
          addresses      = addresses,
          selfAssessment = SelfAssessmentDetails(SaUtr("1234567890"), prefs, debits, Nil)
        )

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
          |}""".stripMargin
      )

      format.reads(addressJson) match {
        case JsSuccess(_: Address, _) => "happy days"
        case JsError(e)               => fail(s"Json does not parse: $e")
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
          |}""".stripMargin
      )

      format.reads(addressJson) match {
        case JsSuccess(_: Address, _) => "happy days"
        case JsError(e)               => fail(s"Json does not parse: $e")
      }
    }

  }

  "fix returns" in {
      implicit def stringToDate(s: String): LocalDate = LocalDate.parse(s)

      implicit def stringToDateO(s: String): Option[LocalDate] = Some(LocalDate.parse(s))

    val saBeforeFix = SelfAssessmentDetails(
      utr                      = SaUtr("1234567890"),
      communicationPreferences =
        CommunicationPreferences(
          welshLanguageIndicator = false, audioIndicator = false, largePrintIndicator = false, brailleIndicator = false
        ),
      debits                   = List(
        Debit(
          originCode = "POA2",
          amount     = 250.52,
          dueDate    = Some(LocalDate.parse("2016-01-31")),
          interest   = Some(Interest(Some(LocalDate.parse("2016-06-01")), 42.32)),
          taxYearEnd = LocalDate.parse("2017-04-05")
        )
      ),
      returns                  = List(
        Return(
          taxYearEnd   = "2000-04-05",
          issuedDate   = "2001-01-23",
          dueDate      = "2001-04-30",
          receivedDate = "2001-04-10"
        ),
        Return(
          taxYearEnd   = "2001-04-10",
          issuedDate   = "2001-04-06",
          dueDate      = "2002-01-31",
          receivedDate = "2001-06-19"
        ),
        Return(
          taxYearEnd   = "2002-04-05",
          issuedDate   = "2002-04-06",
          dueDate      = "2003-01-31",
          receivedDate = "2002-05-08"
        ),
        Return(
          taxYearEnd   = "2003-04-05",
          issuedDate   = "2003-04-06",
          dueDate      = "2004-01-31",
          receivedDate = "2003-07-04"
        ),
        Return(
          taxYearEnd   = "2004-04-05",
          issuedDate   = "2004-04-06",
          dueDate      = "2005-01-31",
          receivedDate = "2004-08-23"
        ),
        Return(
          taxYearEnd   = "2005-04-05",
          issuedDate   = None,
          dueDate      = None,
          receivedDate = None
        )
      )
    )

    val saAfterFix = SelfAssessmentDetails(
      utr                      = SaUtr("1234567890"),
      communicationPreferences =
        CommunicationPreferences(
          welshLanguageIndicator = false, audioIndicator = false, largePrintIndicator = false, brailleIndicator = false
        ),
      debits                   = List(
        Debit(
          originCode = "POA2",
          amount     = 250.52,
          dueDate    = Some(LocalDate.parse("2016-01-31")),
          interest   = Some(Interest(Some(LocalDate.parse("2016-06-01")), 42.32)),
          taxYearEnd = LocalDate.parse("2017-04-05")
        )
      ),
      returns                  = List(
        Return(
          taxYearEnd   = "2001-04-10",
          issuedDate   = "2001-04-06",
          dueDate      = "2002-01-31",
          receivedDate = "2001-06-19"
        ),
        Return(
          taxYearEnd   = "2002-04-05",
          issuedDate   = "2002-04-06",
          dueDate      = "2003-01-31",
          receivedDate = "2002-05-08"
        ),
        Return(
          taxYearEnd   = "2003-04-05",
          issuedDate   = "2003-04-06",
          dueDate      = "2004-01-31",
          receivedDate = "2003-07-04"
        ),
        Return(
          taxYearEnd   = "2004-04-05",
          issuedDate   = "2004-04-06",
          dueDate      = "2005-01-31",
          receivedDate = "2004-08-23"
        ),
        Return(
          taxYearEnd   = "2005-04-05",
          issuedDate   = None,
          dueDate      = None,
          receivedDate = None
        )
      )
    )

    implicit val clock: Clock =
      Clock.fixed(LocalDateTime
        .parse("2006-01-22T16:28:55.185", ISO_DATE_TIME)
        .atZone(ZoneId.of("Europe/London"))
        .toInstant, ZoneId.of("UTC"))

    saBeforeFix.fixReturns shouldBe saAfterFix
  }
}
