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
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences
import uk.gov.hmrc.play.health.AdminController
import uk.gov.hmrc.timetopayeligibility.controllers.EligibilityController
import uk.gov.hmrc.timetopayeligibility.debits.Debits
import uk.gov.hmrc.timetopayeligibility.debits.Debits.Debit
import uk.gov.hmrc.timetopayeligibility.infrastructure.HmrcEligibilityService
import uk.gov.hmrc.timetopayeligibility.returns.Returns
import uk.gov.hmrc.timetopayeligibility.returns.Returns.Return

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

  def hmrcWsCall[T] = HmrcEligibilityService.wsCall[T](wsClient, ApplicationConfig.desServicesUrl) _

  lazy val returns = hmrcWsCall[Seq[Return]](Returns.reader, utr => s"sa/taxpayer/${ utr.value }/returns")
  lazy val debits = hmrcWsCall[Seq[Debit]](Debits.reader, utr => s"sa/taxpayer/${ utr.value }/debits")
  lazy val preferences = hmrcWsCall[CommunicationPreferences](CommunicationPreferences.reader, utr => s"sa/taxpayer/${ utr.value }/communication-preferences")

  lazy val eligibilityController = new EligibilityController(returns, debits, preferences)

  lazy val metricsController = new MetricsController(new GraphiteMetricsImpl(applicationLifecycle, configuration))
  lazy val appRoutes = new app.Routes(httpErrorHandler,  new Provider[EligibilityController] {
    override def get(): EligibilityController = eligibilityController
  })
  lazy val healthRoutes = new manualdihealth.Routes(httpErrorHandler, new Provider[AdminController] {
    override def get(): AdminController = new AdminController(configuration)
  })

  override def router = {
    new Routes(httpErrorHandler, appRoutes, healthRoutes, new Provider[MetricsController] {
      override def get(): MetricsController = metricsController
    })
  }
}