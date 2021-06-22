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

package builder

import java.time.LocalDate

import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.libs.json.Json
import support.UnitSpec
import timetopayarrangement.builder.LetterAndControlBuilder
import timetopayarrangement.des.model.{DesTtpArrangement, LetterAndControl}
import timetopayarrangement.{Instalment, Schedule, SetupArrangementRequest}
import timetopaytaxpayer.cor.model.{Address, CommunicationPreferences, SaUtr, TaxpayerDetails}

class LetterAndControlBuilderSpec extends UnitSpec {
  import TestData.Taxpayers._
  import TestData._

  val taxPayerData = Table(
    ("taxPayer", "exceptionCode", "exceptionReason", "message"),
    (taxPayerWithEnglishAddress, None, None, "1 English Address"),
    (taxPayerWithEnglishAddressWithNoComsPref, None, None, "1 English Address and no comms preference"),
    (taxPayerWithWelshAddress, None, None, "1 Welsh Address"),
    (taxPayerWithNorthernIrelandAddress, None, None, "1 Northern Ireland Address"),
    (taxPayerWithMissingPostcodeAndLine1, Some("9"), Some("incomplete address"), "Missing address line 1 and postcode"),
    (taxPayerWithMissingPostcode, Some("9"), Some("incomplete address"), "missing line 1"),
    (taxPayerWithMissingLine1, Some("9"), Some("incomplete address"), "missing postcode"),
    (taxPayerWithMultipleEnglishAddresses, None, None, "multiple English addresses"),
    (taxPayerWithEnglishAndScottishAddresses, Some("1"), Some("address jurisdiction conflict"), "an English and Scottish address"),
    (taxPayerWithEnglishAndForeignAddresses, None, None, "an English and Foreign address"),
    (taxPayerWithScottishAndForeignAddresses, Some("1"), Some("address jurisdiction conflict"), "a Scottish and Foreign address"),
    (taxPayerWithEnglishScottishAndForeignAddresses, Some("1"), Some("address jurisdiction conflict"), "an English, Scottish and Foreign address"),
    (taxPayerWithNoAddress, Some("8"), Some("no address"), "no address"),
    (taxPayerWithLargePrintAndWelsh, Some("5"), Some("welsh large print required"), "Welsh Language and Large Print")
  )

  forAll(taxPayerData) { (taxpayer, exceptionCode, exceptionReason, message) =>
    s"return (exceptionCode = $exceptionCode and exceptionReason = $exceptionReason) for $message" in {

      val setupArrangementRequest = SetupArrangementRequest(
        utr                  = SaUtr("XXX"),
        paymentPlanReference = "XXX",
        directDebitReference = "XXX",
        debits               = List(),
        schedule             = schedule
      )
      val result = LetterAndControlBuilder.create(setupArrangementRequest, taxpayer)

      result.customerName shouldBe taxpayer.customerName
      result.salutation shouldBe s"Dear ${taxpayer.customerName}"
      result.exceptionType shouldBe exceptionCode
      result.exceptionReason shouldBe exceptionReason
    }
  }

  "Format the clmPymtString correctly" in {
    val scheduleWithInstalments: Schedule = Schedule(LocalDate.now(), LocalDate.now(), 0.0, BigDecimal("100.98"), 100, 0.98, 100.98,
                                                     List(
        Instalment(LocalDate.now(), 10.0),
        Instalment(LocalDate.now(), 10.0),
        Instalment(LocalDate.now(), 10.0),
        Instalment(LocalDate.now(), 10.0),
        Instalment(LocalDate.now(), 10.0),
        Instalment(LocalDate.now(), 10.0),
        Instalment(LocalDate.now(), 10.0),
        Instalment(LocalDate.now(), 10.0),
        Instalment(LocalDate.now(), 10.0),
        Instalment(LocalDate.now(), 10.98)
      ))
    val result = LetterAndControlBuilder.create(
      SetupArrangementRequest(SaUtr("XXX"), "XXX", "XXX", scheduleWithInstalments, List()),
      taxpayer
    )
    result.clmPymtString shouldBe "Initial payment of £10.00 then 8 payments of £10.00 and final payment of £10.98"
    result.totalAll shouldBe "100.98"
  }
  "Format the clmPymtString correctly for large numbers in" in {
    val scheduleWithInstalments: Schedule = Schedule(LocalDate.now(), LocalDate.now(), 5000000.0, BigDecimal("15000000.00"), 100, 0.00, 100.98,
                                                     List(
        Instalment(LocalDate.now(), 100000000.00),
        Instalment(LocalDate.now(), 100000000.00),
        Instalment(LocalDate.now(), 100000000.00)
      ))
    val result = LetterAndControlBuilder.create(
      SetupArrangementRequest(SaUtr("XXX"), "XXX", "XXX", scheduleWithInstalments, List()),
      taxpayer
    )
    result.clmPymtString shouldBe "Initial payment of £105,000,000.00 then 1 payments of £100,000,000.00 and final payment of £100,000,000.00"
  }

}

