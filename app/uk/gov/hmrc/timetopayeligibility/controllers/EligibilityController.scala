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

import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.timetopayeligibility.Utr
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences.CommunicationPreferencesResult
import uk.gov.hmrc.timetopayeligibility.debits.DebitsService.{Charge, Debit, DebitsResult, Interest}
import uk.gov.hmrc.timetopayeligibility.returns.ReturnsService.{Return, ReturnsResult}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EligibilityController(returnsService: (Utr => Future[ReturnsResult]),
                            debitsService: (Utr => Future[DebitsResult]),
                            preferencesService: (Utr => Future[CommunicationPreferencesResult]) )
                           (implicit executionContext: ExecutionContext) extends BaseController {

  def eligibility(utrAsString: String) = Action.async { implicit request =>
    implicit val formatReturn = Json.format[Return]
    implicit val formatInterest = Json.format[Interest]
    implicit val formatCharge = Json.format[Charge]
    implicit val formatDebit = Json.format[Debit]
    implicit val formatCommunicationPreferences = Json.format[CommunicationPreferences]

    val utr = Utr(utrAsString)

    (for {
      returnsResult <- returnsService(utr)
      debitsResult <- debitsService(utr)
      preferencesResult <- preferencesService(utr)
    } yield {
      Seq(
        returnsResult.fold(error => Json.toJson(error.message), returns => Json.toJson(returns)),
        debitsResult.fold(error => Json.toJson(error.message), debits => Json.toJson(debits)),
        preferencesResult.fold(error => Json.toJson(error.message), preferences => Json.toJson(preferences))
      )
    }).map(jsons => Ok(Json.toJson(jsons)))
  }
}
