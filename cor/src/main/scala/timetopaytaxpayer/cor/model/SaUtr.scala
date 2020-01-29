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

package timetopaytaxpayer.cor.model

import play.api.libs.functional.syntax._
import play.api.libs.json.Format
import play.api.mvc.PathBindable
import timetopaytaxpayer.cor.internal.ValueClassBinder

case class SaUtr(value: String) {
  def obfuscate: SaUtr = SaUtr(value = value.take(4) + "***")
}

object SaUtr {
  implicit val format: Format[SaUtr] = implicitly[Format[String]].inmap(SaUtr(_), _.value)
  implicit val journeyIdBinder: PathBindable[SaUtr] = ValueClassBinder.valueClassBinder(_.value)

}
