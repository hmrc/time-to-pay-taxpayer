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

import play.api.Logger
import timetopayarrangement.{Schedule, SetupArrangementRequest}
import timetopayarrangement.des.model.LetterAndControl
import timetopaytaxpayer.cor.model.{Address, CommunicationPreferences, TaxpayerDetails}

import scala.util.Try

object LetterAndControlBuilder {
  type AddressResult = (Address, Option[LetterError])

  case class LetterError(code: Int, message: String)

  object LetterError {
    def welshLargePrint() = LetterError(5, "welsh large print required")
    def welshAudio() = LetterError(7, "audio welsh required")
    def welsh() = LetterError(4, "welsh required")
    def braille() = LetterError(2, "braille required")
    def audio() = LetterError(6, "audio required")
    def largePrint() = LetterError(3, "large print required")
  }

  def create(ttpArrangement: SetupArrangementRequest, taxpayerDetails: TaxpayerDetails): LetterAndControl = {
    val correspondence: AddressResult = resolveAddress(ttpArrangement, taxpayerDetails)
    val (add, letterError: Option[LetterError]) = correspondence

    val address: Address = validateAddressFormat(add)

    val resolveCommsException: (Option[String], Option[String]) = commsPrefException(taxpayerDetails.communicationPreferences)
      .map(letterError => (Some(letterError.code.toString), Some(letterError.message)))
      .getOrElse((None, None))

    val exceptions: (Option[String], Option[String]) = letterError.fold(resolveCommsException)(x => (Some(x.code.toString), Some(x.message)))

    val customerName = taxpayerDetails.customerName
    LetterAndControl(
      customerName       = customerName,
      salutation         = s"Dear $customerName",
      addressLine1       = address.addressLine1.getOrElse(""),
      addressLine2       = address.addressLine2,
      addressLine3       = address.addressLine3,
      addressLine4       = address.addressLine4,
      addressLine5       = address.addressLine5,
      postCode           = address.postcode.getOrElse(""),
      totalAll           = ttpArrangement.schedule.totalPayable.setScale(2).toString(),
      clmPymtString      = paymentMessage(ttpArrangement.schedule),
      clmIndicateInt     = "Including interest due",
      template           = "DMTC13",
      officeName1        = "HMRC",
      officeName2        = "DM 440",
      officePostcode     = "BX5 5AB",
      officePhone        = "0300 200 3822",
      officeFax          = "01708 707502",
      officeOpeningHours = "Monday - Friday 08.00 to 20.00",
      exceptionType      = exceptions._1,
      exceptionReason    = exceptions._2
    )
  }

  private def validateAddressFormat(address: Address): Address = Address(
    addressLine1 = address.addressLine1,
    addressLine2 = if (address.addressLine2.getOrElse("").equals("")) None else address.addressLine2,
    addressLine3 = if (address.addressLine3.getOrElse("").equals("")) None else address.addressLine3,
    addressLine4 = if (address.addressLine4.getOrElse("").equals("")) None else address.addressLine4,
    addressLine5 = if (address.addressLine5.getOrElse("").equals("")) None else address.addressLine5,
    postcode     = address.postcode
  )

  private def resolveAddress(ttpArrangement: SetupArrangementRequest, taxpayerDetails: TaxpayerDetails): AddressResult = {
    implicit val taxpayer: TaxpayerDetails = taxpayerDetails
    taxpayer.addresses match {
      case Nil =>
        Logger.logger.debug("No address found in Digital")
        (Address(), Some(LetterError(8, "no address")))
      case x :: Nil =>
        Logger.logger.debug("Found single address")
        validate(x)
      case _ =>
        Logger.logger.debug("Found multiple addresses")
        multipleAddresses
    }
  }

  private def multipleAddresses(implicit taxpayer: TaxpayerDetails): AddressResult = {
    val uniqueAddressTypes: Seq[JurisdictionType] = taxpayer.addresses.map(JurisdictionChecker.addressToJurisdictionType).distinct

    uniqueAddressTypes match {
      case x :: Nil =>
        Logger.logger.trace("Found single unique address type found")
        validate(taxpayer.addresses.head)
      case _ =>
        Logger.logger.trace(s"Customer has addresses in ${uniqueAddressTypes.mkString(" and")} jurisdictions")
        (Address(), Some(LetterError(1, "address jurisdiction conflict")))
    }
  }

  def validate(address: Address): AddressResult = address match {
    case Address(_, _, _, _, _, Some("")) | Address(Some(""), _, _, _, _, _) =>
      (address, Some(LetterError(9, "incomplete address")))
    case _ =>
      (address, None)
  }

  private def paymentMessage(schedule: Schedule): String = {
    val instalmentSize = schedule.instalments.size - 2
    val regularPaymentAmount = schedule.instalments.head.amount.setScale(2)
    val lastPaymentAmount = schedule.instalments.last.amount.setScale(2)

    val initialPayment = (Try(schedule.initialPayment).getOrElse(BigDecimal(0.0)) + schedule.instalments.head.amount).setScale(2)

    instalmentSize match {
      case 0 => f"Initial payment of £$initialPayment%,.2f then a final payment of £" + s"$lastPaymentAmount%,.2f"
      case _ => f"Initial payment of £$initialPayment%,.2f then $instalmentSize payments of £$regularPaymentAmount%,.2f and final payment of £" +
        f"$lastPaymentAmount%,.2f"
    }
  }

  private def commsPrefException(commsPrefs: CommunicationPreferences): Option[LetterError] = commsPrefs match {
    case CommunicationPreferences(true, _, true, _) =>
      Logger.logger.debug(s"Exception found in LetterAndControl - Code: 5 Reason: Welsh large print required")
      Some(LetterError.welshLargePrint())
    case CommunicationPreferences(true, true, _, _) =>
      Logger.logger.debug(s"Exception found in LetterAndControl - Code: 7 Reason: Audio Welsh required")
      Some(LetterError.welshAudio())
    case CommunicationPreferences(true, _, _, _) =>
      Logger.logger.debug(s"Exception found in LetterAndControl - Code: 4 Reason: Welsh required")
      Some(LetterError.welsh())
    case CommunicationPreferences(_, _, _, true) =>
      Logger.logger.debug(s"Exception found in LetterAndControl - Code: 2 Reason: Braille required")
      Some(LetterError.braille())
    case CommunicationPreferences(_, true, _, _) =>
      Logger.logger.debug(s"Exception found in LetterAndControl - Code: 6 Reason: Audio required")
      Some(LetterError.audio())
    case CommunicationPreferences(_, _, true, _) =>
      Logger.logger.debug(s"Exception found in LetterAndControl - Code: 3 Reason: Large print required")
      Some(LetterError.largePrint())
    case _ => None
  }

}
