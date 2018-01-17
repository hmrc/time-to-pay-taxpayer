/*
 * Copyright 2018 HM Revenue & Customs
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
import cats._
import cats.data._
import cats.implicits._
import play.api.Logger

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

  //from the sa docs: All of the value fields nested under name, address, telephone and email are optional and the client should expect any of them to be unspecified.
  //https://github.tools.tax.service.gov.uk/HMRC/sa#get-saindividualsautrdesignatory-detailstaxpayer
  //SSTTP2-363
  case class Name(
    title: Option[String],
    forename: Option[String],
    secondForename: Option[String],
    surname: String
  ) {

    override def toString: String = fullName

    private def fullName: String = Seq(title, forename, secondForename, surname.some).collect{
      case Some(x) => x
    }.mkString(" ")
  }

  val reader: Reads[Individual]  = {
    implicit val readAddress: Reads[Address] = Json.reads[Address]
    implicit val readName: Reads[Name] = Json.reads[Name]

    Json.reads[Individual]
  }
}
