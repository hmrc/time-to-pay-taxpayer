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

package timetopaytaxpayer

import java.time.LocalDate

import support.TdAll.saUtr
import support._
import timetopaytaxpayer.cor.TaxpayerConnector
import timetopaytaxpayer.cor.model._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, Upstream5xxResponse}
import wiremockresponses.{DesWiremockResponses, SaWiremockResponses}

// todo - remove as part of OPS-4581
class TaxpayerControllerSpec extends ItSpec {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val date20190225 = LocalDate.of(2019, 2, 25)
  private val date20190405 = LocalDate.of(2019, 4, 5)
  private val date20190131 = LocalDate.of(2019, 1, 31)

  private val taxpayer = Taxpayer(
    "Mr Lester Corncrake",
    Seq(Address(Some("123 Any Street"), Some("Kingsland High Road"), Some("E8 3PP"))),
    SelfAssessmentDetails(
      saUtr,
      CommunicationPreferences(
        welshLanguageIndicator = true, audioIndicator = false, largePrintIndicator = false, brailleIndicator = false
      ),
      Seq(Debit("IN1", 2500, date20190225, None, date20190405), Debit("IN2", 2500, date20190225, None, date20190405)),
      Seq(
        Return(date20190405, None, Some(date20190131), None),
        Return(
          LocalDate.of(2018, 4, 5),
          None,
          Some(LocalDate.of(2018, 1, 31)),
          Some(LocalDate.of(2018, 3, 9))
        )
      )
    )
  )

  "should get a 200 with an authorization header" in {
    DesWiremockResponses.getDebits()
    DesWiremockResponses.getReturns()
    DesWiremockResponses.getCommunicationPreferences()
    SaWiremockResponses.getIndividual()

    val taxpayerConnector = app.injector.instanceOf[TaxpayerConnector]

    val taxpayer: Taxpayer = taxpayerConnector.getTaxPayer(saUtr).futureValue
    taxpayer shouldBe taxpayer
  }

  "error case - getDebits fails" in {
    DesWiremockResponses.getDebits(response = "error", status = 500)
    DesWiremockResponses.getReturns()
    DesWiremockResponses.getCommunicationPreferences()
    SaWiremockResponses.getIndividual()

    val taxpayerConnector = app.injector.instanceOf[TaxpayerConnector]

    val e: Throwable = taxpayerConnector.getTaxPayer(saUtr).failed.futureValue
    e shouldBe an[Upstream5xxResponse]
    e.getMessage shouldBe """GET of 'http://localhost:19001/taxpayer/3217334604' returned 502. Response body: '{"statusCode":502,"message":"GET of 'http://localhost:11111/sa/taxpayer/3217334604/debits' returned 500. Response body: 'error'"}'"""
  }

  "error case - getReturns fails" in {
    DesWiremockResponses.getDebits()
    DesWiremockResponses.getReturns(response = "not found ", status = 404)
    DesWiremockResponses.getCommunicationPreferences()
    SaWiremockResponses.getIndividual()

    val taxpayerConnector = app.injector.instanceOf[TaxpayerConnector]

    val e: Throwable = taxpayerConnector.getTaxPayer(saUtr).failed.futureValue
    e shouldBe an[NotFoundException]
    e.getMessage shouldBe """GET of 'http://localhost:19001/taxpayer/3217334604' returned 404 (Not Found). Response body: '{"statusCode":404,"message":"GET of 'http://localhost:11111/sa/taxpayer/3217334604/returns' returned 404 (Not Found). Response body: 'not found '"}'"""
  }

  "error case - getCommunicationPreferences fails" in {
    DesWiremockResponses.getDebits()
    DesWiremockResponses.getReturns()
    DesWiremockResponses.getCommunicationPreferences(response = "some error", status = 500)
    SaWiremockResponses.getIndividual()

    val taxpayerConnector = app.injector.instanceOf[TaxpayerConnector]

    val e: Throwable = taxpayerConnector.getTaxPayer(saUtr).failed.futureValue
    e shouldBe an[Upstream5xxResponse]
    e.getMessage shouldBe """GET of 'http://localhost:19001/taxpayer/3217334604' returned 502. Response body: '{"statusCode":502,"message":"GET of 'http://localhost:11111/sa/taxpayer/3217334604/communication-preferences' returned 500. Response body: 'some error'"}'"""
  }

  "error case - getIndividual fails" in {
    DesWiremockResponses.getDebits()
    DesWiremockResponses.getReturns()
    DesWiremockResponses.getCommunicationPreferences()
    SaWiremockResponses.getIndividual(response = "some error", status = 500)

    val taxpayerConnector = app.injector.instanceOf[TaxpayerConnector]

    val e: Throwable = taxpayerConnector.getTaxPayer(saUtr).failed.futureValue
    e shouldBe an[Upstream5xxResponse]
    e.getMessage shouldBe """GET of 'http://localhost:19001/taxpayer/3217334604' returned 502. Response body: '{"statusCode":502,"message":"GET of 'http://localhost:11111/sa/individual/3217334604/designatory-details/taxpayer' returned 500. Response body: 'some error'"}'"""
  }
}

