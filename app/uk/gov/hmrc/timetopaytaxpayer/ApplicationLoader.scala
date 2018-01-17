/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.Provider

import com.kenshoo.play.metrics.MetricsController
import com.typesafe.config.ConfigFactory
import play.api.ApplicationLoader._
import play.api.libs.ws.ahc.AhcWSClient
import play.api.routing.Router
import play.api.{Application, BuiltInComponentsFromContext, Logger, LoggerConfigurator}
import prod.Routes
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.graphite.GraphiteMetricsImpl
import uk.gov.hmrc.timetopaytaxpayer.communication.preferences.CommunicationPreferences
import uk.gov.hmrc.play.health.AdminController
import uk.gov.hmrc.timetopaytaxpayer.ApplicationConfig.{desAuthorizationToken, desServiceEnvironment, desServicesUrl}
import uk.gov.hmrc.timetopaytaxpayer.controllers.{TaxPayerController, TestOnlyController}
import uk.gov.hmrc.timetopaytaxpayer.debits.Debits
import uk.gov.hmrc.timetopaytaxpayer.debits.Debits.Debit
import uk.gov.hmrc.timetopaytaxpayer.infrastructure.{DesService, WebService}
import uk.gov.hmrc.timetopaytaxpayer.returns.Returns
import uk.gov.hmrc.timetopaytaxpayer.returns.Returns.Return
import uk.gov.hmrc.timetopaytaxpayer.sa.SelfAssessmentService

class ApplicationLoader extends play.api.ApplicationLoader {
  def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }

    new ApplicationModule(context).application
  }
}

class ApplicationModule(context: Context) extends BuiltInComponentsFromContext(context) {

  import play.api.libs.concurrent.Execution.Implicits._

  lazy val wsClient = AhcWSClient()
  lazy val webServiceRequest = WebService.request(MicroserviceAuditConnector, Some(t => Logger.warn(t.getMessage, t))) _

  def hmrcWsCall[T] = DesService.wsCall[T](wsClient, webServiceRequest, desServicesUrl, desServiceEnvironment, desAuthorizationToken) _

  /**
    * Calls the 3 eligibility DES APIS; getSAReturns, getSADebits and getCommPreferences and stores their values for use in the controller response.
    */
  lazy val returns = hmrcWsCall[Seq[Return]](Returns.reader, utr => s"sa/taxpayer/${ utr.value }/returns")
  lazy val debits = hmrcWsCall[Seq[Debit]](Debits.reader, utr => s"sa/taxpayer/${ utr.value }/debits")
  lazy val preferences = hmrcWsCall[CommunicationPreferences](CommunicationPreferences.reader, utr => s"sa/taxpayer/${ utr.value }/communication-preferences")

  /**
    * Calls the SA service and returns the user's address and other personal details such as name, surname and title.
    */
  lazy val saService = SelfAssessmentService.address(wsClient, webServiceRequest, ApplicationConfig.saServicesUrl)(utr=> s"sa/individual/${ utr.value }/designatory-details/taxpayer") _

  lazy val eligibilityController = new TaxPayerController(debits, preferences, returns, saService)

  lazy val testOnlyController = new TestOnlyController()

  lazy val metricsController = new MetricsController(new GraphiteMetricsImpl(applicationLifecycle, configuration))
  lazy val appRoutes = new app.Routes(httpErrorHandler,  new Provider[TaxPayerController] {
    override def get(): TaxPayerController = eligibilityController
  })
  lazy val healthRoutes = new manualdihealth.Routes(httpErrorHandler, new Provider[AdminController] {
    override def get(): AdminController = new AdminController(configuration)
  })


  lazy val prodRoutes = new Routes(httpErrorHandler, appRoutes, healthRoutes, new Provider[MetricsController] {
    override def get(): MetricsController = metricsController
  })

  lazy val testOnlyDoNotUseInAppConfRoutes: testOnlyDoNotUseInProd.Routes = new testOnlyDoNotUseInProd.Routes(
    httpErrorHandler,
    prodRoutes,
    new Provider[TestOnlyController] {
      override def get(): TestOnlyController = testOnlyController
    }
  )

  override def router: Router = {
    val applicationRouterKey = "application.router"

    val config = ConfigFactory.load()

    val applicationRouterProp = config.getString(applicationRouterKey)
    if (applicationRouterProp == null) {
      Logger.info("Using router with prod.routes")
      prodRoutes
    }

    if (applicationRouterProp == "testOnlyDoNotUseInProd.routes") {
      Logger.info("Using router with testOnlyDoNotUseInProd.routes")
      testOnlyDoNotUseInAppConfRoutes
    } else {
      Logger.error(s"The option $applicationRouterKey has unsupported value: $applicationRouterProp. We support only 'testOnlyDoNotUseInProd.routes'. Using 'prodRoutes'.")
      prodRoutes
    }
  }


}
