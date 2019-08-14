/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.LocalDate

import play.api.libs.json.{Json, OFormat}

final case class Debit(
    originCode: String,
    amount:     Double,
    dueDate:    LocalDate,
    interest:   Option[Interest],
    taxYearEnd: LocalDate
)

object Debit {
  implicit val format: OFormat[Debit] = Json.format[Debit]
}