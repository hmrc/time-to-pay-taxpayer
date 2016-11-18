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

package uk.gov.hmrc.timetopaytaxpayer.infrastructure

import java.net.URL

import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.mvc.Headers
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{DataEvent, EventTypes}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

object WebService {

  def request(connector: AuditConnector, errorLogger: Option[(Throwable => Unit)])
             (request: WSRequest)(implicit ec: ExecutionContext): Future[WSResponse] = {
    val eventualResponse = request.execute()

    eventualResponse.onFailure {
      case NonFatal(t) =>
        errorLogger.foreach(log => log(t))
    }
    eventualResponse.map {
      response =>
        val path = new URL(request.url).getPath
        val hc = headerCarrier(request)
        val tags = Map("method" -> request.method, "statusCode" -> s"${ response.status }", "responseBody" -> response.body)
        connector.sendEvent(
          DataEvent("time-to-pay-taxpayer", EventTypes.Succeeded, tags = tags ++ hc.toAuditTags(path, path), detail = hc.toAuditDetails())
        )
        response
    }
  }

  private def headerCarrier(request: WSRequest): HeaderCarrier = {
    val headers = request.headers.map {
      case (k, v) => (k, v.headOption.getOrElse(""))
    }.toSeq

    HeaderCarrier.fromHeadersAndSession(Headers(headers: _*))
  }
}
