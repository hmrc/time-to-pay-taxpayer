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
import timetopayarrangement.builder.{DesTtpArrangementBuilder, LetterAndControlBuilder}
import timetopayarrangement.des.DesConnector
import timetopayarrangement.des.model.DesSetupArrangementRequest
import timetopaytaxpayer.cor.model.TaxpayerDetails
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ArrangementService @Inject() (
    desConnector: DesConnector
)(implicit ec: ExecutionContext) {

  /**
   * Builds and submits the TTPArrangement to Des.
   */
  def submit(setupArrangementRequest: SetupArrangementRequest, taxpayerDetails: TaxpayerDetails)(implicit hc: HeaderCarrier): Future[Unit] = {
    val desSetupArrangementRequest = DesSetupArrangementRequest(
      ttpArrangement   = DesTtpArrangementBuilder.createDesTtpArrangement(setupArrangementRequest, taxpayerDetails),
      letterAndControl = LetterAndControlBuilder.create(setupArrangementRequest, taxpayerDetails)
    )
    desConnector.submitArrangement(
      utr                        = taxpayerDetails.utr,
      desSetupArrangementRequest = desSetupArrangementRequest
    ).map(_ => ())
  }

}
