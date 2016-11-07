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

package uk.gov.hmrc.timetopayeligibility.returns

import org.joda.time.LocalDate
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.timetopayeligibility.Utr

import scala.concurrent.{ExecutionContext, Future}

object ReturnsService {

  type ReturnsResult = Either[ReturnsError, Seq[Return]]

  sealed trait ReturnsError {def message: String }

  case class ReturnsUserNotFound(utr: Utr) extends ReturnsError {
    override def message: String = s"Unable to find returns for UTR ${ utr.value }"
  }

  case class ReturnsServiceError(message: String) extends ReturnsError

  case class Return(taxYearEnd: LocalDate, issuedDate: Option[LocalDate], dueDate: Option[LocalDate], receivedDate: Option[LocalDate])

  def returns(returnsWsCall: (Utr => Future[WSResponse]))(utr: Utr)(implicit executionContext: ExecutionContext): Future[ReturnsResult] = {
    implicit val reader = ReturnsJson.reader

    returnsWsCall(utr).map {
      response => response.status match {
        case 200 => Right(response.json.as[Seq[Return]])
        case 404 => Left(ReturnsUserNotFound(utr))
        case _ => Left(ReturnsServiceError(response.statusText))
      }
    }.recover {
      case e: Exception => Left(ReturnsServiceError(e.getMessage))
    }
  }

  def returnsWsCall(ws: WSClient, baseUrl: String)(utr: Utr)
                   (implicit executionContext: ExecutionContext): Future[WSResponse] = {

    ws.url(s"$baseUrl/sa/taxpayer/${ utr.value }/returns")
      .withHeaders("Authorization" -> "user")
      .get()
  }
}
