/*
 * Copyright 2023 HM Revenue & Customs
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

package timetopaytaxpayer.sa

import play.api.http.Status.NOT_FOUND
import timetopaytaxpayer.cor.model.SaUtr
import timetopaytaxpayer.sa.model.SaIndividual
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SaConnector @Inject() (
    httpClient: HttpClientV2,
    config:     ServicesConfig
)(implicit ec: ExecutionContext) {

  val baseUrl = config.baseUrl("sa-services")

  // return None if API call returns 404
  def getIndividual(utr: SaUtr)(implicit hc: HeaderCarrier): Future[Option[SaIndividual]] = {
    httpClient.get(url"$baseUrl/sa/individual/${utr.value}/designatory-details/taxpayer")
      .execute[SaIndividual]
      .map(Some(_)).recover {
        case e: HttpException if e.responseCode == NOT_FOUND                           => None
        case UpstreamErrorResponse.Upstream4xxResponse(e) if e.statusCode == NOT_FOUND => None
      }
  }
}
