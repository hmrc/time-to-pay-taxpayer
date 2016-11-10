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

package uk.gov.hmrc.timetopayeligibility.controllers

import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences._
import uk.gov.hmrc.timetopayeligibility.debits.Debits._
import uk.gov.hmrc.timetopayeligibility.infrastructure.DesService.{DesError, DesUserNotFoundError}
import uk.gov.hmrc.timetopayeligibility.taxpayer.{Address, SelfAssessmentDetails, TaxPayer}
import uk.gov.hmrc.timetopayeligibility.{Utr, taxpayer}

import scala.concurrent.{ExecutionContext, Future}

class TaxPayerController(debitsService: (Utr => Future[DebitsResult]),
                         preferencesService: (Utr => Future[CommunicationPreferencesResult]))
                        (implicit executionContext: ExecutionContext) extends BaseController {

  def getTaxPayer(utrAsString: String) = Action.async { implicit request =>
    implicit val writeAddress: Writes[TaxPayer] = TaxPayer.writer

    val utr = Utr(utrAsString)

    val result = for {
      debits <- EitherT(debitsService(utr))
      preferences <- EitherT(preferencesService(utr))
    } yield taxPayer(utrAsString, debits, preferences)

    result.fold(handleError, taxPayer => Ok(Json.toJson(taxPayer)))
  }

  private def taxPayer(utrAsString: String, debits: Seq[Debit], preferences: CommunicationPreferences) = {
    TaxPayer(
      customerName = "Customer name",
      addresses = Seq(Address(Seq("123 Fake Street", "Foo", "Bar"), "BN3 2GH")),
      selfAssessment = SelfAssessmentDetails(
        utr = utrAsString,
        communicationPreferences = preferences,
        debits = debits.map(d => taxpayer.Debit(
          originCode = d.charge.originCode,
          amount = d.totalOutstanding,
          dueDate = d.relevantDueDate,
          interest = d.interest.map(i => taxpayer.Interest(i.creationDate, i.amount))
        ))
      )
    )
  }

  private def handleError(error: DesError): Result = error match {
    case DesUserNotFoundError(_) => NotFound
    case e: DesError => InternalServerError(e.message)
  }
}
