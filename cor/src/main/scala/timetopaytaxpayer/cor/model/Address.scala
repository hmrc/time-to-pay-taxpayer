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

final case class Address(
    addressLine1: Option[String] = None,
    addressLine2: Option[String] = None,
    addressLine3: Option[String] = None,
    addressLine4: Option[String] = None,
    addressLine5: Option[String] = None,
    postcode:     Option[String] = None
) extends Encryptable[Address] {

  def obfuscate: Address = Address(
    addressLine1 = addressLine1.map(_.replaceAll("[A-Za-z]", "x")),
    addressLine2 = addressLine2.map(_.replaceAll("[A-Za-z]", "x")),
    addressLine3 = addressLine3.map(_.replaceAll("[A-Za-z]", "x")),
    addressLine4 = addressLine4.map(_.replaceAll("[A-Za-z]", "x")),
    addressLine5 = addressLine5.map(_.replaceAll("[A-Za-z]", "x")),
    postcode     = postcode.map(_.replaceAll("[A-Za-z]", "x"))
  )

  override def encrypt: EncryptedAddress = EncryptedAddress(
    addressLine1.map(SensitiveString),
    addressLine2.map(SensitiveString),
    addressLine3.map(SensitiveString),
    addressLine4.map(SensitiveString),
    addressLine5.map(SensitiveString),
    postcode.map(SensitiveString)
  )
}

object Address {
  implicit val format: OFormat[Address] = Json.format[Address]
}

case class EncryptedAddress(
    addressLine1: Option[SensitiveString] = None,
    addressLine2: Option[SensitiveString] = None,
    addressLine3: Option[SensitiveString] = None,
    addressLine4: Option[SensitiveString] = None,
    addressLine5: Option[SensitiveString] = None,
    postcode:     Option[SensitiveString] = None
) extends Encrypted[Address] {

  override def decrypt: Address = Address(
    addressLine1.map(_.decryptedValue),
    addressLine2.map(_.decryptedValue),
    addressLine3.map(_.decryptedValue),
    addressLine4.map(_.decryptedValue),
    addressLine5.map(_.decryptedValue),
    postcode.map(_.decryptedValue)
  )

}

object EncryptedAddress {
  implicit def format(implicit cryptoFormat: CryptoFormat): OFormat[EncryptedAddress] = {
    implicit val sensitiveFormat: Format[SensitiveString] = crypto.sensitiveStringFormat(cryptoFormat)
    Json.format[EncryptedAddress]
  }
}
