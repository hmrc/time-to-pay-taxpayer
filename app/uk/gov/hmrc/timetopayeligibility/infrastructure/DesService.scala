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

package uk.gov.hmrc.timetopayeligibility.infrastructure

import play.api.http.Status
import play.api.libs.json.Reads
import play.api.libs.ws.WSClient
import uk.gov.hmrc.timetopayeligibility.Utr

import scala.concurrent.{ExecutionContext, Future}

object DesService {

  sealed trait DesError {
    def message: String
  }

  case class DesUserNotFoundError(utr: Utr) extends DesError {
    override def message: String = s"Unable to find communication preferences for UTR ${ utr.value }"
  }

  case class DesServiceError(message: String) extends DesError

  type DesServiceResult[T] = Either[DesError, T]

  def wsCall[T](ws: WSClient, baseUrl: String)
               (reader: Reads[T], path: (Utr => String))(utr: Utr)
               (implicit executionContext: ExecutionContext): Future[DesServiceResult[T]] = {

    ws.url(s"$baseUrl/${ path(utr) }")
      .withHeaders("Authorization" -> "user")
      .get().map {
      response => response.status match {
        case Status.OK => Right(response.json.as[T](reader))
        case Status.NOT_FOUND => Left(DesUserNotFoundError(utr))
        case _ => Left(DesServiceError((response.json \ "reason").asOpt[String].getOrElse(response.statusText)))
      }
    }.recover {
      case e: Exception => Left(DesServiceError(e.getMessage))
    }
  }
}
