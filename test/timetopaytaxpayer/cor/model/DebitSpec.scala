/*
 * Copyright 2024 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class DebitSpec extends AnyWordSpecLike with Matchers {

  "Debit" should {

    "have a format instance" in {
      val debit = Debit(
        "origin",
        BigDecimal(100.2),
        LocalDate.of(2073, 2, 4),
        Some(Interest(Some(LocalDate.of(2080, 1, 23)), BigDecimal(0.12))),
        LocalDate.of(2088, 12, 5)
      )

      val expectedJson = Json.parse(
        """
          |{
          |  "originCode": "origin",
          |  "amount": 100.2,
          |  "dueDate": "2073-02-04",
          |  "interest": {
          |    "creationDate": "2080-01-23",
          |    "amount": 0.12
          |  },
          |  "taxYearEnd": "2088-12-05"
          |}
          |""".stripMargin
      )

      Json.toJson(debit) shouldBe expectedJson
      expectedJson.validate[Debit] shouldBe JsSuccess(debit)
    }

  }

}
