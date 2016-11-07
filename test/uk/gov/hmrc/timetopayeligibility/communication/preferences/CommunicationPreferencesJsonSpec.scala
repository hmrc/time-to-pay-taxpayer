package uk.gov.hmrc.timetopayeligibility.communication.preferences

import java.time.LocalDate

import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferencesService.CommunicationPreferences

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

      CommunicationPreferencesJson.reader.reads(json) match {
        case JsSuccess(returns, _) => returns shouldBe CommunicationPreferences(welshLanguageIndicator = true, audioIndicator = false,
                                                                                largePrintIndicator = false, brailleIndicator = false)
        case _ => fail("Could not extract communication preferences")
      }

    }
  }

}
