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

package timetopaytaxpayer.sa

import play.api.libs.json.{Json, Reads}
import timetopaytaxpayer.cor.model.Address

/**
 * DesignatoryDetails
 */
object Sa {

  case class Individual(
      name:    Name,
      address: Address
  )

  object Individual {
    implicit val reads: Reads[Individual] = Json.reads[Individual]
  }

  //from the sa docs: All of the value fields nested under name, address, telephone and email are optional and the client should expect any of them to be unspecified.
  //SSTTP2-363
  case class Name(
      title:          Option[String],
      forename:       Option[String],
      secondForename: Option[String],
      surname:        String
  ) {

    def fullName: String = Seq(title, forename, secondForename, Some(surname)).collect {
      case Some(x) => x
    }.mkString(" ")
  }

  object Name {
    implicit val reads: Reads[Name] = Json.reads[Name]
  }
}
