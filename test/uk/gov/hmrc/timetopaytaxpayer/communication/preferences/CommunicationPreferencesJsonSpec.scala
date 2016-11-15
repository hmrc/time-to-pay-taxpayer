/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.timetopaytaxpayer.communication.preferences

import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

class CommunicationPreferencesJsonSpec extends UnitSpec {

  "a json value" should {

    "be parsed to communication preferences" in {

      val json = Json.parse(
        """{
          |  "welshLanguageIndicator": true,
          |  "audioIndicator": false,
          |  "largePrintIndicator": false,
          |  "brailleIndicator": false
          |}""".stripMargin)

      CommunicationPreferences.reader.reads(json) match {
        case JsSuccess(returns, _) => returns shouldBe CommunicationPreferences(welshLanguageIndicator = true, audioIndicator = false,
                                                                                largePrintIndicator = false, brailleIndicator = false)
        case _ => fail("Could not extract communication preferences")
      }
    }
  }
}
