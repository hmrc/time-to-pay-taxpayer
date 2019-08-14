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

package uk.gov.hmrc.timetopaytaxpayer.controllers

import play.api.Logger
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import support.{Fixtures, ITSpec, TestConnector, WireMockResponses}
import timetopaytaxpayer.cor.model.Utr
import timetopaytaxpayer.des.DesConnector
import timetopaytaxpayer.sa.SaConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization

class TaxPayerControllerSpec extends ITSpec {

  val connector = fakeApplication().injector.instanceOf[TestConnector]
  val des = fakeApplication.injector.instanceOf[DesConnector]
  val sa = fakeApplication.injector.instanceOf[SaConnector]
  val cc = fakeApplication.injector.instanceOf[ControllerComponents]

  "should get a 401 without any authorization header" in {

      implicit def emptyHC = HeaderCarrier()

    val response = connector.getTaxPayerNotAuthorised(Fixtures.someUtr).failed.futureValue
    response.getMessage should include("401")
  }

  "should get a 200 with an authorization header" in {

    val expectedJson = Json.parse("""{
                 	"customerName": "Mr Lester Corncrake",
                 	"addresses": [{
                 		"addressLine1": "123 Any Street",
                 		"addressLine2": "Kingsland High Road",
                 		"addressLine3": "Dalston",
                 		"addressLine4": "Greater London",
                 		"addressLine5": "",
                 		"postcode": "E8 3PP"
                 	}],
                 	"selfAssessment": {
                 		"utr": "3217334604",
                 		"communicationPreferences": {
                 			"welshLanguageIndicator": true,
                 			"audioIndicator": false,
                 			"largePrintIndicator": false,
                 			"brailleIndicator": false
                 		},
                 		"debits": [{
                 			"originCode": "IN1",
                 			"amount": 2500,
                 			"dueDate": "2019-02-25",
                 			"taxYearEnd": "2019-04-05"
                 		}, {
                 			"originCode": "IN2",
                 			"amount": 2500,
                 			"dueDate": "2019-02-25",
                 			"taxYearEnd": "2019-04-05"
                 		}],
                 		"returns": [{
                 			"taxYearEnd": "2019-04-05",
                 			"dueDate": "2019-01-31"
                 		}, {
                 			"taxYearEnd": "2018-04-05",
                 			"dueDate": "2018-01-31",
                 			"receivedDate": "2018-03-09"
                 		}]
                 	}
                 }""")

    val utr = Utr("3217334604")
    implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("authorization-value")))
    WireMockResponses.commsOk(utr)
    WireMockResponses.debitsOk(utr)
    WireMockResponses.individualOk(utr)
    WireMockResponses.returnsOk(utr)
    val response = connector.getTaxPayerNotAuthorised(utr).futureValue
    response.status shouldBe Status.OK
    response.json shouldBe expectedJson
    Logger.warn(response.body)
  }

  "should get a 400 if comms is a 400" in {
    val utr = Fixtures.someUtr
    implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("authorization-value")))
    WireMockResponses.comms400(utr)
    WireMockResponses.debitsOk(utr)
    WireMockResponses.individualOk(utr)
    WireMockResponses.returnsOk(utr)
    val response = connector.getTaxPayerNotAuthorised(utr).failed.futureValue
    response.getMessage should include("400")
  }

  "should get a 404 if comms is a 404" in {
    val utr = Fixtures.someUtr
    implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("authorization-value")))
    WireMockResponses.comms404(utr)
    WireMockResponses.debitsOk(utr)
    WireMockResponses.individualOk(utr)
    WireMockResponses.returnsOk(utr)
    val response = connector.getTaxPayerNotAuthorised(utr).failed.futureValue
    response.getMessage should include("404")
  }

  "should get a 400 if debits is a 400" in {
    val utr = Fixtures.someUtr
    implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("authorization-value")))
    WireMockResponses.commsOk(utr)
    WireMockResponses.debits400(utr)
    WireMockResponses.individualOk(utr)
    WireMockResponses.returnsOk(utr)
    val response = connector.getTaxPayerNotAuthorised(utr).failed.futureValue
    response.getMessage should include("400")
  }

  "should get a 404 if debits is a 404" in {
    val utr = Fixtures.someUtr
    implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("authorization-value")))
    WireMockResponses.commsOk(utr)
    WireMockResponses.debits404(utr)
    WireMockResponses.individualOk(utr)
    WireMockResponses.returnsOk(utr)
    val response = connector.getTaxPayerNotAuthorised(utr).failed.futureValue
    response.getMessage should include("404")
  }

  "should get a 400 if returns is a 400" in {
    val utr = Fixtures.someUtr
    implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("authorization-value")))
    WireMockResponses.commsOk(utr)
    WireMockResponses.debitsOk(utr)
    WireMockResponses.individualOk(utr)
    WireMockResponses.returns400(utr)
    val response = connector.getTaxPayerNotAuthorised(utr).failed.futureValue
    response.getMessage should include("400")
  }

  "should get a 404 if returns is a 404" in {
    val utr = Fixtures.someUtr
    implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("authorization-value")))
    WireMockResponses.commsOk(utr)
    WireMockResponses.debitsOk(utr)
    WireMockResponses.individualOk(utr)
    WireMockResponses.returns404(utr)
    val response = connector.getTaxPayerNotAuthorised(utr).failed.futureValue
    response.getMessage should include("404")
  }

  "should get a 401 if individual is a 404" in {
    val utr = Fixtures.someUtr
    implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("authorization-value")))
    WireMockResponses.commsOk(utr)
    WireMockResponses.debitsOk(utr)
    WireMockResponses.individual401(utr)
    WireMockResponses.returnsOk(utr)
    val response = connector.getTaxPayerNotAuthorised(utr).failed.futureValue
    response.getMessage should include("401")
  }

  "should get a 500 if individual is a 500" in {
    val utr = Fixtures.someUtr
    implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("authorization-value")))
    WireMockResponses.commsOk(utr)
    WireMockResponses.debitsOk(utr)
    WireMockResponses.individual401(utr)
    WireMockResponses.returnsOk(utr)
    val response = connector.getTaxPayerNotAuthorised(utr).failed.futureValue
    response.getMessage should include("500")
  }

}
