/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.timetopaytaxpayer.debits

import java.time.LocalDate

import org.scalatest.{Matchers, WordSpecLike}
import play.api.libs.json._
import uk.gov.hmrc.timetopaytaxpayer.debits.Debits.{Charge, Debit, Interest}

class DebitsJsonSpec extends WordSpecLike with Matchers {

  "a json value" should {
    "be parsed to debits" in {
      val json = Json.parse(
        """{
          |  "debits": [
          |    {
          |      "taxYearEnd": "2016-04-05",
          |      "charge": {
          |        "originCode": "POA1",
          |        "creationDate": "2015-11-05"
          |      },
          |      "relevantDueDate": "2015-11-05",
          |      "totalOutstanding": 5000,
          |      "interest": {
          |        "creationDate": "2015-11-05",
          |        "amount": 500
          |      }
          |    }
          |  ]
          |}
          |""".stripMargin)

      Debits.reader.reads(json) match {
        case JsSuccess(debits, _) => debits shouldBe List(
          Debit(taxYearEnd       = LocalDate.of(2016, 4, 5),
                charge           = Charge(originCode   = "POA1", creationDate = LocalDate.of(2015, 11, 5)),
                relevantDueDate  = LocalDate.of(2015, 11, 5),
                totalOutstanding = 5000,
                interest         = Some(Interest(creationDate = Some(LocalDate.of(2015, 11, 5)), amount = 500)))
        )
        case _ => fail("Could not extract debit")
      }
    }

    "be parsed to debits without optional fields set" in {
      val json = Json.parse(
        """{
          |  "debits": [
          |    {
          |      "taxYearEnd": "2016-04-05",
          |      "charge": {
          |        "originCode": "POA1",
          |        "creationDate": "2015-11-05"
          |      },
          |      "relevantDueDate": "2015-11-05",
          |      "totalOutstanding": 5000,
          |      "interest": {
          |        "amount": 500
          |      }
          |    },
          |    {
          |      "taxYearEnd": "2016-04-05",
          |      "charge": {
          |        "originCode": "POA1",
          |        "creationDate": "2015-11-05"
          |      },
          |      "relevantDueDate": "2015-11-05",
          |      "totalOutstanding": 5000
          |    }
          |  ]
          |}
          |""".stripMargin)

      Debits.reader.reads(json) match {
        case JsSuccess(debits, _) => debits shouldBe List(
          Debit(taxYearEnd       = LocalDate.of(2016, 4, 5),
                charge           = Charge(originCode   = "POA1", creationDate = LocalDate.of(2015, 11, 5)),
                relevantDueDate  = LocalDate.of(2015, 11, 5),
                totalOutstanding = 5000,
                interest         = Some(Interest(creationDate = None, amount = 500))),
          Debit(taxYearEnd       = LocalDate.of(2016, 4, 5),
                charge           = Charge(originCode   = "POA1", creationDate = LocalDate.of(2015, 11, 5)),
                relevantDueDate  = LocalDate.of(2015, 11, 5),
                totalOutstanding = 5000,
                interest         = None)
        )
        case _ => fail("Could not extract debit")
      }
    }

    "fails when debits missing in Json" in {
      val json = Json.parse(
        """{
          |  "wine": "cheese"
          |}""".stripMargin)

      Debits.reader.reads(json) match {
        case JsSuccess(_, _) => fail("Should not parse")
        case JsError(errors) => errors.nonEmpty shouldBe true
      }
    }
  }
}
