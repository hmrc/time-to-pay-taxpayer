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

package timetopayarrangement.des.model

import play.api.libs.json.{Json, OFormat}

case class LetterAndControl(
    customerName:       String,
    salutation:         String         = "Dear Sir or Madam",
    addressLine1:       String         = "",
    addressLine2:       Option[String] = None,
    addressLine3:       Option[String] = None,
    addressLine4:       Option[String] = None,
    addressLine5:       Option[String] = None,
    postCode:           String         = "",
    totalAll:           String,
    clmIndicateInt:     String         = "Interest is due",
    clmPymtString:      String,
    officeName1:        String         = "",
    officeName2:        String         = "",
    officePostcode:     String         = "",
    officePhone:        String         = "",
    officeFax:          String         = "",
    officeOpeningHours: String         = "9-5",
    template:           String         = "template",
    exceptionType:      Option[String] = None,
    exceptionReason:    Option[String] = None
)

object LetterAndControl {
  implicit val format: OFormat[LetterAndControl] = Json.format[LetterAndControl]
}
