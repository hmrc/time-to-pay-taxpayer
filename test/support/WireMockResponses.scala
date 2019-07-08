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

package support

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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import uk.gov.hmrc.timetopaytaxpayer.Utr

object WireMockResponses {

  def returnsOk(utr: Utr): StubMapping = {

    val response = """{"returns":[{"taxYearEnd":"2019-04-05","dueDate":"2019-01-31","issueDate":"2018-02-15"},{"taxYearEnd":"2018-04-05","dueDate":"2018-01-31","issueDate":"2017-02-15","receivedDate":"2018-03-09"}]}"""
    stubFor(
      get(
        urlEqualTo(s"/sa/taxpayer/${utr.value}/returns")).willReturn(
          aResponse()
            .withStatus(200)
            .withBody(response)))
  }

  //invalid UTR
  def returns400(utr: Utr): StubMapping = {

    val response = """{"returns":[{"taxYearEnd":"2019-04-05","dueDate":"2019-01-31","issueDate":"2018-02-15"},{"taxYearEnd":"2018-04-05","dueDate":"2018-01-31","issueDate":"2017-02-15","receivedDate":"2018-03-09"}]}"""
    stubFor(
      get(
        urlEqualTo(s"/sa/taxpayer/${utr.value}/returns")).willReturn(
          aResponse()
            .withStatus(400)
            .withBody("""{"reason":"Invalid UTR number","reasonCode":"err-code-goes-here"}""")))
  }

  def returns404(utr: Utr): StubMapping = {

    stubFor(
      get(
        urlEqualTo(s"/sa/taxpayer/${utr.value}/returns")).willReturn(
          aResponse()
            .withStatus(404)))
  }

  //----------------------------------------------------------------------------------------------------------------

  def debitsOk(utr: Utr): StubMapping = {

    val response = """{"debits":[{"taxYearEnd":"2019-04-05","charge":{"originCode":"IN1","creationDate":"2019-01-05"},"relevantDueDate":"2019-02-25","totalOutstanding":2500},{"taxYearEnd":"2019-04-05","charge":{"originCode":"IN2","creationDate":"2019-01-05"},"relevantDueDate":"2019-02-25","totalOutstanding":2500}]}"""
    stubFor(
      get(
        urlEqualTo(s"/sa/taxpayer/${utr.value}/debits")).willReturn(
          aResponse()
            .withStatus(200)
            .withBody(response)))
  }

  def debits400(utr: Utr): StubMapping = {

    stubFor(
      get(
        urlEqualTo(s"/sa/taxpayer/${utr.value}/debits")).willReturn(
          aResponse()
            .withStatus(400)
            .withBody("""{"reason":"Invalid UTR number","reasonCode":"err-code-goes-here"}""")))
  }

  def debits404(utr: Utr): StubMapping = {

    stubFor(
      get(
        urlEqualTo(s"/sa/taxpayer/${utr.value}/debits")).willReturn(
          aResponse()
            .withStatus(404)))
  }

  //----------------------------------------------------------------------------------------------------------------

  def commsOk(utr: Utr): StubMapping = {
    val response = """{"welshLanguageIndicator":true,"audioIndicator":false,"largePrintIndicator":false,"brailleIndicator":false}"""
    stubFor(
      get(
        urlEqualTo(s"/sa/taxpayer/${utr.value}/communication-preferences")).willReturn(
          aResponse()
            .withStatus(200)
            .withBody(response)))
  }

  def comms400(utr: Utr): StubMapping = {
    stubFor(
      get(
        urlEqualTo(s"/sa/taxpayer/${utr.value}/communication-preferences")).willReturn(
          aResponse()
            .withStatus(400)
            .withBody("""{"reason":"Invalid UTR number","reasonCode":"err-code-goes-here"}""")))
  }

  def comms404(utr: Utr): StubMapping = {
    stubFor(
      get(
        urlEqualTo(s"/sa/taxpayer/${utr.value}/communication-preferences")).willReturn(
          aResponse()
            .withStatus(404)))
  }

  //----------------------------------------------------------------------------------------------------------------

  def individualOk(utr: Utr): StubMapping = {

    val response = """{"name":{"title":"Mr","forename":"Lester","surname":"Corncrake","honours":"KCBE"},"address":{"addressLine1":"123 Any Street","addressLine2":"Kingsland High Road","addressLine3":"Dalston","addressLine4":"Greater London","addressLine5":"","postcode":"E8 3PP","additionalDeliveryInformation":"Watch the dog"},"contact":{"telephone":{"daytime":"02765760#1235","evening":"027657630","mobile":"rowMapper","fax":"0208875765"},"email":{"primary":"lc@notreal.com.com.com"}}}"""

    stubFor(
      get(
        urlEqualTo(s"/sa/individual/${utr.value}/designatory-details/taxpayer")).willReturn(
          aResponse()
            .withStatus(200)
            .withBody(response)))
  }

  def individual401(utr: Utr): StubMapping = {
    stubFor(
      get(
        urlEqualTo(s"/sa/individual/${utr.value}/designatory-details/taxpayer")).willReturn(
          aResponse()
            .withStatus(401).withBody(s"Unauthorized looking up ${utr.value}")))
  }

  def individual500(utr: Utr): StubMapping = {
    stubFor(
      get(
        urlEqualTo(s"/sa/individual/${utr.value}/designatory-details/taxpayer")).willReturn(
          aResponse()
            .withStatus(500).withBody(s"Server error looking up ${utr.value}")))
  }

}
