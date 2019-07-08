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

package uk.gov.hmrc.timetopaytaxpayer.connectors

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.Reads
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.timetopaytaxpayer.Config.ApplicationConfig
import uk.gov.hmrc.timetopaytaxpayer.Utr
import uk.gov.hmrc.timetopaytaxpayer.communication.preferences.CommunicationPreferences
import uk.gov.hmrc.timetopaytaxpayer.debits.Debits
import uk.gov.hmrc.timetopaytaxpayer.debits.Debits.Debit
import uk.gov.hmrc.timetopaytaxpayer.returns.Returns
import uk.gov.hmrc.timetopaytaxpayer.returns.Returns.Return

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DesConnector @Inject() (
    httpClient: HttpClient,
    config:     ApplicationConfig)(implicit ec: ExecutionContext) {

  lazy implicit val desHeaderCarrier: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Bearer ${config.desAuthorizationToken}")))
    .withExtraHeaders("Environment" -> config.desServiceEnvironment)

  def returns(utr: Utr): Future[Seq[Return]] = {
    val serviceUrl = s"sa/taxpayer/${utr.value}/returns"
    implicit val returnsReads: Reads[Seq[Return]] = Returns.reader
    httpClient.GET[Seq[Return]](s"${config.desServicesUrl}/$serviceUrl")
  }

  def debits(utr: Utr): Future[Seq[Debit]] = {
    val serviceUrl = s"sa/taxpayer/${utr.value}/debits"
    implicit val debitsRead: Reads[Seq[Debit]] = Debits.reader
    httpClient.GET[Seq[Debit]](s"${config.desServicesUrl}/$serviceUrl")
  }

  def preferences(utr: Utr): Future[CommunicationPreferences] = {
    val serviceUrl = s"sa/taxpayer/${utr.value}/communication-preferences"
    implicit val debitsRead: Reads[CommunicationPreferences] = CommunicationPreferences.reader
    httpClient.GET[CommunicationPreferences](s"${config.desServicesUrl}/$serviceUrl")
  }

}

