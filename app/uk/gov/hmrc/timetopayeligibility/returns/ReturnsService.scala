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
import uk.gov.hmrc.play.http.logging.Authorization
import uk.gov.hmrc.play.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.timetopayeligibility.{Utr, WSHttp}

import scala.concurrent.{ExecutionContext, Future}

object ReturnsService {

  type ReturnsResult = Either[ReturnsError, Seq[Return]]

  sealed trait ReturnsError {def message: String }

  case class ReturnsUserNotFound(utr: Utr) extends ReturnsError {
    override def message: String = s"Unable to find returns for UTR ${ utr.value }"
  }

  case class ReturnsServiceError(message: String) extends ReturnsError

  case class Return(taxYearEnd: LocalDate, issuedDate: Option[LocalDate], dueDate: Option[LocalDate], receivedDate: Option[LocalDate])

  def returns(returnsWsCall: (Utr => Future[Seq[Return]]))(utr: Utr)(implicit executionContext: ExecutionContext): Future[ReturnsResult] = {
    returnsWsCall(utr).map(Right(_)).recover {
      case nf: NotFoundException => Left(ReturnsUserNotFound(utr))
      case e => Left(ReturnsServiceError(e.getMessage))
    }
  }

  def localCall(utr: Utr)(implicit executionContext: ExecutionContext) = {
    implicit val reader = ReturnsJson.reader
    implicit val headerCarrier = HeaderCarrier(authorization = Some(Authorization("user")))

    WSHttp.GET[Seq[Return]](s"http://localhost:8887/sa/taxpayer/${ utr.value }/returns")
  }
}
