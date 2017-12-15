/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.timetopaytaxpayer

import java.time.LocalDate

import uk.gov.hmrc.timetopaytaxpayer.communication.preferences.CommunicationPreferences
import uk.gov.hmrc.timetopaytaxpayer.returns.Returns.Return
import uk.gov.hmrc.timetopaytaxpayer.sa.DesignatoryDetails.{Individual, Name}
import uk.gov.hmrc.timetopaytaxpayer.taxpayer.Address
import cats._
import cats.data._
import cats.implicits._

import util.Random.nextInt

object Fixtures {

  def someAuthorizedUser = AuthorizedUser("desmond.stub")

  def someUtr = Utr(Stream.continually(nextInt(9)).take(10).mkString)

  def uniqueUtrs(n: Int) = Stream.continually(someUtr).distinct.take(n)

  def someCommunicationPreferences() = CommunicationPreferences(welshLanguageIndicator = true, audioIndicator = true,
    largePrintIndicator = true, brailleIndicator = true)

  def somePerson() = Individual(someIndividual(), someAddress())

  def someAddress() = Address("465 Any Road".some, "Cheese".some, "Pie".some, "Apple".some, none, "BN3 2GH".some)

  def someIndividual() = Name("President".some, "Donald".some, none, "Trump")

  def someReturns() = List(
    Return(taxYearEnd = LocalDate.of(2014, 4, 5), receivedDate = Some(LocalDate.of(2014, 11, 28))),
    Return(taxYearEnd = LocalDate.of(2014, 4, 5),
      issuedDate = Some(LocalDate.of(2015, 4, 6)), dueDate = Some(LocalDate.of(2016, 1, 31))),
    Return(taxYearEnd = LocalDate.of(2014, 4, 5), issuedDate = Some(LocalDate.of(2016, 4, 6)),
      dueDate = Some(LocalDate.of(2017, 1, 31)), receivedDate = Some(LocalDate.of(2016, 4, 11)))
  )

}
