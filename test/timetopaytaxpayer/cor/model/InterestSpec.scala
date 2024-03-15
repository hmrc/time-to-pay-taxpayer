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

class InterestSpec extends AnyWordSpecLike with Matchers {

  "Interest" should {

    "have a format instance" in {
      val interest = Interest(Some(LocalDate.of(2020, 5, 17)), BigDecimal(1.2345))
      val expectedJson = Json.parse(
        """
          |{
          |  "creationDate": "2020-05-17",
          |  "amount": 1.2345
          |}
          |""".stripMargin
      )

      Json.toJson(interest) shouldBe expectedJson
      expectedJson.validate[Interest] shouldBe JsSuccess(interest)
    }

  }

}
