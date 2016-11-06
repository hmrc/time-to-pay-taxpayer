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

package uk.gov.hmrc.timetopayeligibility.controllers

import play.api.mvc._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.timetopayeligibility.Utr
import uk.gov.hmrc.timetopayeligibility.returns.ReturnsService
import uk.gov.hmrc.timetopayeligibility.returns.ReturnsService.ReturnsResult

import scala.concurrent.{ExecutionContext, Future}

import ExecutionContext.Implicits.global

object EligibilityController extends EligibilityController(ReturnsService.returns(ReturnsService.localCall))

class EligibilityController(returnsService: (Utr => Future[ReturnsResult]))
                           (implicit executionContext: ExecutionContext) extends BaseController {

  def eligibility(utr: String) = Action.async { implicit request =>
    returnsService(Utr(utr)).map {
      _.fold(error => InternalServerError(error.message),
        returns => Ok(utr)
      )
    }
  }
}
