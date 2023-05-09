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

package timetopaytaxpayer.sa.model

import play.api.libs.json.{Json, Reads}

//from the sa docs: All of the value fields nested under name, address, telephone and email are optional and the client should expect any of them to be unspecified.
//SSTTP2-363
case class SaName(
    title:          Option[String],
    forename:       Option[String],
    secondForename: Option[String],
    surname:        String
) {

  def fullName: String = Seq(title, forename, secondForename, Some(surname)).collect {
    case Some(x) => x
  }.mkString(" ")
}

object SaName {
  implicit val reads: Reads[SaName] = Json.reads[SaName]
}
