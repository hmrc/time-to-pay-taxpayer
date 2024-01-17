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

package timetopaytaxpayer.cor

import play.api.http.Status.NOT_FOUND
import timetopaytaxpayer.cor.model.{ReturnsAndDebits, SaUtr, Taxpayer}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

class TaxpayerConnector(
    servicesConfig: ServicesConfig,
    http:           HttpClient
)(
    implicit
    ec: ExecutionContext
) {

  val baseUrl: String = servicesConfig.baseUrl("time-to-pay-taxpayer")

  def getReturnsAndDebits(utr: SaUtr)(implicit hc: HeaderCarrier): Future[ReturnsAndDebits] = {
    http.GET[ReturnsAndDebits](s"$baseUrl/taxpayer/returns-and-debits/${utr.value}")
  }

  // todo - remove as part of OPS-4581
  def getTaxPayer(utr: SaUtr)(implicit hc: HeaderCarrier): Future[Option[Taxpayer]] = {
    http.GET[Taxpayer](s"$baseUrl/taxpayer/${utr.value}")
      .map(Some(_))
      .recover {
        case e: HttpException if e.responseCode == NOT_FOUND                           => None
        case UpstreamErrorResponse.Upstream4xxResponse(e) if e.statusCode == NOT_FOUND => None
      }
  }

}
