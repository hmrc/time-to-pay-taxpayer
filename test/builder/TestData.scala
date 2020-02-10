/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.Json
import timetopayarrangement.{Instalment, Schedule}
import timetopayarrangement.des.model.{DesTtpArrangement, LetterAndControl}
import timetopaytaxpayer.cor.model.{Address, CommunicationPreferences, SaUtr, TaxpayerDetails}

object TestData {

  val setupArrangementRequet = Json.parse( //language=json
    s"""
                                             {
                                               "paymentPlanReference": "12345678901234567890123456789012345678900123456",
                                               "directDebitReference": "12345678901234567890123456789012345678900123456",
                                                 "addresses": [
                                                   {
                                                     "addressLine1": "",
                                                     "addressLine2": "",
                                                     "addressLine3": "",
                                                     "addressLine4": "",
                                                     "addressLine5": "",
                                                     "postcode": ""
                                                   }
                                                 ],
                                                 "customerName": "Customer Name",

                                                   "utr": "1234567890",
                                                   "communicationPreferences": {
                                                     "welshLanguageIndicator": false,
                                                     "audioIndicator": false,
                                                     "largePrintIndicator": false,
                                                     "brailleIndicator": false
                                                   },
                                                   "debits": [
                                                     {
                                                       "originCode": "IN2",
                                                       "amount": 4950.00,
                                                       "dueDate": "2004-07-31",
                                                       "taxYearEnd": "2016-08-09"
                                                     }
                                                   ],
                                               "schedule": {
                                                 "startDate": "2016-09-01",
                                                 "endDate": "2017-08-01",
                                                 "initialPayment": 50,
                                                 "amountToPay": 50000000,
                                                 "instalmentBalance": 4950,
                                                 "totalInterestCharged": 45.83,
                                                 "totalPayable": 5045.83,
                                                 "instalments": [
                                                   {
                                                     "paymentDate": "2016-10-01",
                                                     "amount": 1248.95
                                                   },
                                                   {
                                                     "paymentDate": "2016-11-01",
                                                     "amount": 1248.95
                                                   },
                                                   {
                                                     "paymentDate": "2016-12-01",
                                                     "amount": 1248.95
                                                   },
                                                   {
                                                     "paymentDate": "2017-01-01",
                                                     "amount": 1248.95
                                                   }
                                                 ]
                                               }
                                             }"""
  )

  val submitArrangementTTPArrangement: DesTtpArrangement =
    Json.parse(s"""
                  |{
                  |  "startDate": "2016-08-09",
                  |  "endDate": "2016-09-16",
                  |  "firstPaymentDate": "2016-08-09",
                  |  "firstPaymentAmount": "90000.00",
                  |  "regularPaymentAmount": "6000.00",
                  |  "regularPaymentFrequency": "Monthly",
                  |  "reviewDate": "2016-08-09",
                  |  "initials": "ZZZ",
                  |  "enforcementAction": "CCP",
                  |  "directDebit": true,
                  |  "debitDetails": [
                  |    {
                  |      "debitType": "IN2",
                  |      "dueDate": "2004-07-31"
                  |    }
                  |  ],
                  |  "saNote": "SA Note Text Here"
                  |}""".stripMargin).as[DesTtpArrangement]

  val letterAndControl: LetterAndControl =
    Json.parse(s"""{
                  |  "customerName": "Customer Name",
                  |  "salutation": "Dear Sir or Madam",
                  |  "addressLine1": "Plaza 2",
                  |  "addressLine2": "Ironmasters Way",
                  |  "addressLine3": "Telford",
                  |  "addressLine4": "Shropshire",
                  |  "addressLine5": "UK",
                  |  "postCode": "TF3 4NA",
                  |  "totalAll": "50000",
                  |  "clmIndicateInt": "Interest is due",
                  |  "clmPymtString": "1 payment of x.xx then 11 payments of x.xx",
                  |  "officeName1": "office name 1",
                  |  "officeName2": "office name 2",
                  |  "officePostcode": "TF2 8JU",
                  |  "officePhone": "1234567",
                  |  "officeFax": "12345678",
                  |  "officeOpeningHours": "9-5",
                  |  "template": "template",
                  |  "exceptionType": "2",
                  |  "exceptionReason": "Customer requires Large Format printing"
                  |}
                  |""".stripMargin).as[LetterAndControl]

  val taxpayer: TaxpayerDetails =
    Json.parse( //language=json
      s"""{
                    "customerName" : "Customer Name",
                    "addresses": [
                      {
                        "addressLine1": "",
                        "addressLine2": "",
                        "addressLine3": "",
                        "addressLine4": "",
                        "addressLine5": "",
                        "postcode": ""
                      }
                    ],
                      "utr": "1234567890",
                      "communicationPreferences": {
                        "welshLanguageIndicator": false,
                        "audioIndicator": false,
                        "largePrintIndicator": false,
                        "brailleIndicator": false
                      },
                      "debits": [
                        {
                          "originCode": "IN2",
                          "dueDate": "2004-07-31"
                        }
                      ]
                    }
                  """.stripMargin
    ).as[TaxpayerDetails]

  val schedule: Schedule = Schedule(LocalDate.now(), LocalDate.now(), 0.0, BigDecimal("2000.00"), 0.0, 0.0, 0.0, List(Instalment(LocalDate.now(), 0.0)))
  val happyCommsPref = CommunicationPreferences(welshLanguageIndicator = false, audioIndicator = false, largePrintIndicator = false, brailleIndicator = false)
  val welshAndLargePrintCommsPref = CommunicationPreferences(welshLanguageIndicator = true, audioIndicator = false, largePrintIndicator = true, brailleIndicator = false)

  object Addresses {
    val englishAddress1 = Address(addressLine1 = Some("XXX"), postcode = Some("B45 0HY"))
    val englishAddress2 = Address(addressLine1 = Some("XXX"), postcode = Some("B97 5HZ"))
    val welshAddress = Address(addressLine1 = Some("XXX"), postcode = Some("CF23 8PF"))
    val northernIrelandAddress = Address(addressLine1 = Some("XXX"), postcode = Some("BT52 2PP"))
    val scottishAddress = Address(addressLine1 = Some("XXX"), postcode = Some("G3 8NW"))
    val foreignAddress = Address(addressLine1 = Some("XXX"), postcode = Some("400089"))
    val englishAddressMissingPostCodeAndLine1 = Address(addressLine1 = Some(""), postcode = Some(""))
    val englishAddressMissingPostCode = Address(addressLine1 = Some("XXXX"), postcode = Some(""))
    val englishAddressMissingLine1 = Address(addressLine1 = Some(""), postcode = Some("XXXX"))
    val scottishAddress1 = Address(addressLine1 = Some("XXX"), addressLine2 = Some("XXX"), addressLine3 = Some("XXX"), addressLine4 = Some("XXXX"), addressLine5 = Some("XXXX"), postcode = Some("G3 8NW"))
    val scottishAddress2 = Address(addressLine1 = Some("XXX"), addressLine2 = Some("XXX"), addressLine3 = Some("XXX"), addressLine4 = Some("XXXX"), addressLine5 = Some("XXXX"), postcode = Some("EH14 8NW"))
    val welshAddress1 = Address(addressLine1 = Some("XXX"), addressLine2 = Some("XXX"), addressLine3 = Some("XXX"), addressLine4 = Some("XXXX"), addressLine5 = Some("XXXX"), postcode = Some("LL57 3DL"))
    val welshAddress2 = Address(addressLine1 = Some("XXX"), addressLine2 = Some("XXX"), addressLine3 = Some("XXX"), addressLine4 = Some("XXXX"), addressLine5 = Some("XXXX"), postcode = Some("SY23 3YA"))
  }

  object Taxpayers {

    import Addresses._

    val taxPayerWithEnglishAddressWithNoComsPref = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(englishAddress1), happyCommsPref)
    val taxPayerWithScottishAddress = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(scottishAddress), happyCommsPref)
    val taxPayerWithEnglishAddress = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(englishAddress1), happyCommsPref)
    val taxPayerWithWelshAddress = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(welshAddress), happyCommsPref)
    val taxPayerWithNorthernIrelandAddress = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(northernIrelandAddress), happyCommsPref)
    val taxPayerWithMissingPostcodeAndLine1 = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(englishAddressMissingPostCodeAndLine1), happyCommsPref)
    val taxPayerWithMissingLine1 = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(englishAddressMissingLine1), happyCommsPref)
    val taxPayerWithMissingPostcode = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(englishAddressMissingPostCode), happyCommsPref)
    val taxPayerWithMultipleEnglishAddresses = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(englishAddress1, englishAddress2), happyCommsPref)
    val taxPayerWithEnglishAndScottishAddresses = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(englishAddress1, scottishAddress), happyCommsPref)
    val taxPayerWithEnglishAndForeignAddresses = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(englishAddress1, foreignAddress), happyCommsPref)
    val taxPayerWithScottishAndForeignAddresses = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(scottishAddress, foreignAddress), happyCommsPref)
    val taxPayerWithEnglishScottishAndForeignAddresses = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(englishAddress1, scottishAddress, foreignAddress), happyCommsPref)
    val taxPayerWithNoAddress = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(), happyCommsPref)
    val taxPayerWithLargePrintAndWelsh = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(englishAddress1), welshAndLargePrintCommsPref)
    val taxPayerWithMultipleWelshAddresses = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(welshAddress1, welshAddress2), happyCommsPref)
    val taxPayerWithMultipleScottishAddresses = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(scottishAddress1, scottishAddress2), happyCommsPref)
    val taxPayerWithMultipleJurisdictions = TaxpayerDetails(SaUtr("XXX"), "CustomerName", List(welshAddress, scottishAddress), happyCommsPref)

  }
}
