/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.timetopaytaxpayer.returns

import play.api.libs.json._
import support.UnitSpec
import timetopaytaxpayer.cor.model._
import timetopaytaxpayer.des.model._

import java.time.LocalDate

class ReturnsJsonSpec extends UnitSpec {

  "a json value" - {
    "be parsed to returns" in {
      val json = Json.parse(
        """{
          |  "returns": [
          |    {
          |      "taxYearEnd": "2014-04-05",
          |      "receivedDate": "2014-11-28"
          |    },
          |    {
          |      "taxYearEnd": "2014-04-06",
          |      "issuedDate": "2016-04-06",
          |      "dueDate": "2017-01-31",
          |      "receivedDate": "2016-04-11"
          |    }
          |  ]
          |}""".stripMargin
      )

      DesReturns.reads.reads(json) match {
        case JsSuccess(returns, _) => returns shouldBe DesReturns(List(
          Return(taxYearEnd   = LocalDate.of(2014, 4, 5), issuedDate = None, dueDate = None, receivedDate = Some(LocalDate.of(2014, 11, 28))),
          Return(taxYearEnd   = LocalDate.of(2014, 4, 6), issuedDate = Some(LocalDate.of(2016, 4, 6)), dueDate = Some(LocalDate.of(2017, 1, 31)), receivedDate = Some(LocalDate.of(2016, 4, 11)))
        ))
        case _ => fail("Could not extract returns")
      }
    }

    "fails when returns missing in Json" in {
      val json = Json.parse(
        """{
          |  "wine": "cheese"
          |}""".stripMargin
      )

      DesReturns.reads.reads(json) match {
        case JsSuccess(returns, _) => fail("Should not parse")
        case JsError(errors)       => errors.nonEmpty shouldBe true
      }
    }
  }
}
