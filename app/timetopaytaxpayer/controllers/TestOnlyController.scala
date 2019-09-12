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

package timetopaytaxpayer.controllers

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import javax.inject._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import timetopaytaxpayer.config.ApplicationConfig
import timetopaytaxpayer.des.DesConnector
import timetopaytaxpayer.sa.SaConnector
import uk.gov.hmrc.play.bootstrap.controller.BackendController

class TestOnlyController @Inject() (
    applicationConfig: ApplicationConfig,
    cc:                ControllerComponents,
    desConnector:      DesConnector,
    saConnector:       SaConnector
)
  extends BackendController(cc) {

  def config() = cc.actionBuilder { r =>
    val result: JsValue = Json.parse(
      ConfigFactory.load().root().render(ConfigRenderOptions.concise())
    )
    Results.Ok(result)
  }

  def connectorsConfig() = cc.actionBuilder { r =>
    Ok(Json.obj(
      "desServicesUrl" -> desConnector.baseUrl,
      "desAuthorizationToken" -> desConnector.token, //it's test only endpoint, no worries
      "desServiceEnvironment" -> desConnector.environment,
      "saServicesUrl" -> saConnector.baseUrl
    ))
  }

}
