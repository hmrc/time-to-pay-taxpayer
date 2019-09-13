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

package timetopaytaxpayer.controllers

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import timetopaytaxpayer.cor.model._
import timetopaytaxpayer.des.DesConnector
import timetopaytaxpayer.des.model.{DesReturns, _}
import timetopaytaxpayer.sa.SaConnector
import timetopaytaxpayer.sa.model.SaIndividual
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext

class TaxpayerController @Inject() (
    saConnector:  SaConnector,
    desConnector: DesConnector,
    cc:           ControllerComponents
)(implicit executionContext: ExecutionContext) extends BackendController(cc) {

  def getTaxPayer(utr: SaUtr): Action[AnyContent] = Action.async { implicit request =>

    //start features before for comprehension
    val returnsF = desConnector.getReturns(utr)
    val debitsF = desConnector.getDebits(utr)
    val preferencesF = desConnector.getCommunicationPreferences(utr)
    val individualF = saConnector.getIndividual(utr)

    for {
      returns <- returnsF
      debits <- debitsF
      preferences <- preferencesF
      individual <- individualF
    } yield {
      Ok(Json.toJson(taxPayer(
        utr,
        debits, preferences, returns, individual
      )))
    }
  }

  /**
   * Builds a TaxPayer object based upon the information retrieved from the DES APIs.
   */
  private def taxPayer(
      utr:                      SaUtr,
      debits:                   DesDebits,
      communicationPreferences: CommunicationPreferences,
      returns:                  DesReturns,
      individual:               SaIndividual
  ): Taxpayer = {

    Taxpayer(
      customerName   = individual.name.fullName,
      addresses      = List(individual.address),
      selfAssessment = SelfAssessmentDetails(
        utr                      = utr,
        communicationPreferences = communicationPreferences,
        debits                   = debits.debits.map(_.asDebit()),
        returns                  = returns.returns
      )
    )
  }

}
