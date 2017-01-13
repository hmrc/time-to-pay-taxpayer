/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.timetopaytaxpayer

import uk.gov.hmrc.play.config.ServicesConfig

object ApplicationConfig extends ServicesConfig {

  lazy val desServicesUrl = baseUrl("des-services")
  lazy val desAuthorizationToken = getConfString("des-services.authorizationToken")
  lazy val desServiceEnvironment = getConfString("des-services.serviceEnvironment")

  lazy val saServicesUrl = baseUrl("sa-services")

  def getConfString(key: String): String = getConfString(key, throw new IllegalArgumentException(s"Missing property $key"))
}
