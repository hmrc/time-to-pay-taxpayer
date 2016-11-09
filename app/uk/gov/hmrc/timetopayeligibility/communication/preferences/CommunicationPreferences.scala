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

package uk.gov.hmrc.timetopayeligibility.communication.preferences

import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.timetopayeligibility.infrastructure.HmrcEligibilityService._


case class CommunicationPreferences(welshLanguageIndicator: Boolean, audioIndicator: Boolean,
                                    largePrintIndicator: Boolean, brailleIndicator: Boolean)

object CommunicationPreferences {

  val reader: Reads[CommunicationPreferences] = Json.reads[CommunicationPreferences]

  type CommunicationPreferencesResult = HmrcEligibilityServiceResult[CommunicationPreferences]
}