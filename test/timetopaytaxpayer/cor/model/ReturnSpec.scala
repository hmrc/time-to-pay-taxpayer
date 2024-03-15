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

class ReturnSpec extends AnyWordSpecLike with Matchers {

  "Return" should {

    "have a format instance" in {
      val r = Return(
        LocalDate.of(4122, 12, 25),
        Some(LocalDate.of(2, 11, 17)),
        Some(LocalDate.of(489, 8, 31)),
        Some(LocalDate.of(1932, 6, 1))
      )

      val expectedJson = Json.parse(
        """
          |{
          |  "taxYearEnd":   "4122-12-25",
          |  "issuedDate":   "0002-11-17",
          |  "dueDate":      "0489-08-31",
          |  "receivedDate": "1932-06-01"
          |}
          |""".stripMargin
      )

      Json.toJson(r) shouldBe expectedJson
      expectedJson.validate[Return] shouldBe JsSuccess(r)
    }

  }

}
