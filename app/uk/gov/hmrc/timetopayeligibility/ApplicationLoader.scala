/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.timetopayeligibility


import javax.inject.Provider

import com.kenshoo.play.metrics.MetricsController
import play.api.ApplicationLoader._
import play.api.libs.ws.ahc.AhcWSClient
import play.api.{BuiltInComponentsFromContext, LoggerConfigurator}
import prod.Routes
import uk.gov.hmrc.play.graphite.GraphiteMetricsImpl
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferencesService
import uk.gov.hmrc.timetopayeligibility.controllers.EligibilityController
import uk.gov.hmrc.timetopayeligibility.debits.DebitsService
import uk.gov.hmrc.timetopayeligibility.returns.ReturnsService

class ApplicationLoader extends play.api.ApplicationLoader {
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }
    new ApplicationModule(context).application
  }
}

class ApplicationModule(context: Context) extends BuiltInComponentsFromContext(context) {

  import play.api.libs.concurrent.Execution.Implicits._

  lazy val wsClient = AhcWSClient()
  lazy val returns = ReturnsService.returns(ReturnsService.returnsWsCall(wsClient, ApplicationConfig.desServicesUrl)) _
  lazy val debits = DebitsService.debits(DebitsService.debitsWsCall(wsClient, ApplicationConfig.desServicesUrl)) _
  lazy val preferences = CommunicationPreferencesService.preferences(CommunicationPreferencesService.wsCall(wsClient, ApplicationConfig.desServicesUrl)) _
  lazy val eligibilityController = new EligibilityController(returns, debits, preferences)
  lazy val metricsController = new MetricsController(new GraphiteMetricsImpl(applicationLifecycle, configuration))
  lazy val appRoutes = new app.Routes(httpErrorHandler,  new Provider[EligibilityController] {
    override def get(): EligibilityController = eligibilityController
  })

  override def router = {
    new Routes(httpErrorHandler, appRoutes, health.Routes, new Provider[MetricsController] {
      override def get(): MetricsController = metricsController
    })
  }
}