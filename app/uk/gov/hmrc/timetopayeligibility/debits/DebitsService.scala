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

package uk.gov.hmrc.timetopayeligibility.debits

import org.joda.time.LocalDate
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.timetopayeligibility.Utr

import scala.concurrent.{ExecutionContext, Future}

object DebitsService {

  type DebitsResult = Either[DebitsError, Seq[Debit]]

  sealed trait DebitsError {def message: String }

  case class DebitsUserNotFound(utr: Utr) extends DebitsError {
    override def message: String = s"Unable to find debits for UTR ${ utr.value }"
  }

  case class DebitsServiceError(message: String) extends DebitsError

  case class Charge(originCode: String, creationDate: LocalDate)

  case class Interest(creationDate: Option[LocalDate], amount: Int)

  case class Debit(taxYearEnd: LocalDate, charge: Charge, relevantDueDate: LocalDate, totalOutstanding: Int, interest: Option[Interest])

  def debits(debitsWsCall: (Utr => Future[WSResponse]))(utr: Utr)(implicit executionContext: ExecutionContext): Future[DebitsResult] = {
    implicit val reader = DebitsJson.reader

    debitsWsCall(utr).map {
      response => response.status match {
        case 200 => Right(response.json.as[Seq[Debit]])
        case 404 => Left(DebitsUserNotFound(utr))
        case _ => Left(DebitsServiceError(response.statusText))
      }
    }.recover {
      case e: Exception => Left(DebitsServiceError(e.getMessage))
    }
  }

  def debitsWsCall(ws: WSClient, baseUrl: String)(utr: Utr)
                  (implicit executionContext: ExecutionContext): Future[WSResponse] = {

    ws.url(s"$baseUrl/sa/taxpayer/${ utr.value }/debits")
      .withHeaders("Authorization" -> "user")
      .get()
  }

}
