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

package timetopaytaxpayer.des.model

import java.time.LocalDate

import play.api.libs.json.{Json, Reads}
import timetopaytaxpayer.cor.model.{Debit, Interest}

final case class DesDebit(
    taxYearEnd:       LocalDate,
    charge:           DesCharge,
    relevantDueDate:          Option[LocalDate],
    totalOutstanding: Double,
    interest:         Option[Interest]
) {

  def asDebit() = Debit(
    originCode = this.charge.originCode,
    amount     = this.totalOutstanding,
    dueDate    = this.relevantDueDate,
    interest   = this.interest,
    taxYearEnd = this.taxYearEnd
  )
}

object DesDebit {
  implicit val reads: Reads[DesDebit] = Json.reads[DesDebit]
}
