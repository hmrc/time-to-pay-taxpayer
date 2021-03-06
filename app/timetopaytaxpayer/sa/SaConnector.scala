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

package timetopaytaxpayer.sa

import javax.inject.{Inject, Singleton}
import timetopaytaxpayer.cor.model.SaUtr
import timetopaytaxpayer.sa.model.SaIndividual
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SaConnector @Inject() (
    httpClient: HttpClient,
    config:     ServicesConfig
)(implicit ec: ExecutionContext) {

  val baseUrl = config.baseUrl("sa-services")

  def getIndividual(utr: SaUtr)(implicit hc: HeaderCarrier): Future[SaIndividual] = {
    val serviceUrl = s"/sa/individual/${utr.value}/designatory-details/taxpayer"
    httpClient.GET[SaIndividual](s"$baseUrl$serviceUrl")
  }
}
