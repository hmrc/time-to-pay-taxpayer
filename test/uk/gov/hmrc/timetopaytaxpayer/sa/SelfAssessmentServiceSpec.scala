/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.timetopaytaxpayer.sa

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import play.api.http.Status
import play.api.libs.ws.ahc.AhcWSClient
import uk.gov.hmrc.timetopaytaxpayer.sa.DesignatoryDetails.{Individual, Name}
import uk.gov.hmrc.timetopaytaxpayer.sa.SelfAssessmentService.SaUnauthorizedError
import uk.gov.hmrc.timetopaytaxpayer.taxpayer.Address
import uk.gov.hmrc.timetopaytaxpayer.{AuthorizedUser, Fixtures, Utr}
import cats.implicits._

import scala.concurrent.ExecutionContext

class SelfAssessmentServiceSpec extends WordSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  lazy val server = new WireMockServer(wireMockConfig().dynamicPort())

  final lazy val serverUrl = s"http://localhost:${ server.port() }"

  case class SimpleJson(utr: String)

  implicit val executionContext = ExecutionContext.Implicits.global

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val service = SelfAssessmentService.address(AhcWSClient(), _.execute(), serverUrl)(_.value) _

  val successfulUtr = Fixtures.someUtr

  val authorizedUser = AuthorizedUser("dave.clifton")
  val unauthorizedUser = AuthorizedUser("tony.hayers")

  override def beforeAll() = {
    super.beforeAll()
    server.start()

    addMapping(successfulUtr, Status.OK, Some("""{
                                   |  "name": {
                                   |    "title": "President",
                                   |    "forename": "Donald",
                                   |    "surname": "Trump",
                                   |    "honours": "KCBE"
                                   |  },
                                   |  "address": {
                                   |    "addressLine1": "75 King's Street",
                                   |    "addressLine2": "Stamford Street",
                                   |    "addressLine3": "London",
                                   |    "addressLine4": "Greater London",
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

    addMapping(successfulUtr, Status.UNAUTHORIZED, authorizedUserHeaderValue = unauthorizedUser)
  }

  def addMapping(utr: Utr, statusCode: Int, body: Option[String] = None, authorizedUserHeaderValue: AuthorizedUser = authorizedUser) = {
    server.addStubMapping(get(urlPathMatching(s"/${ utr.value }"))
      .withHeader("Authorization", equalTo(authorizedUserHeaderValue.value))
      .willReturn(
        aResponse()
          .withBody(body.getOrElse(""))
          .withStatus(statusCode))
      .build())
  }

  override def afterAll() = {
    super.afterAll()
    server.stop()
  }

  override implicit val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  "sa service" should {
    "handle valid responses" in {
      service(successfulUtr, authorizedUser).futureValue shouldBe Right(Individual(
        Name("President".some, "Donald".some, none, "Trump"),
        Address(
          "75 King's Street".some,
          "Stamford Street".some,
          "London".some,
          "Greater London".some,
          none,
          "WC2H 9Dl".some
        )
      ))
    }

    "handle unauthorized users" in {
      service(successfulUtr, unauthorizedUser).futureValue shouldBe Left(SaUnauthorizedError(successfulUtr, unauthorizedUser))
    }
  }
}
