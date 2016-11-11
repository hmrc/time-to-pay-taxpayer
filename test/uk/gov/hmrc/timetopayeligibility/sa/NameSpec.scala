/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.timetopayeligibility.sa

import uk.gov.hmrc.play.test.UnitSpec

class NameSpec extends UnitSpec {

  "Name" should {
    "print to string correctly without middle name" in {
      DesignatoryDetails.Name("President", "Donald", None, "Trump").toString() shouldBe "President Donald Trump"
    }

    "print to string correctly with middle name" in {
      DesignatoryDetails.Name("President", "Donald", Some("John"), "Trump").toString() shouldBe "President Donald John Trump"
    }
  }


}
