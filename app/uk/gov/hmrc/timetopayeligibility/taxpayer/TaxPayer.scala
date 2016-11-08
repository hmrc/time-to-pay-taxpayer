package uk.gov.hmrc.timetopayeligibility.taxpayer

import java.time.LocalDate

import play.api.libs.json._
import uk.gov.hmrc.timetopayeligibility.communication.preferences.CommunicationPreferences

object TaxPayer {

  val writer: Writes[TaxPayer] = {
    implicit val writeReturn: Writes[TaxPayer] = TaxPayer.writer
    implicit val writeAddress: Writes[Address] = Address.writer
    implicit val writePreferences: Writes[CommunicationPreferences] = Json.writes[CommunicationPreferences]
    implicit val writeSelfAssessmentDetails: Writes[SelfAssessmentDetails] = Json.writes[SelfAssessmentDetails]
    implicit val writeDebits: Writes[Debit] = Json.writes[Debit]

    (JsPath \ "taxPayer").write[TaxPayer]
  }
}

case class TaxPayer(customerName: String, addresses: Seq[Address],
                    selfAssessmentDetails: SelfAssessmentDetails, debits: Seq[Debit])


case class SelfAssessmentDetails(utr: String, communicationPreferences: CommunicationPreferences)

case class Debit(debitType: String, dueDate: LocalDate)


object Address {

  val writer: Writes[Address] = Writes[Address](address => {
    val addressLines = address.lines.zipWithIndex.map { p => "addressLine" + (p._2+1) -> JsString(p._1) }
    JsObject(addressLines :+ ("postCode" -> JsString(address.postCode)))
  })

}

case class Address(lines: Seq[String], postCode: String)


