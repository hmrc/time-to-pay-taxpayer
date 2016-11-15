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

package uk.gov.hmrc.timetopaytaxpayer.sa

import play.api.http.Status
import play.api.libs.json.{Json, Reads}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.timetopaytaxpayer.sa.DesignatoryDetails.Individual
import uk.gov.hmrc.timetopaytaxpayer.taxpayer.Address
import uk.gov.hmrc.timetopaytaxpayer.{AuthorizedUser, Utr}

import scala.concurrent.{ExecutionContext, Future}

object SelfAssessmentService {

  sealed trait SaError {
    def message: String
  }

  case class SaUserNotFoundError(utr: Utr) extends SaError {
    override def message: String = s"Unable to find self assessment details for UTR ${ utr.value }"
  }

  case class SaServiceError(message: String) extends SaError
  case class SaUnauthorizedError(utr: Utr, user: AuthorizedUser) extends SaError {
    override def message: String = s"User [${user.value}] not authorized to retrieve data for UTR [${ utr.value }]"
  }

  type SaServiceResult = Either[SaError, Individual]

  def address(ws: WSClient, baseUrl: String)
                (path: (Utr => String))(utr: Utr, authorizedUser: AuthorizedUser)
                (implicit executionContext: ExecutionContext): Future[SaServiceResult] = {

    ws.url(s"$baseUrl/${ path(utr) }")
      .withHeaders("Authorization" -> authorizedUser.value)
      .get().map {
      response => response.status match {
        case Status.OK => Right(response.json.as[Individual](DesignatoryDetails.reader))
        case Status.NOT_FOUND => Left(SaUserNotFoundError(utr))
        case Status.UNAUTHORIZED => Left(SaUnauthorizedError(utr, authorizedUser))
        case _ => Left(SaServiceError(response.statusText))
      }
    }.recover {
      case e: Exception => Left(SaServiceError(e.getMessage))
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
