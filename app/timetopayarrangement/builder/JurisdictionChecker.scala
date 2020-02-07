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

package timetopayarrangement.builder

import timetopaytaxpayer.cor.model.Address

sealed trait JurisdictionType

case object English extends JurisdictionType
case object Scottish extends JurisdictionType
case object Welsh extends JurisdictionType

object JurisdictionChecker {

  private val scottishPostCodeRegex = "^(AB|DD|DG|EH|FK|G|HS|IV|KA|KW|KY|ML|PA|PH|TD|ZE)[0-9].*".r
  private val welshPostCodeRegex = "^(LL|SY|LD|HR|NP|CF|SA)[0-9].*".r

  def addressToJurisdictionType(address: Address): JurisdictionType = {
    address.postcode.getOrElse("") match {
      case scottishPostCodeRegex(_) => Scottish
      case welshPostCodeRegex(_)    => Welsh
      case _                        => English
    }
  }
}
