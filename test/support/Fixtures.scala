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

package support

import java.time.LocalDate

import timetopaytaxpayer.cor.model._
import timetopaytaxpayer.sa.Sa.{Individual, Name}

object Fixtures {

  def someAuthorizedUser = AuthorizedUser("desmond.stub")

  val random = new scala.util.Random
  val start = 111111111
  val end = 999999998

  def someUtr = Utr(s"1${start + random.nextInt((end - start) + 1)}")

  def uniqueUtrs(n: Int) = Stream.continually(someUtr).distinct.take(n)

  def someCommunicationPreferences() = CommunicationPreferences(welshLanguageIndicator = true, audioIndicator = true,
                                                                largePrintIndicator    = true, brailleIndicator = true)

  def somePerson() = Individual(someIndividual(), someAddress())

  def someAddress() = Address(Some("465 Any Road"), Some("Cheese"), Some("Pie"), Some("Apple"), None, Some("BN3 2GH"))

  def someIndividual() = Name(Some("President"), Some("Donald"), None, "Trump")

  def someReturns() = List(
    Return(taxYearEnd   = LocalDate.of(2014, 4, 5), receivedDate = Some(LocalDate.of(2014, 11, 28))),
    Return(
      taxYearEnd = LocalDate.of(2014, 4, 5),
      issuedDate = Some(LocalDate.of(2015, 4, 6)), dueDate = Some(LocalDate.of(2016, 1, 31))
    ),
    Return(taxYearEnd   = LocalDate.of(2014, 4, 5), issuedDate = Some(LocalDate.of(2016, 4, 6)),
           dueDate      = Some(LocalDate.of(2017, 1, 31)), receivedDate = Some(LocalDate.of(2016, 4, 11)))
  )

}
