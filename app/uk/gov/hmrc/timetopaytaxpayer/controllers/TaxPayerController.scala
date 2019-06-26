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

import cats.data.EitherT
import cats.implicits._
import play.api.Logger
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.timetopaytaxpayer.communication.preferences.CommunicationPreferences
import uk.gov.hmrc.timetopaytaxpayer.communication.preferences.CommunicationPreferences._
import uk.gov.hmrc.timetopaytaxpayer.debits.Debits._
import uk.gov.hmrc.timetopaytaxpayer.infrastructure.DesService.{DesError, DesUnauthorizedError, DesUserNotFoundError}
import uk.gov.hmrc.timetopaytaxpayer.returns.Returns.{Return, ReturnsResult}
import uk.gov.hmrc.timetopaytaxpayer.sa.DesignatoryDetails.Individual
import uk.gov.hmrc.timetopaytaxpayer.sa.SelfAssessmentService.{SaError, SaServiceResult, SaUnauthorizedError, SaUserNotFoundError}
import uk.gov.hmrc.timetopaytaxpayer.taxpayer.{SelfAssessmentDetails, TaxPayer}
import uk.gov.hmrc.timetopaytaxpayer.{AuthorizedUser, Utr, taxpayer}

import scala.concurrent.{ExecutionContext, Future}

class TaxPayerController(debitsService: (Utr => Future[DebitsResult]),
                         preferencesService: (Utr => Future[CommunicationPreferencesResult]),
                         returnsService: (Utr => Future[ReturnsResult]),
                         saService: ((Utr, AuthorizedUser) => Future[SaServiceResult]))
                        (implicit executionContext: ExecutionContext) extends BaseController {

  def getTaxPayer(utrAsString: String) = Action.async { implicit request =>
    implicit val writeTaxPayer: Writes[TaxPayer] = TaxPayer.writer

    val utr = Utr(utrAsString)

    def lookupAuthorizationHeader() = {
      val headerResult: Either[Result, AuthorizedUser] = request.headers.get(HeaderNames.authorisation).map(AuthorizedUser.apply).toRight(Unauthorized("No authorization header set"))
      EitherT(Future.successful(headerResult))
    }

    val service: Future[DebitsResult] = debitsService(utr)
    EitherT(service).leftMap(handleError)

    /**
      * Collects the information from the DES APIs and attempts to build a TaxPayer.
      */
    (for {
      authorizedUser <- lookupAuthorizationHeader()
      debits <- EitherT(debitsService(utr)).leftMap(handleError)
      preferences <- EitherT(preferencesService(utr)).leftMap(handleError)
      returns <- EitherT(returnsService(utr)).leftMap(handleError)
      individual <- EitherT(saService(utr, authorizedUser)).leftMap(handleError)
    } yield {
      Ok(Json.toJson(taxPayer(utrAsString, debits, preferences, returns, individual)))
    }).merge
  }

  /**
    * Builds a TaxPayer object based upon the information retrieved from the DES APIs.
    */
  private def taxPayer(utrAsString: String, debits: Seq[Debit], preferences: CommunicationPreferences,
                       returns: Seq[Return], individual: Individual) = {
    val address = individual.address
    TaxPayer(
      customerName = individual.name.toString(),
      addresses = List(address),
      selfAssessment = SelfAssessmentDetails(
        utr = utrAsString,
        communicationPreferences = preferences,
        debits = debits.map(d => taxpayer.Debit(
          originCode = d.charge.originCode,
          amount = d.totalOutstanding,
          dueDate = d.relevantDueDate,
          interest = d.interest.map(i => taxpayer.Interest(i.creationDate, i.amount)),
          taxYearEnd = d.taxYearEnd
        )),
        returns = returns
      )
    )
  }

  /**
    * Handles any errors that are thrown back from the DES APIs.
    */
  private def handleError(error: DesError): Result = error match {
    case e@DesUserNotFoundError(_) => Logger.error(e.message)
      NotFound
    case e@DesUnauthorizedError(_) => Logger.error(e.message)
      Unauthorized(error.message)
    case e: DesError => Logger.error(e.message)
      InternalServerError(e.message)
  }

  /**
    * Handles any errors that are thrown back from the SA service.
    */
  private def handleError(error: SaError): Result = error match {
    case e@SaUserNotFoundError(_) => Logger.error(e.message)
      NotFound
    case e@SaUnauthorizedError(_, _) => Logger.error(e.message)
      Unauthorized(error.message)
    case e: SaError => Logger.error(e.message)
      InternalServerError(e.message)
  }
}
