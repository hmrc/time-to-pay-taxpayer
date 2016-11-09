package uk.gov.hmrc.timetopayeligibility.controllers

import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences._
import uk.gov.hmrc.timetopayeligibility.debits.Debits._
import uk.gov.hmrc.timetopayeligibility.taxpayer.{Address, SelfAssessmentDetails, TaxPayer}
import uk.gov.hmrc.timetopayeligibility.{Utr, taxpayer}

import scala.concurrent.{ExecutionContext, Future}

class TaxPayerController(debitsService: (Utr => Future[DebitsResult]),
                         preferencesService: (Utr => Future[CommunicationPreferencesResult]) )
                        (implicit executionContext: ExecutionContext) extends BaseController {

  def taxPayer(utrAsString: String) = Action.async { implicit request =>
    implicit val writeAddress: Writes[TaxPayer] = TaxPayer.writer

    val utr = Utr(utrAsString)

    (for {
      debitsResult: DebitsResult <- debitsService(utr)
      preferencesResult: CommunicationPreferencesResult <- preferencesService(utr)
    } yield {
      TaxPayer(
        customerName = "Customer name",
        addresses = Seq(Address(Seq("123 Fake Street", "Foo", "Bar"), "BN3 2GH")),
        selfAssessment = SelfAssessmentDetails(
          utr = utrAsString,
          communicationPreferences = preferencesResult.right.get,
          debits = debitsResult.right.get.map{ d => taxpayer.Debit(d.charge.originCode, d.relevantDueDate) }
        )
      )
    }).map(jsons => Ok(Json.toJson(jsons)))

  }

}
