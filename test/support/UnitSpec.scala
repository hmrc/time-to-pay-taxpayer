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

package support

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

import com.google.inject.AbstractModule
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, FreeSpecLike}
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.{Application, Configuration}
import timetopaytaxpayer.cor.TaxpayerCorModule
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext

/**
 * This is common spec for every test case which brings all of useful routines we want to use in our scenarios.
 */
trait UnitSpec
  extends FreeSpecLike
  with RichMatchers
