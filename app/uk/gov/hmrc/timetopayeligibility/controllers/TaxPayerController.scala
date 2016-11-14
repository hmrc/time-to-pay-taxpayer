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
import uk.gov.hmrc.timetopayeligibility.infrastructure.DesService.{DesError, DesUnauthorizedError, DesUserNotFoundError}
import uk.gov.hmrc.timetopayeligibility.returns.Returns.{Return, ReturnsResult}
import uk.gov.hmrc.timetopayeligibility.sa.DesignatoryDetails.Individual
import uk.gov.hmrc.timetopayeligibility.sa.SelfAssessmentService.{SaError, SaServiceResult, SaUserNotFoundError}
import uk.gov.hmrc.timetopayeligibility.taxpayer.{Address, SelfAssessmentDetails, TaxPayer}
import uk.gov.hmrc.timetopayeligibility.{AuthorizedUser, Utr, taxpayer}

import scala.concurrent.{ExecutionContext, Future}

class TaxPayerController(debitsService: ((Utr, AuthorizedUser) => Future[DebitsResult]),
                         preferencesService: ((Utr, AuthorizedUser) => Future[CommunicationPreferencesResult]),
                         returnsService: ((Utr, AuthorizedUser) => Future[ReturnsResult]),
                         saService: ((Utr, AuthorizedUser) => Future[SaServiceResult]))
                        (implicit executionContext: ExecutionContext) extends BaseController {

  def getTaxPayer(utrAsString: String) = Action.async { implicit request =>
    implicit val writeAddress: Writes[TaxPayer] = TaxPayer.writer

    val utr = Utr(utrAsString)

    def lookupAuthorizationHeader() = {
      val headerResult: Either[Result, AuthorizedUser] = request.headers.get("authorized").map(AuthorizedUser.apply).toRight(Unauthorized("No authorized header set"))
      EitherT(Future.successful(headerResult))
    }

    (for {
      authorizedUser <- lookupAuthorizationHeader()
      debits <- EitherT(debitsService(utr, authorizedUser)).leftMap(handleError)
      preferences <- EitherT(preferencesService(utr, authorizedUser)).leftMap(handleError)
      returns <- EitherT(returnsService(utr, authorizedUser)).leftMap(handleError)
      individual <- EitherT(saService(utr, authorizedUser)).leftMap(handleError)
    } yield {
      Ok(Json.toJson(taxPayer(utrAsString, debits, preferences, returns, individual)))
    }).merge
  }

  private def taxPayer(utrAsString: String, debits: Seq[Debit], preferences: CommunicationPreferences,
                       returns: Seq[Return], individual: Individual) = {
    val address = individual.address
    TaxPayer(
      customerName = individual.name.toString(),
      addresses = List(
        Address(address.addressLine1,
          address.addressLine2,
          address.addressLine3,
          address.addressLine4,
          address.addressLine5,
          address.postcode)),
      selfAssessment = SelfAssessmentDetails(
        utr = utrAsString,
        communicationPreferences = preferences,
        debits = debits.map(d => taxpayer.Debit(
          originCode = d.charge.originCode,
          amount = d.totalOutstanding,
          dueDate = d.relevantDueDate,
          interest = d.interest.map(i => taxpayer.Interest(i.creationDate, i.amount))
        )),
        returns = returns
      )
    )
  }

  private def handleError(error: DesError): Result = error match {
    case DesUserNotFoundError(_) => NotFound
    case error @ DesUnauthorizedError(_, _) => Unauthorized(error.message)
    case e: DesError => InternalServerError(e.message)
  }

  private def handleError(error: SaError): Result = error match {
    case SaUserNotFoundError(_) => NotFound
    case e: SaError => InternalServerError(e.message)
  }
}
