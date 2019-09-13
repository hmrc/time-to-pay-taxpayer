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

package wiremockresponses

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import support.TdAll
import timetopaytaxpayer.cor.model.SaUtr

object DesWiremockResponses {

  def getReturns(
      utr:      SaUtr  = TdAll.saUtr,
      status:   Int    = 200,
      response: String = """{"returns":[{"taxYearEnd":"2019-04-05","dueDate":"2019-01-31","issueDate":"2018-02-15"},{"taxYearEnd":"2018-04-05","dueDate":"2018-01-31","issueDate":"2017-02-15","receivedDate":"2018-03-09"}]}"""
  ): StubMapping = {
    stubFor(
      get(
        urlEqualTo(s"/sa/taxpayer/${utr.value}/returns")
      )
        .withHeader("Authorization", equalTo("Bearer secretDesToken"))
        .withHeader("Environment", equalTo("localhostEnvironment"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(response)
        )
    )
  }

  def getDebits(
      utr:      SaUtr  = TdAll.saUtr,
      response: String = """{"debits":[{"taxYearEnd":"2019-04-05","charge":{"originCode":"IN1","creationDate":"2019-01-05"},"relevantDueDate":"2019-02-25","totalOutstanding":2500},{"taxYearEnd":"2019-04-05","charge":{"originCode":"IN2","creationDate":"2019-01-05"},"relevantDueDate":"2019-02-25","totalOutstanding":2500}]}""",
      status:   Int    = 200
  ): StubMapping = {
    stubFor(
      get(
        urlEqualTo(s"/sa/taxpayer/${utr.value}/debits")
      )
        .withHeader("Authorization", equalTo("Bearer secretDesToken"))
        .withHeader("Environment", equalTo("localhostEnvironment"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(response)
        )
    )
  }

  def getCommunicationPreferences(
      utr:      SaUtr  = TdAll.saUtr,
      response: String = """{"welshLanguageIndicator":true,"audioIndicator":false,"largePrintIndicator":false,"brailleIndicator":false}""",
      status:   Int    = 200
  ): StubMapping = {

    stubFor(
      get(
        urlEqualTo(s"/sa/taxpayer/${utr.value}/communication-preferences")
      )
        .withHeader("Authorization", equalTo("Bearer secretDesToken"))
        .withHeader("Environment", equalTo("localhostEnvironment"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(response)
        )
    )
  }

}
