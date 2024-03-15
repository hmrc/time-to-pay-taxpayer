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

package timetopaytaxpayer.cor.model

import java.time.Clock
import java.time.LocalDate.now

import play.api.libs.json.{Json, OFormat}

// todo - remove as part of OPS-4581
final case class SelfAssessmentDetails(
    utr:                      SaUtr,
    communicationPreferences: CommunicationPreferences,
    debits:                   Seq[Debit],
    returns:                  Seq[Return]
) {
  /**
   * Removes returns older than 5 years.
   */
  def fixReturns(implicit clock: Clock): SelfAssessmentDetails =
    copy(returns = returns.filter(_.taxYearEnd.isAfter(now(clock).minusYears(5))))

  def obfuscate: SelfAssessmentDetails =
    copy(utr = utr.obfuscate)
}

object SelfAssessmentDetails {
  implicit val format: OFormat[SelfAssessmentDetails] = Json.format[SelfAssessmentDetails]
}
