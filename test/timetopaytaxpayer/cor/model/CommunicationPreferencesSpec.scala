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

class CommunicationPreferencesSpec extends AnyWordSpecLike with Matchers {

  "CommunicationPreferences" should {

    "have a format instance" in {
      val communicationPreferences = CommunicationPreferences(true, false, true, false)
      val expectedJson = Json.parse(
        """
          |{
          |  "welshLanguageIndicator": true,
          |  "audioIndicator":         false,
          |  "largePrintIndicator":    true,
          |  "brailleIndicator":       false
          |}
          |""".stripMargin
      )

      Json.toJson(communicationPreferences) shouldBe expectedJson
      expectedJson.validate[CommunicationPreferences] shouldBe JsSuccess(communicationPreferences)
    }

  }

}
