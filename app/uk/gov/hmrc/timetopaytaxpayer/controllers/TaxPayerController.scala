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

package uk.gov.hmrc.timetopaytaxpayer.controllers

import javax.inject.Inject
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.timetopaytaxpayer.communication.preferences.CommunicationPreferences
import uk.gov.hmrc.timetopaytaxpayer.connectors.{DesConnector, SaConnector}
import uk.gov.hmrc.timetopaytaxpayer.debits.Debits._
import uk.gov.hmrc.timetopaytaxpayer.returns.Returns.Return
import uk.gov.hmrc.timetopaytaxpayer.taxpayer.DesignatoryDetails.Individual
import uk.gov.hmrc.timetopaytaxpayer.taxpayer.{Interest, SelfAssessmentDetails, TaxPayer}
import uk.gov.hmrc.timetopaytaxpayer.{AuthorizedUser, Utr, taxpayer}

import scala.concurrent.{ExecutionContext, Future}

class TaxPayerController @Inject() (
    saConnector:  SaConnector,
    desConnector: DesConnector,
    cc:           ControllerComponents)
  (implicit executionContext: ExecutionContext) extends BackendController(cc) {

  def getTaxPayer(utrAsString: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val writeTaxPayer: Writes[TaxPayer] = TaxPayer.writer

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
          Ok(Json.toJson(taxPayer(utrAsString, debits, preferences, returns, individual)))
        }
      }
    }

  }

  /**
   * Builds a TaxPayer object based upon the information retrieved from the DES APIs.
   */
  private def taxPayer(utrAsString: String, debits: Seq[Debit], preferences: CommunicationPreferences,
                       returns: Seq[Return], individual: Individual) = {
    val address = individual.address
    TaxPayer(
      customerName   = individual.name.fullName,
      addresses      = List(address),
      selfAssessment = SelfAssessmentDetails(
        utr                      = utrAsString,
        communicationPreferences = preferences,
        debits                   = debits.map(d => taxpayer.Debit(
          originCode = d.charge.originCode,
          amount     = d.totalOutstanding,
          dueDate    = d.relevantDueDate,
          interest   = d.interest.map(i => Interest(i.creationDate, i.amount)),
          taxYearEnd = d.taxYearEnd
        )),
        returns                  = returns
      )
    )
  }

}
