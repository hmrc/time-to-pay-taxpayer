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

package support

import com.google.inject.{AbstractModule, Provides, Singleton}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.{DefaultTestServerFactory, RunningServer}
import play.api.{Application, Configuration, Mode}
import play.core.server.ServerConfig
import timetopaytaxpayer.cor.{TaxpayerConnector, TaxpayerCorModule}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDateTime, ZoneId, ZonedDateTime}
import scala.concurrent.ExecutionContext

/**
 * This is common spec for every test case which brings all of useful routines we want to use in our scenarios.
 */
trait ItSpec
  extends AnyFreeSpecLike
  with RichMatchers
  with BeforeAndAfterEach
  with GuiceOneServerPerSuite
  with WireMockSupport {

  val testServerPort = 19001

  lazy val frozenZonedDateTime: ZonedDateTime = {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    LocalDateTime.parse("2023-11-02T16:28:55.185", formatter).atZone(ZoneId.of("Europe/London"))
  }

  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  lazy val overridingsModule = new AbstractModule {
    override def configure(): Unit = ()

    @Provides
    @Singleton
    def clock(): Clock = Clock.fixed(frozenZonedDateTime.toInstant, frozenZonedDateTime.getZone)
  }
  lazy val servicesConfig = fakeApplication().injector.instanceOf[ServicesConfig]
  lazy val config = fakeApplication().injector.instanceOf[Configuration]
  val baseUrl: String = s"http://localhost:$WireMockSupport.port"

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout  = scaled(Span(3, Seconds)),
    interval = scaled(Span(300, Millis))
  )

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(GuiceableModule.fromGuiceModules(Seq(overridingsModule, new TaxpayerCorModule)))
    .configure(Map[String, Any](
      "microservice.services.des-services.port" -> WireMockSupport.port,
      "microservice.services.sa-services.port" -> WireMockSupport.port,
      "microservice.services.auth.port" -> WireMockSupport.port,
      "microservice.services.time-to-pay-taxpayer.port" -> testServerPort,
      "microservice.services.time-to-pay-taxpayer.host" -> "localhost"

    )).build()

  object TestServerFactory extends DefaultTestServerFactory {
    override protected def serverConfig(app: Application): ServerConfig = {
      val sc = ServerConfig(port    = Some(testServerPort), sslPort = None, mode = Mode.Test, rootDir = app.path)
      sc.copy(configuration = sc.configuration.withFallback(overrideServerConfiguration(app)))
    }
  }

  override implicit protected lazy val runningServer: RunningServer =
    TestServerFactory.start(app)

}
