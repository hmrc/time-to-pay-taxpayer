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
import uk.gov.hmrc.timetopayeligibility.{Fixtures, Utr}
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences._
import uk.gov.hmrc.timetopayeligibility.debits.Debits.{Charge, Debit, DebitsResult, Interest}
import uk.gov.hmrc.timetopayeligibility.infrastructure.DesService.{DesServiceError, DesUserNotFoundError}
import uk.gov.hmrc.timetopayeligibility.sa.DesignatoryDetails.Individual
import uk.gov.hmrc.timetopayeligibility.sa.SelfAssessmentService._
import uk.gov.hmrc.timetopayeligibility.taxpayer.Address

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxPayerControllerSpec extends UnitSpec with ScalaFutures {

  override implicit val patienceConfig = PatienceConfig(timeout = Span(1, Second))

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def createdController(debitsService: (Utr => Future[DebitsResult]) = _ => Future.successful(Right(Seq.empty)),
                 preferencesService: (Utr => Future[CommunicationPreferencesResult]) = _ => Future.successful(Right(Fixtures.someCommunicationPreferences())),
                 saService: (Utr => Future[SaServiceResult]) = _ => Future.successful(Right(Fixtures.somePerson()))) = {

    new TaxPayerController(debitsService, preferencesService, saService)
  }

  "tax payer controller" should {

    "produce a valid tax payer" in {
      val debitResult: DebitsResult = Right(Seq(Debit(
        charge = Charge(originCode = "IN2",
        creationDate = LocalDate.of(2013, 7, 31)),
        relevantDueDate = LocalDate.of(2016, 1, 31),
        taxYearEnd = LocalDate.of(2015, 4, 1),
        totalOutstanding = 250.52,
        interest = Some(Interest(Some(LocalDate.of(2016, 6, 1)), 42.32))
      )))

      val preferencesResult = Right(CommunicationPreferences( welshLanguageIndicator = true, audioIndicator = true,
        largePrintIndicator = true, brailleIndicator = true))

      val saResult = Right(Individual(Fixtures.someIndividual(),
        Address("321 Fake Street", "Worthing", "West Sussex", "Another Line", "One More Line", "BN3 2GH")))

      val controller = createdController(
        (utr) => Future.successful(debitResult),
        (utr) => Future.successful(preferencesResult),
        (utr) => Future.successful(saResult))


      val json = jsonBodyOf(controller.getTaxPayer("1234567890").apply(FakeRequest()).futureValue)

      val expectedJson = Json.parse(
        """
          |{
          |   "customerName": "President Donald Trump",
          |   "addresses": [
          |           {
          |             "addressLine1": "321 Fake Street",
          |             "addressLine2": "Worthing",
          |             "addressLine3": "West Sussex",
          |             "addressLine4": "Another Line",
          |             "addressLine5": "One More Line",
          |             "postcode": "BN3 2GH"
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
          |          "amount": 250.52,
          |          "dueDate": "2016-01-31",
          |          "interest": {
          |             "calculationDate" : "2016-06-01",
          |             "amountAccrued" : 42.32
          |          }
          |        }
          |      ]
          |   }
          |}""".stripMargin)

      json shouldBe expectedJson
    }

    "fail with a 500 if a downstream service is not successful" in {
      val debitResult: DebitsResult = Left(DesServiceError("Foo"))

      val controller = createdController(debitsService = (utr) => Future.successful(debitResult))

      val result = controller.getTaxPayer(Fixtures.someUtr.value).apply(FakeRequest()).futureValue

      result.header.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "fail with 404 if downstream service does not know about user" in {
      val utr = Fixtures.someUtr
      val debitResult = Left(DesUserNotFoundError(utr))

      val controller = createdController(debitsService = (utr) => Future.successful(debitResult))
      val result = controller.getTaxPayer(utr.value).apply(FakeRequest()).futureValue

      result.header.status shouldBe Status.NOT_FOUND
    }
  }

}
