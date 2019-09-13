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

object TdAll {

  val saUtr = SaUtr("3217334604")

  val taxpayer: Taxpayer = Taxpayer(
    customerName   = "Mr Lester Corncrake",
    addresses      = List(
      Address(
        addressLine1 = "123 Any Street",
        addressLine2 = "Kingsland High Road",
        addressLine3 = "Dalston",
        addressLine4 = "Greater London",
        addressLine5 = "",
        postcode     = "E8 3PP"
      )
    ),
    selfAssessment = SelfAssessmentDetails(
      utr                      = saUtr,
      communicationPreferences = CommunicationPreferences(
        welshLanguageIndicator = true,
        audioIndicator         = false,
        largePrintIndicator    = false,
        brailleIndicator       = false
      ),
      debits                   = List(
        Debit(
          originCode = "IN1",
          amount     = 2500,
          dueDate    = "2019-02-25",
          interest   = None,
          taxYearEnd = "2019-04-05"
        ),
        Debit(
          originCode = "IN2",
          amount     = 2500,
          dueDate    = "2019-02-25",
          interest   = None,
          taxYearEnd = "2019-04-05"
        )
      ),
      returns                  = List(
        Return(
          taxYearEnd   = "2019-04-05",
          issuedDate   = None,
          dueDate      = "2019-01-31",
          receivedDate = None
        ),
        Return(
          taxYearEnd   = "2018-04-05",
          issuedDate   = None,
          dueDate      = "2018-01-31",
          receivedDate = "2018-03-09"
        )
      )
    )
  )

  private implicit def toSome[T](t: T): Option[T] = Some(t)
  private implicit def toLocalDate(t: String): LocalDate = LocalDate.parse(t)
  private implicit def toSomeLocalDate(t: String): Option[LocalDate] = Some(LocalDate.parse(t))

}
