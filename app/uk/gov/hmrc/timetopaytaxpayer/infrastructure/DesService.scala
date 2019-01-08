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

package uk.gov.hmrc.timetopaytaxpayer.infrastructure

import play.api.http.Status
import play.api.libs.json.Reads
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import uk.gov.hmrc.timetopaytaxpayer.Utr

import scala.concurrent.{ExecutionContext, Future}

object DesService {

  sealed trait DesError {
    def message: String
  }

  case class DesUserNotFoundError(utr: Utr) extends DesError {
    override def message: String = s"User with UTR [${ utr.value }] not found in DES"
  }

  case class DesServiceError(message: String) extends DesError

  case class DesUnauthorizedError(utr: Utr) extends DesError {
    override def message: String = s"Unauthorized DES call for user with UTR [${ utr.value }] not found"
  }

  type DesServiceResult[T] = Either[DesError, T]

  /**
    * Calls a GET request to the given DES API endpoint and handles the response accordingly.
    * Returns the relevant data eg SAReturns, SADebits or Communication Preferences with an OK status.
    * Will otherwise return any errors outlined in the DES eligibility API specifications.
    */
  def wsCall[T](ws: WSClient, wsRequest: (WSRequest => Future[WSResponse]), baseUrl: String, serviceEnvironment: String, authorizationToken: String)
               (reader: Reads[T], path: (Utr => String))(utr: Utr)
               (implicit executionContext: ExecutionContext): Future[DesServiceResult[T]] = {

    wsRequest(ws.url(s"$baseUrl/${ path(utr) }")
      .withHeaders("Authorization" -> s"Bearer $authorizationToken")
      .withHeaders("Environment" -> serviceEnvironment)
      .withMethod("GET")).map {
      response => response.status match {
        case Status.OK => Right(response.json.as[T](reader))
        case Status.NOT_FOUND => Left(DesUserNotFoundError(utr))
        case Status.UNAUTHORIZED => Left(DesUnauthorizedError(utr))
        case _ => Left(DesServiceError((response.json \ "reason").asOpt[String].getOrElse(response.statusText)))
      }
    }.recover {
      case e: Exception =>
        e.printStackTrace()
        Left(DesServiceError(s"DES error [${ e.getMessage }]"))
    }
  }
}
