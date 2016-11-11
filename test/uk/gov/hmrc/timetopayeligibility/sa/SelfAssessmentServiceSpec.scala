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

package uk.gov.hmrc.timetopayeligibility.sa

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Second, Span}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.ahc.AhcWSClient
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.timetopayeligibility.{Fixtures, Utr}
import uk.gov.hmrc.timetopayeligibility.infrastructure.DesService
import uk.gov.hmrc.timetopayeligibility.taxpayer.Address

import scala.concurrent.ExecutionContext

class SelfAssessmentServiceSpec extends UnitSpec with BeforeAndAfterAll with ScalaFutures {

  lazy val server = new WireMockServer(wireMockConfig().dynamicPort())

  final lazy val serverUrl = s"http://localhost:${ server.port() }"

  case class SimpleJson(utr: String)

  implicit val executionContext = ExecutionContext.Implicits.global

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val service = SelfAssessmentService.address(AhcWSClient(), serverUrl)(_.value) _

  val uniqueUtrs = Fixtures.uniqueUtrs(4)
  val successfulUtr = uniqueUtrs.head


  override def beforeAll() = {
    super.beforeAll()
    server.start()

    addMapping(successfulUtr, Status.OK, Some("""{
                                   |  "name": {
                                   |    "title": "Mr",
                                   |    "forename": "Robert",
                                   |    "secondForename": "Arthur",
                                   |    "surname": "Builder",
                                   |    "honours": "KCBE"
                                   |  },
                                   |  "address": {
                                   |    "addressLine1": "75 King's Street",
                                   |    "addressLine2": "Stamford Street",
                                   |    "addressLine3": "London",
                                   |    "addressLine4": "Greater London",
                                   |    "addressLine5": "",
                                   |    "postcode": "WC2H 9Dl",
                                   |    "additionalDeliveryInformation": "Leave by door"
                                   |  },
                                   |  "contact": {
                                   |     "telephone": {
                                   |       "daytime": "02765760#1235",
                                   |       "evening": "027657630",
                                   |       "mobile": "07897658",
                                   |       "fax": "0208875765"
                                   |     },
                                   |     "email": {
                                   |       "primary": "bob@notreal.com"
                                   |     }
                                   |  }
                                   |}""".stripMargin))
  }

  def addMapping(utr: Utr, statusCode: Int, body: Option[String] = None) = {
    server.addStubMapping(get(urlPathMatching(s"/${ utr.value }"))
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

  "sa service" should {
    "handle valid responses" in {
      service(successfulUtr).futureValue shouldBe Right(Address("75 King's Street", "Stamford Street",
        "London", "Greater London", "", "WC2H 9Dl"))
    }
  }

}
