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

class TaxpayerDetailsSpec extends AnyWordSpecLike with Matchers {

  "TaxpayerDetails" should {

    val taxpayerDetails = TaxpayerDetails(
      SaUtr("1234567890"),
      "customer name",
      Seq.empty,
      CommunicationPreferences(false, true, false, true)
    )

    "have an obfuscate method" in {
      taxpayerDetails.obfuscate shouldBe taxpayerDetails.copy(
        utr          = SaUtr("1234***"),
        customerName = "xxxxxxxx xxxx"
      )
    }

    "have a format instance" in {
      val expectedJson = Json.parse(
        """
          |{
          |  "utr" : "1234567890",
          |  "customerName" : "customer name",
          |  "addresses" : [ ],
          |  "communicationPreferences" : {
          |    "welshLanguageIndicator" : false,
          |    "audioIndicator" : true,
          |    "largePrintIndicator" : false,
          |    "brailleIndicator" : true
          |  }
          |}
          |""".stripMargin
      )

      Json.toJson(taxpayerDetails) shouldBe expectedJson
      expectedJson.validate[TaxpayerDetails] shouldBe JsSuccess(taxpayerDetails)
    }

  }

}
