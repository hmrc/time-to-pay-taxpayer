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

package timetopaytaxpayer.des

import timetopaytaxpayer.cor.model.{CommunicationPreferences, SaUtr}
import timetopaytaxpayer.des.model._
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DesConnector @Inject() (
    httpClient: HttpClientV2,
    desConfig:  DesConfig
)(implicit ec: ExecutionContext) {

  private val baseUrl = desConfig.baseUrl

  // external services require explicitly passed headers
  private implicit val emptyHc: HeaderCarrier = HeaderCarrier()
  private val headers: Seq[(String, String)] = desConfig.desHeaders

  def getReturns(utr: SaUtr): Future[DesReturns] = {
    httpClient.get(url"$baseUrl/sa/taxpayer/${utr.value}/returns")
      .setHeader(headers: _*)
      .execute[DesReturns]
  }

  def getDebits(utr: SaUtr): Future[DesDebits] = {
    httpClient.get(url"$baseUrl/sa/taxpayer/${utr.value}/debits")
      .setHeader(headers: _*)
      .execute[DesDebits]
  }

  def getCommunicationPreferences(utr: SaUtr): Future[CommunicationPreferences] = {
    httpClient.get(url"$baseUrl/sa/taxpayer/${utr.value}/communication-preferences")
      .setHeader(headers: _*)
      .execute[CommunicationPreferences]
  }
}

