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

package uk.gov.hmrc.timetopayeligibility.debits

import java.time.LocalDate

import play.api.libs.json.{JsPath, Json, Reads}
import uk.gov.hmrc.timetopayeligibility.infrastructure.HmrcEligibilityService._

object Debits {

  type DebitsResult = HmrcEligibilityServiceResult[Seq[Debit]]

  case class Charge(originCode: String, creationDate: LocalDate)

  case class Interest(creationDate: Option[LocalDate], amount: Int)

  case class Debit(taxYearEnd: LocalDate, charge: Charge, relevantDueDate: LocalDate, totalOutstanding: Int, interest: Option[Interest])

  val reader: Reads[Seq[Debit]] = {
    implicit val readCharge: Reads[Charge] = Json.reads[Charge]
    implicit val readInterest: Reads[Interest] = Json.reads[Interest]
    implicit val readReturn: Reads[Debit] = Json.reads[Debit]

    (JsPath \ "debits").read[Seq[Debit]]
  }
}