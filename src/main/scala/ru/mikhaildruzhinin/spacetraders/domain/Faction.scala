package ru.mikhaildruzhinin.spacetraders.domain

import enumeratum._

/**
 * Faction details.
 *
 * @param symbol       The symbol of the faction.
 * @param name         Name of the faction.
 * @param description  Description of the faction.
 * @param headquarters The waypoint in which the faction's HQ is located in.
 * @param traits       List of traits that define this faction.
 * @param isRecruiting Whether or not the faction is currently recruiting new agents.
 */
case class Faction(symbol: FactionSymbol,
                   name: String,
                   description: String,
                   headquarters: String,
                   traits: List[FactionTrait],
                   isRecruiting: Boolean)

/**
 * The symbol of the faction.
 */
sealed trait FactionSymbol extends EnumEntry

//noinspection ScalaUnusedSymbol
case object FactionSymbol extends Enum[FactionSymbol] with CirceEnum[FactionSymbol] {
  case object COSMIC extends FactionSymbol

  case object VOID extends FactionSymbol

  case object GALACTIC extends FactionSymbol

  case object QUANTUM extends FactionSymbol

  case object DOMINION extends FactionSymbol

  case object ASTRO extends FactionSymbol

  case object CORSAIRS extends FactionSymbol

  case object OBSIDIAN extends FactionSymbol

  case object AEGIS extends FactionSymbol

  case object UNITED extends FactionSymbol

  case object SOLITARY extends FactionSymbol

  case object COBALT extends FactionSymbol

  case object OMEGA extends FactionSymbol

  case object ECHO extends FactionSymbol

  case object LORDS extends FactionSymbol

  case object CULT extends FactionSymbol

  case object ANCIENTS extends FactionSymbol

  case object SHADOW extends FactionSymbol

  case object ETHEREAL extends FactionSymbol

  //noinspection TypeAnnotation
  val values = findValues
}

/**
 * @param symbol      The unique identifier of the trait.
 * @param name        The name of the trait
 * @param description A description of the trait.
 */
case class FactionTrait(symbol: FactionTraitSymbol,
                        name: String,
                        description: String)

/**
 * The unique identifier of the trait.
 */
sealed trait FactionTraitSymbol extends EnumEntry

//noinspection ScalaUnusedSymbol
case object FactionTraitSymbol extends Enum[FactionTraitSymbol] with CirceEnum[FactionTraitSymbol] {
  case object BUREAUCRATIC extends FactionTraitSymbol

  case object SECRETIVE extends FactionTraitSymbol

  case object CAPITALISTIC extends FactionTraitSymbol

  case object INDUSTRIOUS extends FactionTraitSymbol

  case object PEACEFUL extends FactionTraitSymbol

  case object DISTRUSTFUL extends FactionTraitSymbol

  case object WELCOMING extends FactionTraitSymbol

  case object SMUGGLERS extends FactionTraitSymbol

  case object SCAVENGERS extends FactionTraitSymbol

  case object REBELLIOUS extends FactionTraitSymbol

  case object EXILES extends FactionTraitSymbol

  case object PIRATES extends FactionTraitSymbol

  case object RAIDERS extends FactionTraitSymbol

  case object CLAN extends FactionTraitSymbol

  case object GUILD extends FactionTraitSymbol

  case object DOMINION extends FactionTraitSymbol

  case object FRINGE extends FactionTraitSymbol

  case object FORSAKEN extends FactionTraitSymbol

  case object ISOLATED extends FactionTraitSymbol

  case object LOCALIZED extends FactionTraitSymbol

  case object ESTABLISHED extends FactionTraitSymbol

  case object NOTABLE extends FactionTraitSymbol

  case object DOMINANT extends FactionTraitSymbol

  case object INESCAPABLE extends FactionTraitSymbol

  case object INNOVATIVE extends FactionTraitSymbol

  case object BOLD extends FactionTraitSymbol

  case object VISIONARY extends FactionTraitSymbol

  case object CURIOUS extends FactionTraitSymbol

  case object DARING extends FactionTraitSymbol

  case object EXPLORATORY extends FactionTraitSymbol

  case object RESOURCEFUL extends FactionTraitSymbol

  case object FLEXIBLE extends FactionTraitSymbol

  case object COOPERATIVE extends FactionTraitSymbol

  case object UNITED extends FactionTraitSymbol

  case object STRATEGIC extends FactionTraitSymbol

  case object INTELLIGENT extends FactionTraitSymbol

  case object RESEARCH_FOCUSED extends FactionTraitSymbol

  case object COLLABORATIVE extends FactionTraitSymbol

  case object PROGRESSIVE extends FactionTraitSymbol

  case object MILITARISTIC extends FactionTraitSymbol

  case object TECHNOLOGICALLY_ADVANCED extends FactionTraitSymbol

  case object AGGRESSIVE extends FactionTraitSymbol

  case object IMPERIALISTIC extends FactionTraitSymbol

  case object TREASURE_HUNTERS extends FactionTraitSymbol

  case object DEXTEROUS extends FactionTraitSymbol

  case object UNPREDICTABLE extends FactionTraitSymbol

  case object BRUTAL extends FactionTraitSymbol

  case object FLEETING extends FactionTraitSymbol

  case object ADAPTABLE extends FactionTraitSymbol

  case object SELF_SUFFICIENT extends FactionTraitSymbol

  case object DEFENSIVE extends FactionTraitSymbol

  case object PROUD extends FactionTraitSymbol

  case object DIVERSE extends FactionTraitSymbol

  case object INDEPENDENT extends FactionTraitSymbol

  case object SELF_INTERESTED extends FactionTraitSymbol

  case object FRAGMENTED extends FactionTraitSymbol

  case object COMMERCIAL extends FactionTraitSymbol

  case object FREE_MARKETS extends FactionTraitSymbol

  case object ENTREPRENEURIAL extends FactionTraitSymbol

  //noinspection TypeAnnotation
  val values = findValues
}
