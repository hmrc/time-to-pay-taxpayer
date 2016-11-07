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

package uk.gov.hmrc.timetopayeligibility.debits

import akka.util.ByteString
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Second, Span}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSCookie, WSResponse}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.timetopayeligibility.debits.DebitsService.{DebitsServiceError, DebitsUserNotFound}
import uk.gov.hmrc.timetopayeligibility.{Fixtures, Utr}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

class DebitsServiceSpec extends UnitSpec with ScalaFutures {

  implicit val executionContext = ExecutionContext.Implicits.global

  override implicit val patienceConfig = PatienceConfig(timeout = Span(1, Second))

  "debits service" should {

    val uniqueUtrs = Fixtures.uniqueUtrs(4)
    val successfulUtr = uniqueUtrs.head
    val badJsonUtr = uniqueUtrs(1)
    val unknownUtr = uniqueUtrs(2)
    val serverErrorUtr = uniqueUtrs(3)

    val call: (Utr) => Future[WSResponse] = {
      case `successfulUtr` => Future.successful(stubResponse(200))
      case `badJsonUtr` => Future.successful(stubResponse(200, jsonBody = Json.parse("""{ "cheese":[] }""")))
      case `unknownUtr` => Future.successful(stubResponse(404))
      case `serverErrorUtr` => Future.successful(stubResponse(500, statusTextValue = "apple"))
    }

    "handle successful call from ws" in {
      DebitsService.debits(call)(successfulUtr).futureValue shouldBe Right(Seq.empty)
    }

    "handle call from ws with dodgy JSON" in {
      DebitsService.debits(call)(badJsonUtr).futureValue.left.get shouldBe a [DebitsServiceError]
    }

    "handle call for unknown UTR" in {
      DebitsService.debits(call)(unknownUtr).futureValue shouldBe Left(DebitsUserNotFound(unknownUtr))
    }

    "handle call when error downstream" in {
      DebitsService.debits(call)(serverErrorUtr).futureValue shouldBe Left(DebitsServiceError("apple"))
    }
  }

  def stubResponse(statusCode: Int, jsonBody: JsValue = Json.parse("""{ "debits":[] }"""), statusTextValue: String = ""): WSResponse  = {
    new WSResponse {
      override def statusText: String = statusTextValue

      override def underlying[T]: T = ???

      override def xml: Elem = ???

      override def body: String = ???

      override def header(key: String): Option[String] = ???

      override def cookie(name: String): Option[WSCookie] = ???

      override def bodyAsBytes: ByteString = ???

      override def cookies: Seq[WSCookie] = ???

      override def status: Int = statusCode

      override def json: JsValue = jsonBody

      override def allHeaders: Map[String, Seq[String]] = ???
    }
  }
}
