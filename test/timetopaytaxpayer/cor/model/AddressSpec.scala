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

class AddressSpec extends AnyWordSpecLike with Matchers {

  "Address" should {

    val address = Address(
      Some("line-1"),
      Some("line-2"),
      Some("line-3"),
      Some("line-4"),
      Some("line-5"),
      Some("ABC 1234")
    )

    "have a format instance" in {
      val expectedJson = Json.parse(
        """
          |{
          |  "addressLine1": "line-1",
          |  "addressLine2": "line-2",
          |  "addressLine3": "line-3",
          |  "addressLine4": "line-4",
          |  "addressLine5": "line-5",
          |  "postcode": "ABC 1234"
          |}
          |""".stripMargin
      )

      Json.toJson(address) shouldBe expectedJson
      expectedJson.validate[Address] shouldBe JsSuccess(address)
    }

    "have an obfuscate method" in {
      address.obfuscate shouldBe Address(
        Some("xxxx-1"),
        Some("xxxx-2"),
        Some("xxxx-3"),
        Some("xxxx-4"),
        Some("xxxx-5"),
        Some("xxx 1234")
      )
    }

  }

}
