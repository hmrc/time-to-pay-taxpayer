/*
 * Copyright 2023 HM Revenue & Customs
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
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json.toJson
import play.api.mvc._
import timetopaytaxpayer.actions.Actions
import timetopaytaxpayer.cor.model._
import timetopaytaxpayer.des.DesConnector
import timetopaytaxpayer.des.model.{DesReturns, _}
import timetopaytaxpayer.sa.SaConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxpayerController @Inject() (
    actions:      Actions,
    saConnector:  SaConnector,
    desConnector: DesConnector,
    cc:           ControllerComponents
)(implicit executionContext: ExecutionContext, clock: Clock) extends BackendController(cc) {

  def getTaxPayer(utr: SaUtr): Action[AnyContent] = actions.authenticatedAction.async { implicit request =>
    saConnector.getIndividual(utr).flatMap[Result]{
      case None =>
        Future.successful(NotFound)

      case Some(individual) =>
        for {
          returns <- desConnector.getReturns(utr)
          debits <- desConnector.getDebits(utr)
          preferences <- desConnector.getCommunicationPreferences(utr)
        } yield {
          Ok(toJson(Taxpayer(
            customerName   = individual.name.fullName,
            addresses      = List(individual.address),
            selfAssessment = SelfAssessmentDetails(utr, preferences, debits.debits.map(_.asDebit()), returns.returns).fixReturns
          )))
        }
    }
  }

}
