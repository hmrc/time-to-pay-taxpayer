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

package uk.gov.hmrc.timetopayeligibility.taxpayer

import java.time.LocalDate
import play.api.libs.json._
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences


case class TaxPayer(customerName: String, addresses: Seq[Address],
                    selfAssessment: SelfAssessmentDetails)

case class SelfAssessmentDetails(utr: String, communicationPreferences: CommunicationPreferences, debits: Seq[Debit])

case class Debit(originCode: String, amount: Double, dueDate: LocalDate, interest: Option[Interest])

case class Interest(calculationDate: Option[LocalDate], amountAccrued: Double)

case class Address(addressLine1: String, addressLine2: String, addressLine3: String,
                   addressLine4: String, addressLine5: String, postcode: String)


object TaxPayer {

  val writer: Writes[TaxPayer] = {
    implicit val writeReturn: Writes[TaxPayer] = TaxPayer.writer
    implicit val writeAddress: Writes[Address] = Json.writes[Address]
    implicit val writePreferences: Writes[CommunicationPreferences] = Json.writes[CommunicationPreferences]
    implicit val writeInterest: Writes[Interest] = Json.writes[Interest]
    implicit val writeDebits: Writes[Debit] = Json.writes[Debit]
    implicit val writeSelfAssessmentDetails: Writes[SelfAssessmentDetails] = Json.writes[SelfAssessmentDetails]

    Json.writes[TaxPayer]
  }
}

