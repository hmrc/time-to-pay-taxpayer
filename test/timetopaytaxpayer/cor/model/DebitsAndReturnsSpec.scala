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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsSuccess, Json}

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate, LocalDateTime, ZoneId}

class DebitsAndReturnsSpec extends AnyWordSpecLike with Matchers {

  "DebitAndReturns" should {

    "have a fixReturns method" in {
      implicit val clock: Clock = Clock.fixed(
        LocalDateTime
          .parse("2006-01-22T16:28:55.185", DateTimeFormatter.ISO_DATE_TIME)
          .atZone(ZoneId.of("Europe/London")).toInstant,
        ZoneId.of("UTC")
      )
      saBeforeFix.fixReturns shouldBe saAfterFix
    }

    "should have a format instance" in {
      val expectedJson = Json.parse(
        """
          |{
          |  "debits" : [ {
          |    "originCode" : "POA2",
          |    "amount" : 250.52,
          |    "dueDate" : "2016-01-31",
          |    "interest" : {
          |      "creationDate" : "2016-06-01",
          |      "amount" : 42.32
          |    },
          |    "taxYearEnd" : "2017-04-05"
          |  } ],
          |  "returns" : [ {
          |    "taxYearEnd" : "2000-04-05",
          |    "issuedDate" : "2001-01-23",
          |    "dueDate" : "2001-04-30",
          |    "receivedDate" : "2001-04-10"
          |  }, {
          |    "taxYearEnd" : "2001-04-10",
          |    "issuedDate" : "2001-04-06",
          |    "dueDate" : "2002-01-31",
          |    "receivedDate" : "2001-06-19"
          |  }, {
          |    "taxYearEnd" : "2002-04-05",
          |    "issuedDate" : "2002-04-06",
          |    "dueDate" : "2003-01-31",
          |    "receivedDate" : "2002-05-08"
          |  }, {
          |    "taxYearEnd" : "2003-04-05",
          |    "issuedDate" : "2003-04-06",
          |    "dueDate" : "2004-01-31",
          |    "receivedDate" : "2003-07-04"
          |  }, {
          |    "taxYearEnd" : "2004-04-05",
          |    "issuedDate" : "2004-04-06",
          |    "dueDate" : "2005-01-31",
          |    "receivedDate" : "2004-08-23"
          |  }, {
          |    "taxYearEnd" : "2005-04-05"
          |  } ]
          |}
          |""".stripMargin
      )

      Json.toJson(saBeforeFix) shouldBe expectedJson
      expectedJson.validate[ReturnsAndDebits] shouldBe JsSuccess(saBeforeFix)
    }

  }

  private implicit def stringToDate(s: String): LocalDate = LocalDate.parse(s)
  private implicit def stringToDateO(s: String): Option[LocalDate] = Some(LocalDate.parse(s))
  private lazy val saAfterFix = ReturnsAndDebits(
    debits  = List(
      Debit(
        originCode = "POA2",
        amount     = 250.52,
        dueDate    = LocalDate.parse("2016-01-31"),
        interest   = Some(Interest(Some(LocalDate.parse("2016-06-01")), 42.32)),
        taxYearEnd = LocalDate.parse("2017-04-05")
      )
    ),
    returns = List(
      Return(
        taxYearEnd   = "2001-04-10",
        issuedDate   = "2001-04-06",
        dueDate      = "2002-01-31",
        receivedDate = "2001-06-19"
      ),
      Return(
        taxYearEnd   = "2002-04-05",
        issuedDate   = "2002-04-06",
        dueDate      = "2003-01-31",
        receivedDate = "2002-05-08"
      ),
      Return(
        taxYearEnd   = "2003-04-05",
        issuedDate   = "2003-04-06",
        dueDate      = "2004-01-31",
        receivedDate = "2003-07-04"
      ),
      Return(
        taxYearEnd   = "2004-04-05",
        issuedDate   = "2004-04-06",
        dueDate      = "2005-01-31",
        receivedDate = "2004-08-23"
      ),
      Return(
        taxYearEnd   = "2005-04-05",
        issuedDate   = None,
        dueDate      = None,
        receivedDate = None
      )
    )
  )

  private lazy val saBeforeFix = ReturnsAndDebits(
    debits  = List(
      Debit(
        originCode = "POA2",
        amount     = 250.52,
        dueDate    = LocalDate.parse("2016-01-31"),
        interest   = Some(Interest(Some(LocalDate.parse("2016-06-01")), 42.32)),
        taxYearEnd = LocalDate.parse("2017-04-05")
      )
    ),
    returns = List(
      Return(
        taxYearEnd   = "2000-04-05",
        issuedDate   = "2001-01-23",
        dueDate      = "2001-04-30",
        receivedDate = "2001-04-10"
      ),
      Return(
        taxYearEnd   = "2001-04-10",
        issuedDate   = "2001-04-06",
        dueDate      = "2002-01-31",
        receivedDate = "2001-06-19"
      ),
      Return(
        taxYearEnd   = "2002-04-05",
        issuedDate   = "2002-04-06",
        dueDate      = "2003-01-31",
        receivedDate = "2002-05-08"
      ),
      Return(
        taxYearEnd   = "2003-04-05",
        issuedDate   = "2003-04-06",
        dueDate      = "2004-01-31",
        receivedDate = "2003-07-04"
      ),
      Return(
        taxYearEnd   = "2004-04-05",
        issuedDate   = "2004-04-06",
        dueDate      = "2005-01-31",
        receivedDate = "2004-08-23"
      ),
      Return(
        taxYearEnd   = "2005-04-05",
        issuedDate   = None,
        dueDate      = None,
        receivedDate = None
      )
    )
  )

}
