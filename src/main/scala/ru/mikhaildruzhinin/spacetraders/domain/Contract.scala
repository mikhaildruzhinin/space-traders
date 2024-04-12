package ru.mikhaildruzhinin.spacetraders.domain

import enumeratum._

import java.time.Instant

/**
 * Contract details.
 *
 * @param id               ID of the contract.
 * @param factionSymbol    The symbol of the faction that this contract is for.
 * @param `type`           Type of contract.
 * @param terms            The terms to fulfill the contract.
 * @param accepted         Whether the contract has been accepted by the agent
 * @param fulfilled        Whether the contract has been fulfilled
 * @param expiration       Deprecated in favor of deadlineToAccept
 * @param deadlineToAccept The time at which the contract is no longer available to be accepted
 */
case class Contract(id: String,
                    factionSymbol: FactionSymbol,
                    `type`: ContractType,
                    terms: ContractTerms,
                    accepted: Boolean,
                    fulfilled: Boolean,
                    @deprecated("Deprecated in favor of deadlineToAccept")
                    expiration: Instant,
                    deadlineToAccept: Instant) {

  def getDescription: String = {
    new StringBuilder()
      .append(s"Contract $id: ${`type`} of ")
      .append(
        terms
          .deliver
          .map(cargo => s"${cargo.unitsRequired} units of ${cargo.tradeSymbol} to ${cargo.destinationSymbol} ")
          .mkString(", ")
      )
      .append(s"until ${terms.deadline}")
      .toString()
  }
}

/**
 * Type of contract.
 */
sealed trait ContractType extends EnumEntry

//noinspection ScalaUnusedSymbol
case object ContractType extends Enum[ContractType] with CirceEnum[ContractType] {
  case object PROCUREMENT extends ContractType

  case object TRANSPORT extends ContractType

  case object SHUTTLE extends ContractType

  //noinspection TypeAnnotation
  val values = findValues
}

/**
 * The terms to fulfill the contract.
 *
 * @param deadline The deadline for the contract.
 * @param payment  Payments for the contract.
 * @param deliver  The cargo that needs to be delivered to fulfill the contract.
 */
case class ContractTerms(deadline: Instant,
                         payment: ContractPayment,
                         deliver: List[ContractDeliverGood])

/**
 * Payments for the contract.
 *
 * @param onAccepted  The amount of credits received up front for accepting the contract.
 * @param onFulfilled The amount of credits received when the contract is fulfilled.
 */
case class ContractPayment(onAccepted: Int,
                           onFulfilled: Int)

/**
 * The details of a delivery contract. Includes the type of good, units needed, and the destination.
 *
 * @param tradeSymbol       The symbol of the trade good to deliver.
 * @param destinationSymbol The destination where goods need to be delivered.
 * @param unitsRequired     The number of units that need to be delivered on this contract.
 * @param unitsFulfilled    The number of units fulfilled on this contract.
 */
case class ContractDeliverGood(tradeSymbol: String,
                               destinationSymbol: String,
                               unitsRequired: Int,
                               unitsFulfilled: Int)
