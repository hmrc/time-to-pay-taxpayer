/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Reads
import timetopaytaxpayer.cor.model.{CommunicationPreferences, SaUtr}
import timetopaytaxpayer.des.model._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DesConnector @Inject() (
    httpClient:     HttpClient,
    servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext) {

  val baseUrl = servicesConfig.baseUrl ("des-services")
  val token = servicesConfig.getString("microservice.services.des-services.authorizationToken")
  val environment = servicesConfig.getString("microservice.services.des-services.serviceEnvironment")

  implicit val desHeaderCarrier: HeaderCarrier = HeaderCarrier(
    authorization = Some(Authorization(s"Bearer $token"))
  ).withExtraHeaders("Environment" -> environment)

  def getReturns(utr: SaUtr): Future[DesReturns] = {
    val serviceUrl = s"/sa/taxpayer/${utr.value}/returns"
    httpClient.GET[DesReturns](s"$baseUrl$serviceUrl")
  }

  def getDebits(utr: SaUtr): Future[DesDebits] = {
    val serviceUrl = s"/sa/taxpayer/${utr.value}/debits"
    httpClient.GET[DesDebits](s"$baseUrl$serviceUrl")
  }

  def getCommunicationPreferences(utr: SaUtr): Future[CommunicationPreferences] = {
    val serviceUrl = s"/sa/taxpayer/${utr.value}/communication-preferences"
    httpClient.GET[CommunicationPreferences](s"$baseUrl$serviceUrl")
  }
}

