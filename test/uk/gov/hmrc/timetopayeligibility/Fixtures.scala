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

package uk.gov.hmrc.timetopayeligibility

import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences
import uk.gov.hmrc.timetopayeligibility.sa.DesignatoryDetails.{Name, Individual}
import uk.gov.hmrc.timetopayeligibility.taxpayer.Address

import util.Random.nextInt

object Fixtures {

  def someUtr = Utr(Stream.continually(nextInt(9)).take(10).mkString)

  def uniqueUtrs(n: Int) = Stream.continually(someUtr).distinct.take(n)

  def someCommunicationPreferences() = CommunicationPreferences(welshLanguageIndicator = true, audioIndicator = true,
    largePrintIndicator = true, brailleIndicator = true)

  def somePerson() = Individual(someIndividual(), someAddress())

  def someAddress() = Address("465 Any Road", "Cheese", "Pie", "Apple", "Orange", "BN3 2GH")

  def someIndividual() = Name("President", "Donald", None, "Trump")

}
