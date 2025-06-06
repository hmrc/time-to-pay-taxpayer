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

package timetopaytaxpayer.testonly.controllers

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import timetopaytaxpayer.des.DesConfig
import timetopaytaxpayer.sa.SaConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}

@Singleton
class TestOnlyController @Inject() (
    cc:          ControllerComponents,
    desConfig:   DesConfig,
    saConnector: SaConnector
)
  extends BackendController(cc) {

  val config = cc.actionBuilder { r =>
    val result: JsValue = Json.parse(
      ConfigFactory.load().root().render(ConfigRenderOptions.concise())
    )
    Results.Ok(result)
  }

  val connectorsConfig = cc.actionBuilder { r =>
    Ok(Json.obj(
      "desServicesUrl" -> desConfig.baseUrl,
      "desAuthorizationToken" -> desConfig.authorisationToken, //it's test only endpoint, no worries
      "desServiceEnvironment" -> desConfig.serviceEnvironment,
      "saServicesUrl" -> saConnector.baseUrl
    ))
  }

}
