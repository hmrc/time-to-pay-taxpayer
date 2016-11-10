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

package uk.gov.hmrc.timetopayeligibility.controllers

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Second, Span}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.mvc.Http.Status
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.timetopayeligibility.Fixtures
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences
import uk.gov.hmrc.timetopayeligibility.debits.Debits.{Charge, Debit, DebitsResult}
import uk.gov.hmrc.timetopayeligibility.infrastructure.DesService.{DesServiceError, DesUserNotFoundError}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxPayerControllerSpec extends UnitSpec with ScalaFutures {

  override implicit val patienceConfig = PatienceConfig(timeout = Span(1, Second))

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  "tax payer controller" should {

    "produce a valid tax payer" in {

      val debitResult: DebitsResult = Right(Seq(Debit(
        charge = Charge(originCode = "IN2",
        creationDate = LocalDate.of(2013, 7, 31)),
        relevantDueDate = LocalDate.of(2014, 7, 31),
        taxYearEnd = LocalDate.of(2015, 4, 1),
        totalOutstanding = 0,
        interest = None )))

      val preferencesResult = Right(CommunicationPreferences( welshLanguageIndicator = true, audioIndicator = true,
        largePrintIndicator = true, brailleIndicator = true))

      val controller = new TaxPayerController(
        (utr) => Future.successful(debitResult),
        (utr) => Future.successful(preferencesResult) )


      val json = jsonBodyOf(controller.getTaxPayer("1234567890").apply(FakeRequest()).futureValue)

      val expectedJson = Json.parse(
        """
          |{
          |   "customerName": "Customer name",
          |   "addresses": [
          |           {
          |             "addressLine1": "123 Fake Street",
          |             "addressLine2": "Foo",
          |             "addressLine3": "Bar",
          |             "postCode": "BN3 2GH"
          |           }
          |         ],
          |    "selfAssessment": {
          |      "utr": "1234567890",
          |      "communicationPreferences": {
          |        "welshLanguageIndicator": true,
          |        "audioIndicator": true,
          |        "largePrintIndicator": true,
          |        "brailleIndicator": true
          |      },
          |      "debits": [
          |        {
          |          "originCode": "IN2",
          |          "dueDate": "2014-07-31"
          |        }
          |      ]
          |   }
          |}""".stripMargin)

      json shouldBe expectedJson
    }

    "fail with a 500 if a downstream service is not successful" in {
      val debitResult: DebitsResult = Left(DesServiceError("Foo"))

      val controller = new TaxPayerController(
        (utr) => Future.successful(debitResult),
        (utr) => Future.successful(Right(Fixtures.someCommunicationPreferences())) )

      val result = controller.getTaxPayer(Fixtures.someUtr.value).apply(FakeRequest()).futureValue

      result.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "fail with 404 if downstream service does not know about user" in {
      val utr = Fixtures.someUtr
      val debitResult = Left(DesUserNotFoundError(utr))

      val controller = new TaxPayerController(
        (utr) => Future.successful(debitResult),
        (utr) => Future.successful(Right(Fixtures.someCommunicationPreferences()))
      )

      val result = controller.getTaxPayer(utr.value).apply(FakeRequest()).futureValue

      result.header.status shouldBe Status.NOT_FOUND
    }
  }

}
