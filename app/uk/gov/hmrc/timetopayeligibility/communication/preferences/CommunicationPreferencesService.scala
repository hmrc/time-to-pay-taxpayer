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

package uk.gov.hmrc.timetopayeligibility.communication.preferences

import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.timetopayeligibility.Utr

import scala.concurrent.{ExecutionContext, Future}

object CommunicationPreferencesService {

  type CommunicationPreferencesResult = Either[CommunicationPreferencesError, CommunicationPreferences]

  sealed trait CommunicationPreferencesError {def message: String }

  case class CommunicationPreferencesUserNotFound(utr: Utr) extends CommunicationPreferencesError {
    override def message: String = s"Unable to find communication preferences for UTR ${ utr.value }"
  }

  case class CommunicationPreferencesServiceError(message: String) extends CommunicationPreferencesError

  case class CommunicationPreferences(welshLanguageIndicator: Boolean, audioIndicator: Boolean,
                                      largePrintIndicator: Boolean, brailleIndicator: Boolean)

  def preferences(wsCall: (Utr => Future[WSResponse]))(utr: Utr)
                 (implicit executionContext: ExecutionContext): Future[CommunicationPreferencesResult] = {
    implicit val reader = CommunicationPreferencesJson.reader

    wsCall(utr).map {
      response => response.status match {
        case 200 => Right(response.json.as[CommunicationPreferences])
        case 404 => Left(CommunicationPreferencesUserNotFound(utr))
        case _ => Left(CommunicationPreferencesServiceError(response.statusText))
      }
    }.recover {
      case e: Exception => Left(CommunicationPreferencesServiceError(e.getMessage))
    }
  }

  def wsCall(ws: WSClient, baseUrl: String)(utr: Utr)
            (implicit executionContext: ExecutionContext): Future[WSResponse] = {

    ws.url(s"$baseUrl/sa/taxpayer/${ utr.value }/communication-preferences")
      .withHeaders("Authorization" -> "user")
      .get()
  }
}
