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

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import com.google.inject.AbstractModule
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.{Application, Configuration}
import timetopaytaxpayer.cor.TaxpayerCorModule
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.ExecutionContext

/**
 * This is common spec for every test case which brings all of useful routines we want to use in our scenarios.
 */
trait ItSpec
  extends AnyFreeSpecLike
  with RichMatchers
  with BeforeAndAfterEach
  with GuiceOneServerPerTest
  with WireMockSupport {

  lazy val frozenZonedDateTime: ZonedDateTime = {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    LocalDateTime.parse("2018-11-02T16:28:55.185", formatter).atZone(ZoneId.of("Europe/London"))
  }

  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  lazy val overridingsModule = new AbstractModule {
    override def configure(): Unit = ()
  }
  lazy val servicesConfig = fakeApplication().injector.instanceOf[ServicesConfig]
  lazy val config = fakeApplication().injector.instanceOf[Configuration]
  val baseUrl: String = s"http://localhost:$WireMockSupport.port"

  override implicit val patienceConfig = PatienceConfig(
    timeout  = scaled(Span(3, Seconds)),
    interval = scaled(Span(300, Millis))
  )

  def httpClient = fakeApplication().injector.instanceOf[HttpClient]

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(GuiceableModule.fromGuiceModules(Seq(overridingsModule, new TaxpayerCorModule)))
    .configure(Map[String, Any](
      "microservice.services.des-services.port" -> WireMockSupport.port,
      "microservice.services.sa-services.port" -> WireMockSupport.port,

      "microservice.services.time-to-pay-taxpayer.port" -> port,
      "microservice.services.time-to-pay-taxpayer.host" -> "localhost"

    )).build()

}
