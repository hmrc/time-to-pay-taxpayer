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

package uk.gov.hmrc.timetopaytaxpayer.controllers

import javax.inject._
import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.timetopaytaxpayer.Config.ApplicationConfig

class TestOnlyController @Inject() (applicationConfig: ApplicationConfig, cc: ControllerComponents)
  extends BackendController(cc) {

  def config() = cc.actionBuilder { r =>
    val result: JsValue = Json.parse(
      ConfigFactory.load().root().render(ConfigRenderOptions.concise())
    )
    Results.Ok(result)
  }

  def connectorsConfig() = cc.actionBuilder { r =>
    Ok(Json.obj(
      "desServicesUrl" -> applicationConfig.desServicesUrl,
      "desAuthorizationToken" -> applicationConfig.desAuthorizationToken,
      "desServiceEnvironment" -> applicationConfig.desServiceEnvironment,
      "saServicesUrl" -> applicationConfig.saServicesUrl
    ))
  }

}
