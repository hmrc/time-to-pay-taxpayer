/*
 * Copyright 2021 HM Revenue & Customs
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

package timetopayarrangement.des

import com.google.inject.Inject
import timetopayarrangement.des.model.DesSetupArrangementRequest
import timetopaytaxpayer.cor.model.SaUtr
import timetopaytaxpayer.des.DesConfig
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.Authorization
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

class DesConnector @Inject() (
    servicesConfig: ServicesConfig,
    desConfig:      DesConfig,
    httpClient:     HttpClient
)(implicit ec: ExecutionContext) {

  private val desArrangementUrl: String = servicesConfig.baseUrl("des-arrangement-api")

  // external services require explicitly passed headers
  private implicit val emptyHc: HeaderCarrier = HeaderCarrier()
  private val headers: Seq[(String, String)] = desConfig.desHeaders

  def submitArrangement(utr: SaUtr, desSetupArrangementRequest: DesSetupArrangementRequest): Future[Unit] = {
    val url = s"$desArrangementUrl/time-to-pay/taxpayers/${utr.value}/arrangements"
    httpClient.POST[DesSetupArrangementRequest, Unit](url, desSetupArrangementRequest, headers = headers)
  }
}
