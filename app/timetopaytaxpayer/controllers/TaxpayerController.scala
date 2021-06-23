/*
 * Copyright 2021 HM Revenue & Customs
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

package timetopaytaxpayer.controllers

import java.time.Clock
import javax.inject.Inject
import play.api.libs.json.Json.toJson
import play.api.mvc._
import timetopaytaxpayer.cor.model._
import timetopaytaxpayer.des.DesConnector
import timetopaytaxpayer.des.model.{DesReturns, _}
import timetopaytaxpayer.sa.SaConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class TaxpayerController @Inject() (
    saConnector:  SaConnector,
    desConnector: DesConnector,
    cc:           ControllerComponents
)(implicit executionContext: ExecutionContext, clock: Clock) extends BackendController(cc) {

  // todo - remove as part of OPS-4581
  def getTaxPayer(utr: SaUtr): Action[AnyContent] = Action.async { implicit request =>
    for {
      returns <- desConnector.getReturns(utr)
      debits <- desConnector.getDebits(utr)
      preferences <- desConnector.getCommunicationPreferences(utr)
      individual <- saConnector.getIndividual(utr)
    } yield {
      Ok(toJson(Taxpayer(
        customerName   = individual.name.fullName,
        addresses      = List(individual.address),
        selfAssessment = SelfAssessmentDetails(utr, preferences, debits.debits.map(_.asDebit()), returns.returns).fixReturns
      )))
    }
  }

  def getReturnsAndDebits(utr: SaUtr): Action[AnyContent] = Action.async { _ =>
    val returnsF = desConnector.getReturns(utr)
    val debitsF = desConnector.getDebits(utr)

    for {
      returns: DesReturns <- returnsF
      debits: DesDebits <- debitsF
      returnsAndDebits = ReturnsAndDebits(
        debits  = debits.debits.map(_.asDebit()),
        returns = returns.returns
      ).fixReturns
      result = returnsAndDebits.fixReturns
    } yield Ok(toJson(result))
  }
}
