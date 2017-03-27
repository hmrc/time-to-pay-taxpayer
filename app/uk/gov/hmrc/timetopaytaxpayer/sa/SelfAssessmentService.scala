/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.timetopaytaxpayer.sa

import play.api.http.Status
import play.api.libs.json.{Json, Reads}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import uk.gov.hmrc.timetopaytaxpayer.sa.DesignatoryDetails.Individual
import uk.gov.hmrc.timetopaytaxpayer.taxpayer.Address
import uk.gov.hmrc.timetopaytaxpayer.{AuthorizedUser, Utr}

import scala.concurrent.{ExecutionContext, Future}

object SelfAssessmentService {

  sealed trait SaError {
    def message: String
  }

  case class SaUserNotFoundError(utr: Utr) extends SaError {
    override def message: String = s"Unable to find Self Assessment details for UTR ${ utr.value }"
  }

  case class SaServiceError(message: String) extends SaError
  case class SaUnauthorizedError(utr: Utr, user: AuthorizedUser) extends SaError {
    override def message: String = s"User [${user.value}] not authorized to retrieve Self Assessment details for UTR [${ utr.value }]"
  }

  type SaServiceResult = Either[SaError, Individual]

  /**
    * Calls a GET request to the SA service and returns the user's address and personal details if successful.
    * Will otherwise return an error depending on the information sent.
    */
  def address(ws: WSClient, wsRequest: (WSRequest => Future[WSResponse]), baseUrl: String)
                (path: (Utr => String))(utr: Utr, authorizedUser: AuthorizedUser)
                (implicit executionContext: ExecutionContext): Future[SaServiceResult] = {

    wsRequest(ws.url(s"$baseUrl/${ path(utr) }")
      .withHeaders("Authorization" -> authorizedUser.value)
      .withMethod("GET")).map {
      response => response.status match {
        case Status.OK => Right(response.json.as[Individual](DesignatoryDetails.reader))
        case Status.NOT_FOUND => Left(SaUserNotFoundError(utr))
        case Status.UNAUTHORIZED => Left(SaUnauthorizedError(utr, authorizedUser))
        case _ => Left(SaServiceError(response.statusText))
      }
    }.recover {
      case e: Exception => Left(SaServiceError(s"Self Assessment Service error [${e.getMessage}]"))
    }
  }

}

object DesignatoryDetails {

  case class Individual(name: Name, address: Address)

  case class Name(title: String, forename: String, secondForename: Option[String], surname: String) {
    override def toString() = Seq(title, forename, secondForename.getOrElse(""), surname).filterNot(_.isEmpty).mkString(" ")
  }

  val reader: Reads[Individual]  = {
    implicit val readAddress: Reads[Address] = Json.reads[Address]
    implicit val readName: Reads[Name] = Json.reads[Name]

    Json.reads[Individual]
  }
}
