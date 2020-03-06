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

package uk.gov.hmrc.timetopaytaxpayer.debits

import java.time.LocalDate

import org.scalatest.{Matchers, WordSpecLike}
import play.api.libs.json._
import timetopaytaxpayer.cor.model.Interest
import timetopaytaxpayer.des.model.{DesCharge, DesDebit, DesDebits}

class DesDebitsJsonSpec extends WordSpecLike with Matchers {

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
          |""".stripMargin
      )

      val desDebits = DesDebits.reads.reads(json)

      desDebits match {
        case JsSuccess(debits, _) => debits shouldBe DesDebits(List(
          DesDebit(
            taxYearEnd       = LocalDate.of(2016, 4, 5),
            charge           = DesCharge(originCode   = "POA1", creationDate = LocalDate.of(2015, 11, 5)),
            relevantDueDate  = Some(LocalDate.of(2015, 11, 5)),
            totalOutstanding = 5000,
            interest         = Some(Interest(creationDate = Some(LocalDate.of(2015, 11, 5)), amount = 500))
          )
        ))
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
          |""".stripMargin
      )

      DesDebits.reads.reads(json) match {
        case JsSuccess(debits, _) => debits shouldBe DesDebits(List(
          DesDebit(
            taxYearEnd       = LocalDate.of(2016, 4, 5),
            charge           = DesCharge(originCode   = "POA1", creationDate = LocalDate.of(2015, 11, 5)),
            relevantDueDate  = Some(LocalDate.of(2015, 11, 5)),
            totalOutstanding = 5000,
            interest         = Some(Interest(creationDate = None, amount = 500))
          ),
          DesDebit(
            taxYearEnd       = LocalDate.of(2016, 4, 5),
            charge           = DesCharge(originCode   = "POA1", creationDate = LocalDate.of(2015, 11, 5)),
            relevantDueDate  = Some(LocalDate.of(2015, 11, 5)),
            totalOutstanding = 5000,
            interest         = None
          )
        ))
        case _ => fail("Could not extract debit")
      }
    }

    "fails when debits missing in Json" in {
      val json = Json.parse(
        """{
          |  "wine": "cheese"
          |}""".stripMargin
      )

      DesDebits.reads.reads(json) match {
        case JsSuccess(_, _) => fail("Should not parse")
        case JsError(errors) => errors.nonEmpty shouldBe true
      }
    }
  }
}
