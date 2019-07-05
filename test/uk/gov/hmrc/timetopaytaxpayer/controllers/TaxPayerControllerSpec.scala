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

import play.api.mvc.ControllerComponents
import support.{ITSpec, TestConnector}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.timetopaytaxpayer.Utr
import uk.gov.hmrc.timetopaytaxpayer.connectors.{DesConnector, SaConnector}

class TaxPayerControllerSpec extends ITSpec {

  val connector = fakeApplication().injector.instanceOf[TestConnector]
  val des = fakeApplication.injector.instanceOf[DesConnector]
  val sa = fakeApplication.injector.instanceOf[SaConnector]
  val cc = fakeApplication.injector.instanceOf[ControllerComponents]

  implicit def emptyHC = HeaderCarrier()

  "should get a 401 without any authorization header" in {
    val response = connector.getTaxPayer(Utr("123456789")).failed.futureValue
    response.getMessage should include ("401")
  }

}
