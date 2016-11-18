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

package uk.gov.hmrc.timetopaytaxpayer.infrastructure

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Second, Span}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.ahc.AhcWSClient
import uk.gov.hmrc.play.http.HeaderNames
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.timetopaytaxpayer.infrastructure.DesService.{DesServiceError, DesUnauthorizedError, DesUserNotFoundError}
import uk.gov.hmrc.timetopaytaxpayer.{AuthorizedUser, Fixtures, Utr}

import scala.concurrent.ExecutionContext

class DesServiceSpec extends UnitSpec with BeforeAndAfterAll with ScalaFutures {

  lazy val server = new WireMockServer(wireMockConfig().dynamicPort())

  final lazy val serverUrl = s"http://localhost:${ server.port() }"

  case class SimpleJson(utr: String)

  implicit val executionContext = ExecutionContext.Implicits.global

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val serviceEnvironment = "someEnvironment"
  val authorizationToken = "someToken"
  val authorizationHeader = "Bearer someToken"
  val service = DesService.wsCall(AhcWSClient(), _.execute, serverUrl, serviceEnvironment, authorizationToken)(Json.reads[SimpleJson], _.value) _

  val uniqueUtrs = Fixtures.uniqueUtrs(4)
  val successfulUtr = uniqueUtrs.head
  val badJsonUtr = uniqueUtrs(1)
  val unknownUtr = uniqueUtrs(2)
  val serverErrorUtr = uniqueUtrs(3)

  override def beforeAll() = {
    super.beforeAll()
    server.start()

    addMapping(successfulUtr, Status.OK)
    addMapping(badJsonUtr, Status.OK, Some("""{"cheese":"cake"}"""))
    addMapping(unknownUtr, Status.NOT_FOUND)
    addMapping(serverErrorUtr, Status.INTERNAL_SERVER_ERROR, Some("""{"reason":"foo"}"""))
  }

  def addMapping(utr: Utr, statusCode: Int, body: Option[String] = None) = {
    server.addStubMapping(get(urlPathMatching(s"/${ utr.value }"))
      .withHeader("environment", equalTo(serviceEnvironment))
      .withHeader(HeaderNames.authorisation, equalTo(authorizationHeader))
      .willReturn(
        aResponse()
          .withBody(body.getOrElse(s"""{"utr":"${ utr.value }"}"""))
          .withStatus(statusCode))
      .build())
  }

  override def afterAll() = {
    super.afterAll()
    server.stop()
  }

  override implicit val patienceConfig = PatienceConfig(timeout = Span(1, Second))

  "des service" should {
    "handle valid responses" in {
      service(successfulUtr).futureValue shouldBe Right(SimpleJson(successfulUtr.value))
    }

    "handle call from ws with dodgy JSON" in {
      service(badJsonUtr).futureValue.left.get shouldBe a[DesServiceError]
    }

    "handle call for unknown UTR" in {
      service(unknownUtr).futureValue shouldBe Left(DesUserNotFoundError(unknownUtr))
    }

    "handle call when error downstream" in {
      service(serverErrorUtr).futureValue shouldBe Left(DesServiceError("foo"))
    }
  }
}
