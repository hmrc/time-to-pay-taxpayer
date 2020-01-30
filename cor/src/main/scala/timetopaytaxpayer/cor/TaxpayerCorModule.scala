/*
 * Copyright 2020 HM Revenue & Customs
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

import com.google.inject.{AbstractModule, Provides, Singleton}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext

class TaxpayerCorModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def provideTaxpayerConnector(
      servicesConfig: ServicesConfig,
      http:           HttpClient
  )(
      implicit
      ec: ExecutionContext
  ): TaxpayerConnector = new TaxpayerConnector(servicesConfig, http)

}