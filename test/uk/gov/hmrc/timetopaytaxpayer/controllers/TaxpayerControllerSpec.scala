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

package uk.gov.hmrc.timetopaytaxpayer.controllers

import play.api.http.Status.UNAUTHORIZED
import support.TdAll.saUtr
import support._
import timetopaytaxpayer.cor.TaxpayerConnector
import timetopaytaxpayer.cor.model._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, UpstreamErrorResponse}
import wiremockresponses.{AuthWiremockResponses, DesWiremockResponses, SaWiremockResponses}

import java.time.LocalDate

class TaxpayerControllerSpec extends ItSpec {
  implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))

  private val date20190225 = LocalDate.of(2019, 2, 25)
  private val date20190405 = LocalDate.of(2019, 4, 5)
  private val date20190131 = LocalDate.of(2019, 1, 31)

  private val expectedTaxpayer = Taxpayer(
    "Mr Lester Corncrake",
    Vector(Address(Some("123 Any Street"), Some("Kingsland High Road"), Some("Dalston"), Some("Greater London"), Some(""), Some("E8 3PP"))),
    SelfAssessmentDetails(
      saUtr,
      CommunicationPreferences(
        welshLanguageIndicator = true, audioIndicator = false, largePrintIndicator = false, brailleIndicator = false
      ),
      Vector(Debit("IN1", 2500, date20190225, None, date20190405), Debit("IN2", 2500, date20190225, None, date20190405)),
      Vector(
        Return(date20190405, None, Some(date20190131), None)
      )
    )
  )

  lazy val taxpayerConnector = app.injector.instanceOf[TaxpayerConnector]

  "should get a 200 with an authorization header" in {
    AuthWiremockResponses.authorise()
    DesWiremockResponses.getDebits()
    DesWiremockResponses.getReturns()
    DesWiremockResponses.getCommunicationPreferences()
    SaWiremockResponses.getIndividual()

    val taxpayer: Option[Taxpayer] = taxpayerConnector.getTaxPayer(saUtr).futureValue
    taxpayer shouldBe Some(expectedTaxpayer)

    AuthWiremockResponses.ensureAuthoriseCalled()
  }

  "error case - no auth token" in {
    val e: Throwable = taxpayerConnector.getTaxPayer(saUtr)(HeaderCarrier()).failed.futureValue

    e match {
      case u: UpstreamErrorResponse =>
        u.statusCode shouldBe UNAUTHORIZED

      case other =>
        fail(s"Expected UpstreamErrorResponse but got ${other.toString}")
    }
  }

  "error case - getDebits fails" in {
    AuthWiremockResponses.authorise()
    DesWiremockResponses.getDebits(response = "error", status = 500)
    DesWiremockResponses.getReturns()
    DesWiremockResponses.getCommunicationPreferences()
    SaWiremockResponses.getIndividual()

    val e: Throwable = taxpayerConnector.getTaxPayer(saUtr).failed.futureValue
    e shouldBe an[UpstreamErrorResponse]
    e.getMessage shouldBe s"""GET of 'http://localhost:${testServerPort.toString}/taxpayer/3217334604' returned 502. Response body: '{"statusCode":502,"message":"GET of 'http://localhost:11111/sa/taxpayer/3217334604/debits' returned 500. Response body: 'error'"}'"""

    AuthWiremockResponses.ensureAuthoriseCalled()
  }

  "error case - getReturns fails" in {
    AuthWiremockResponses.authorise()
    DesWiremockResponses.getDebits()
    DesWiremockResponses.getReturns(response = "not found ", status = 404)
    DesWiremockResponses.getCommunicationPreferences()
    SaWiremockResponses.getIndividual()

    val e: Throwable = taxpayerConnector.getTaxPayer(saUtr).failed.futureValue
    e shouldBe an[UpstreamErrorResponse]
    e.getMessage shouldBe s"""GET of 'http://localhost:${testServerPort.toString}/taxpayer/3217334604' returned 500. Response body: '{"statusCode":500,"message":"GET of 'http://localhost:11111/sa/taxpayer/3217334604/returns' returned 404. Response body: 'not found '"}'"""

    AuthWiremockResponses.ensureAuthoriseCalled()
  }

  "error case - getCommunicationPreferences fails" in {
    AuthWiremockResponses.authorise()
    DesWiremockResponses.getDebits()
    DesWiremockResponses.getReturns()
    DesWiremockResponses.getCommunicationPreferences(response = "some error", status = 500)
    SaWiremockResponses.getIndividual()

    val e: Throwable = taxpayerConnector.getTaxPayer(saUtr).failed.futureValue
    e shouldBe an[UpstreamErrorResponse]
    e.getMessage shouldBe s"""GET of 'http://localhost:${testServerPort.toString}/taxpayer/3217334604' returned 502. Response body: '{"statusCode":502,"message":"GET of 'http://localhost:11111/sa/taxpayer/3217334604/communication-preferences' returned 500. Response body: 'some error'"}'"""

    AuthWiremockResponses.ensureAuthoriseCalled()
  }

  "error case - getIndividual fails (not a 404)" in {
    AuthWiremockResponses.authorise()
    DesWiremockResponses.getDebits()
    DesWiremockResponses.getReturns()
    DesWiremockResponses.getCommunicationPreferences()
    SaWiremockResponses.getIndividual(response = "some error", status = 500)

    val e: Throwable = taxpayerConnector.getTaxPayer(saUtr).failed.futureValue
    e shouldBe an[UpstreamErrorResponse]
    e.getMessage shouldBe s"""GET of 'http://localhost:${testServerPort.toString}/taxpayer/3217334604' returned 502. Response body: '{"statusCode":502,"message":"GET of 'http://localhost:11111/sa/individual/3217334604/designatory-details/taxpayer' returned 500. Response body: 'some error'"}'"""

    AuthWiremockResponses.ensureAuthoriseCalled()
  }

  "error case - getIndividual fails with a 404" in {
    AuthWiremockResponses.authorise()
    SaWiremockResponses.getIndividual(response = "some error", status = 404)

    taxpayerConnector.getTaxPayer(saUtr).futureValue shouldBe None

    AuthWiremockResponses.ensureAuthoriseCalled()
  }

}
