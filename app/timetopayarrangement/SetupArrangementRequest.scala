/*
 * Copyright 2023 HM Revenue & Customs
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

package timetopayarrangement

import java.time.LocalDate

import play.api.libs.json.{Json, OFormat}
import timetopaytaxpayer.cor.model.{Debit, SaUtr}

case class SetupArrangementRequest(
    utr:                  SaUtr,
    paymentPlanReference: String,
    directDebitReference: String,
    schedule:             Schedule,
    debits:               List[Debit]
)

object SetupArrangementRequest {
  implicit val format: OFormat[SetupArrangementRequest] = Json.format[SetupArrangementRequest]
}

case class Schedule(
    startDate:            LocalDate,
    endDate:              LocalDate,
    initialPayment:       BigDecimal,
    amountToPay:          BigDecimal,
    instalmentBalance:    BigDecimal,
    totalInterestCharged: BigDecimal,
    totalPayable:         BigDecimal,
    instalments:          List[Instalment]
)

object Schedule {
  implicit val format: OFormat[Schedule] = Json.format[Schedule]

}
case class Instalment(paymentDate: LocalDate, amount: BigDecimal)

object Instalment {
  implicit val format: OFormat[Instalment] = Json.format[Instalment]
}
