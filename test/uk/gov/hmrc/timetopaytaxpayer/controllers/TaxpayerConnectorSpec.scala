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

package uk.gov.hmrc.timetopaytaxpayer.controllers

import support._
import timetopaytaxpayer.cor.TaxpayerConnector
import timetopaytaxpayer.cor.model._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, Upstream5xxResponse}
import wiremockresponses.{DesWiremockResponses, SaWiremockResponses}

class TaxpayerConnectorSpec extends ItSpec {

  implicit val hc = HeaderCarrier()

  "getSelfAssessmentsAndDebits happy path" in {

    DesWiremockResponses.getDebits()
    DesWiremockResponses.getReturns()

    val taxpayerConnector = app.injector.instanceOf[TaxpayerConnector]

    val returnsAndDebits = taxpayerConnector.getReturnsAndDebits(TdAll.saUtr).futureValue
    returnsAndDebits shouldBe TdAll.returnsAndDebits
  }

  "getSelfAssessmentsAndDebits error case - getDebits fails" in {

    DesWiremockResponses.getDebits(response = "error", status = 500)
    DesWiremockResponses.getReturns()

    val taxpayerConnector = app.injector.instanceOf[TaxpayerConnector]

    val e: Throwable = taxpayerConnector.getReturnsAndDebits(TdAll.saUtr).failed.futureValue
    e shouldBe an[Upstream5xxResponse]
    e.getMessage shouldBe """GET of 'http://localhost:19001/taxpayer/returns-and-debits/3217334604' returned 502. Response body: '{"statusCode":502,"message":"GET of 'http://localhost:11111/sa/taxpayer/3217334604/debits' returned 500. Response body: 'error'"}'"""
  }

  "getSelfAssessmentsAndDebits error case - getReturns fails" in {

    DesWiremockResponses.getDebits()
    DesWiremockResponses.getReturns(response = "not found ", status = 404)

    val taxpayerConnector = app.injector.instanceOf[TaxpayerConnector]

    val e: Throwable = taxpayerConnector.getReturnsAndDebits(TdAll.saUtr).failed.futureValue
    e shouldBe an[NotFoundException]
    e.getMessage shouldBe """GET of 'http://localhost:19001/taxpayer/returns-and-debits/3217334604' returned 404 (Not Found). Response body: '{"statusCode":404,"message":"GET of 'http://localhost:11111/sa/taxpayer/3217334604/returns' returned 404 (Not Found). Response body: 'not found '"}'"""
  }
}
