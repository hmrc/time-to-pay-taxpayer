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

package timetopaytaxpayer.cor.model

import play.api.libs.json.{Format, Json, OFormat}
import timetopaytaxpayer.cor.crypto
import timetopaytaxpayer.cor.crypto.CryptoFormat
import timetopaytaxpayer.cor.crypto.model.{Encryptable, Encrypted}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

//// todo - remove as part of OPS-4581
case class Taxpayer(
    customerName:   String,
    addresses:      Seq[Address],
    selfAssessment: SelfAssessmentDetails
) extends Encryptable[Taxpayer] {
  def obfuscate: Taxpayer = Taxpayer(
    customerName   = customerName.replaceAll("[A-Za-z]", "x"),
    addresses      = addresses.map(_.obfuscate),
    selfAssessment = selfAssessment.obfuscate
  )

  override def encrypt: EncryptedTaxpayer = EncryptedTaxpayer(
    SensitiveString(customerName),
    addresses.map(_.encrypt),
    selfAssessment
  )
}

object Taxpayer {
  implicit val format: OFormat[Taxpayer] = Json.format[Taxpayer]
}

case class EncryptedTaxpayer(
    customerName:   SensitiveString,
    addresses:      Seq[EncryptedAddress],
    selfAssessment: SelfAssessmentDetails
) extends Encrypted[Taxpayer] {
  override def decrypt: Taxpayer = Taxpayer(
    customerName.decryptedValue,
    addresses.map(_.decrypt),
    selfAssessment
  )
}

object EncryptedTaxpayer {
  implicit def format(implicit cryptoFormat: CryptoFormat): OFormat[EncryptedTaxpayer] = {
    implicit val sensitiveFormat: Format[SensitiveString] = crypto.sensitiveStringFormat(cryptoFormat)
    Json.format[EncryptedTaxpayer]
  }
}
