/*
 * Copyright 2021 HM Revenue & Customs
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
import timetopaytaxpayer.sa.model.{SaIndividual, SaName}

object SaWiremockResponses {

  private val individual =
    """{
      |"name":{"title":"Mr","forename":"Lester","surname":"Corncrake","honours":"KCBE"},
      |"address":{"addressLine1":"123 Any Street","addressLine2":"Kingsland High Road","addressLine3":"Dalston","addressLine4":"Greater London","addressLine5":"","postcode":"E8 3PP","additionalDeliveryInformation":"Watch the dog"},"contact":{"telephone":{"daytime":"02765760#1235","evening":"027657630","mobile":"rowMapper","fax":"0208875765"},
      |"email":{"primary":"lc@notreal.com.com.com"}}}""".stripMargin

  def getIndividual(
      utr:      SaUtr  = TdAll.saUtr,
      response: String = individual,
      status:   Int    = 200
  ): StubMapping = {
    stubFor(
      get(
        urlEqualTo(s"/sa/individual/${utr.value}/designatory-details/taxpayer")
      ).willReturn(
          aResponse()
            .withStatus(status)
            .withBody(response)
        )
    )
  }

}
