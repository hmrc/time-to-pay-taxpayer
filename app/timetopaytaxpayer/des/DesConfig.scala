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

import com.google.inject.Inject
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

final case class DesConfig(baseUrl: String, serviceEnvironment: String, authorisationToken: String) {
  @Inject
  def this(servicesConfig: ServicesConfig) = {
    this(
      baseUrl            = servicesConfig.baseUrl("des-services"),
      serviceEnvironment = servicesConfig.getString("microservice.services.des-services.serviceEnvironment"),
      authorisationToken = servicesConfig.getString("microservice.services.des-services.authorizationToken")
    )
  }

  val desHeaders: Seq[(String, String)] = Seq(
    "Authorization" -> s"Bearer $authorisationToken",
    "Environment" -> serviceEnvironment
  )

}

object DesConfig {
  implicit val desConfigFormat: Format[DesConfig] = Json.format[DesConfig]
}
