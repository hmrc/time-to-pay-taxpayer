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
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import timetopaytaxpayer.cor.model._
import timetopaytaxpayer.des.DesConnector
import timetopaytaxpayer.des.model.{DesReturns, _}
import timetopaytaxpayer.sa.Sa.Individual
import timetopaytaxpayer.sa.SaConnector
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class TaxPayerController @Inject() (
    saConnector:  SaConnector,
    desConnector: DesConnector,
    cc:           ControllerComponents
)(implicit executionContext: ExecutionContext) extends BackendController(cc) {

  def getTaxPayer(utrAsString: String): Action[AnyContent] = Action.async { implicit request =>
    val utr = Utr(utrAsString)

    val possibleUser: Option[AuthorizedUser] = request.headers.get(HeaderNames.authorisation).map(AuthorizedUser.apply)

    possibleUser match {
      case None => Future.successful(Unauthorized(s"Unauthorized DES call for user with UTR [${utr.value}] not found"))
      case Some(authorizedUser) => {

        //start features before for comprehension
        val returnsF = desConnector.returns(utr)
        val debitsF = desConnector.debits(utr)
        val preferencesF = desConnector.preferences(utr)
        val individualF = saConnector.individual(utr, authorizedUser)

        for {
          returns <- returnsF
          debits <- debitsF
          preferences <- preferencesF
          individual <- individualF
        } yield {
          Ok(Json.toJson(taxPayer(
            utrAsString,
            debits, preferences, returns, individual
          )))
        }
      }
    }
  }

  /**
   * Builds a TaxPayer object based upon the information retrieved from the DES APIs.
   */
  private def taxPayer(
      utrAsString:              String,
      debits:                   DesDebits,
      communicationPreferences: CommunicationPreferences,
      returns:                  DesReturns,
      individual:               Individual
  ): TaxPayer = {

    TaxPayer(
      customerName   = individual.name.fullName,
      addresses      = List(individual.address),
      selfAssessment = SelfAssessmentDetails(
        utr                      = utrAsString,
        communicationPreferences = communicationPreferences,
        debits                   = debits.debits.map(_.asDebit()),
        returns                  = returns.returns
      )
    )
  }

}
