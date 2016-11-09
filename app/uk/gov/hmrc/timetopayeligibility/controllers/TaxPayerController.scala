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

import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences._
import uk.gov.hmrc.timetopayeligibility.debits.Debits._
import uk.gov.hmrc.timetopayeligibility.infrastructure.HmrcEligibilityService.HmrcUserNotFoundError
import uk.gov.hmrc.timetopayeligibility.taxpayer.{Address, SelfAssessmentDetails, TaxPayer}
import uk.gov.hmrc.timetopayeligibility.{Utr, taxpayer}

import scala.concurrent.{ExecutionContext, Future}

class TaxPayerController(debitsService: (Utr => Future[DebitsResult]),
                         preferencesService: (Utr => Future[CommunicationPreferencesResult]))
                        (implicit executionContext: ExecutionContext) extends BaseController {

  def taxPayer(utrAsString: String) = Action.async { implicit request =>
    implicit val writeAddress: Writes[TaxPayer] = TaxPayer.writer

    val utr = Utr(utrAsString)

    (for {
      debitsResult <- debitsService(utr)
      preferencesResult <- preferencesService(utr)
    } yield {
      for {
        debits <- debitsResult.right
        preferences <- preferencesResult.right
      } yield {
        TaxPayer(
          customerName = "Customer name",
          addresses = Seq(Address(Seq("123 Fake Street", "Foo", "Bar"), "BN3 2GH")),
          selfAssessment = SelfAssessmentDetails(
            utr = utrAsString,
            communicationPreferences = preferences,
            debits = debits.map(d => taxpayer.Debit(d.charge.originCode, d.relevantDueDate))
          )
        )
      }
    }).map {
      _.fold({
        case HmrcUserNotFoundError(_) => NotFound
        case error => InternalServerError(error.message)
      }, taxPayer => Ok(Json.toJson(taxPayer)))
    }
  }

}