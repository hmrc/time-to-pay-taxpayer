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

package timetopayarrangement

import javax.inject.Inject
import play.api.mvc.{Action, ControllerComponents}
import timetopaytaxpayer.cor.model.{CommunicationPreferences, TaxpayerDetails}
import timetopaytaxpayer.des.DesConnector
import timetopaytaxpayer.sa.PaymentStubsProtectedConnector
import timetopaytaxpayer.sa.model.SaIndividual
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext

class ArrangementController @Inject() (
                                        arrangementService: ArrangementService,
                                        cc:                 ControllerComponents,
                                        desConnector:       DesConnector,
                                        paymentStubsProtectedConnector:        PaymentStubsProtectedConnector
)(implicit ec: ExecutionContext) extends BackendController(cc) {

  def submitArrangement(): Action[SetupArrangementRequest] = Action.async(parse.json[SetupArrangementRequest]) { implicit request =>
    val setupArrangementRequest: SetupArrangementRequest = request.body

    val preferencesF = desConnector.getCommunicationPreferences(setupArrangementRequest.utr)
    val individualF = paymentStubsProtectedConnector.getIndividual(setupArrangementRequest.utr)

    for {
      preferences: CommunicationPreferences <- preferencesF
      individual: SaIndividual <- individualF
      taxpayerDetails = TaxpayerDetails(
        utr                      = setupArrangementRequest.utr,
        customerName             = individual.name.fullName,
        addresses                = List(individual.address),
        communicationPreferences = preferences
      )
      _ <- arrangementService.submit(setupArrangementRequest, taxpayerDetails)
    } yield Created
  }
}
