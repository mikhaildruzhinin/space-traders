package ru.mikhaildruzhinin.spacetraders.domain

import enumeratum._

/**
 * A waypoint is a location that ships can travel to such as a Planet, Moon or Space Station.
 *
 * @param symbol The symbol of the waypoint.
 * @param `type` The type of waypoint.
 * @param systemSymbol The symbol of the system.
 * @param x Relative position of the waypoint on the system's x axis. This is not an absolute position in the universe.
 * @param y Relative position of the waypoint on the system's y axis. This is not an absolute position in the universe.
 * @param orbitals Waypoints that orbit this waypoint.
 * @param orbits The symbol of the parent waypoint, if this waypoint is in orbit around another waypoint.
 *               Otherwise this value is undefined.
 * @param faction The faction that controls the waypoint.
 * @param traits The traits of the waypoint.
 * @param modifiers The modifiers of the waypoint.
 * @param chart The chart of a system or waypoint, which makes the location visible to other agents.
 * @param isUnderConstruction True if the waypoint is under construction.
 */
case class Waypoint(symbol: String,
                    `type`: WaypointType,
                    systemSymbol: String,
                    x: Int,
                    y: Int,
                    orbitals: List[WaypointOrbital],
                    orbits: Option[String],
                    faction: Option[WaypointFaction],
                    traits: List[WaypointTrait],
                    modifiers: Option[List[WaypointModifier]],
                    chart: Option[Chart],
                    isUnderConstruction: Boolean)

/**
 * The type of waypoint.
 */
sealed trait WaypointType extends EnumEntry

//noinspection ScalaUnusedSymbol
case object WaypointType extends Enum[WaypointType] with CirceEnum[WaypointType] {
  case object PLANET extends WaypointType

  case object GAS_GIANT extends WaypointType

  case object MOON extends WaypointType

  case object ORBITAL_STATION extends WaypointType

  case object JUMP_GATE extends WaypointType

  case object ASTEROID_FIELD extends WaypointType

  case object ASTEROID extends WaypointType

  case object ENGINEERED_ASTEROID extends WaypointType

  case object ASTEROID_BASE extends WaypointType

  case object NEBULA extends WaypointType

  case object DEBRIS_FIELD extends WaypointType

  case object GRAVITY_WELL extends WaypointType

  case object ARTIFICIAL_GRAVITY_WELL extends WaypointType

  case object FUEL_STATION extends WaypointType

  val values = findValues
}

/**
 * An orbital is another waypoint that orbits a parent waypoint.
 *
 * @param symbol The symbol of the orbiting waypoint.
 */
case class WaypointOrbital(symbol: String)

/**
 * The faction that controls the waypoint.
 *
 * @param symbol The symbol of the faction.
 */
case class WaypointFaction(symbol: FactionSymbol)

/**
 * The trait of the waypoint.
 *
 * @param symbol The unique identifier of the trait.
 * @param name The name of the trait.
 * @param description A description of the trait.
 */
case class WaypointTrait(symbol: WaypointTraitSymbol,
                         name: String,
                         description: String)

/**
 * The unique identifier of the trait.
 */
sealed trait WaypointTraitSymbol extends EnumEntry

//noinspection ScalaUnusedSymbol
object WaypointTraitSymbol extends Enum [WaypointTraitSymbol] with CirceEnum[WaypointTraitSymbol] {
  case object UNCHARTED extends WaypointTraitSymbol

  case object UNDER_CONSTRUCTION extends WaypointTraitSymbol

  case object MARKETPLACE extends WaypointTraitSymbol

  case object SHIPYARD extends WaypointTraitSymbol

  case object OUTPOST extends WaypointTraitSymbol

  case object SCATTERED_SETTLEMENTS extends WaypointTraitSymbol

  case object SPRAWLING_CITIES extends WaypointTraitSymbol

  case object MEGA_STRUCTURES extends WaypointTraitSymbol

  case object PIRATE_BASE extends WaypointTraitSymbol

  case object OVERCROWDED extends WaypointTraitSymbol

  case object HIGH_TECH extends WaypointTraitSymbol

  case object CORRUPT extends WaypointTraitSymbol

  case object BUREAUCRATIC extends WaypointTraitSymbol

  case object TRADING_HUB extends WaypointTraitSymbol

  case object INDUSTRIAL extends WaypointTraitSymbol

  case object BLACK_MARKET extends WaypointTraitSymbol

  case object RESEARCH_FACILITY extends WaypointTraitSymbol

  case object MILITARY_BASE extends WaypointTraitSymbol

  case object SURVEILLANCE_OUTPOST extends WaypointTraitSymbol

  case object EXPLORATION_OUTPOST extends WaypointTraitSymbol

  case object MINERAL_DEPOSITS extends WaypointTraitSymbol

  case object COMMON_METAL_DEPOSITS extends WaypointTraitSymbol

  case object PRECIOUS_METAL_DEPOSITS extends WaypointTraitSymbol

  case object RARE_METAL_DEPOSITS extends WaypointTraitSymbol

  case object METHANE_POOLS extends WaypointTraitSymbol

  case object ICE_CRYSTALS extends WaypointTraitSymbol

  case object EXPLOSIVE_GASES extends WaypointTraitSymbol

  case object STRONG_MAGNETOSPHERE extends WaypointTraitSymbol

  case object VIBRANT_AURORAS extends WaypointTraitSymbol

  case object SALT_FLATS extends WaypointTraitSymbol

  case object CANYONS extends WaypointTraitSymbol

  case object PERPETUAL_DAYLIGHT extends WaypointTraitSymbol

  case object PERPETUAL_OVERCAST extends WaypointTraitSymbol

  case object DRY_SEABEDS extends WaypointTraitSymbol

  case object MAGMA_SEAS extends WaypointTraitSymbol

  case object SUPERVOLCANOES extends WaypointTraitSymbol

  case object ASH_CLOUDS extends WaypointTraitSymbol

  case object VAST_RUINS extends WaypointTraitSymbol

  case object MUTATED_FLORA extends WaypointTraitSymbol

  case object TERRAFORMED extends WaypointTraitSymbol

  case object EXTREME_TEMPERATURES extends WaypointTraitSymbol

  case object EXTREME_PRESSURE extends WaypointTraitSymbol

  case object DIVERSE_LIFE extends WaypointTraitSymbol

  case object SCARCE_LIFE extends WaypointTraitSymbol

  case object FOSSILS extends WaypointTraitSymbol

  case object WEAK_GRAVITY extends WaypointTraitSymbol

  case object STRONG_GRAVITY extends WaypointTraitSymbol

  case object CRUSHING_GRAVITY extends WaypointTraitSymbol

  case object TOXIC_ATMOSPHERE extends WaypointTraitSymbol

  case object CORROSIVE_ATMOSPHERE extends WaypointTraitSymbol

  case object BREATHABLE_ATMOSPHERE extends WaypointTraitSymbol

  case object THIN_ATMOSPHERE extends WaypointTraitSymbol

  case object JOVIAN extends WaypointTraitSymbol

  case object ROCKY extends WaypointTraitSymbol

  case object VOLCANIC extends WaypointTraitSymbol

  case object FROZEN extends WaypointTraitSymbol

  case object SWAMP extends WaypointTraitSymbol

  case object BARREN extends WaypointTraitSymbol

  case object TEMPERATE extends WaypointTraitSymbol

  case object JUNGLE extends WaypointTraitSymbol

  case object OCEAN extends WaypointTraitSymbol

  case object RADIOACTIVE extends WaypointTraitSymbol

  case object MICRO_GRAVITY_ANOMALIES extends WaypointTraitSymbol

  case object DEBRIS_CLUSTER extends WaypointTraitSymbol

  case object DEEP_CRATERS extends WaypointTraitSymbol

  case object SHALLOW_CRATERS extends WaypointTraitSymbol

  case object UNSTABLE_COMPOSITION extends WaypointTraitSymbol

  case object HOLLOWED_INTERIOR extends WaypointTraitSymbol

  case object STRIPPED extends WaypointTraitSymbol

  val values = findValues
}

/**
 * The modifier of the waypoint.
 *
 * @param symbol The unique identifier of the modifier.
 * @param name The name of the modifier.
 * @param description A description of modifier.
 */
case class WaypointModifier(symbol: WaypointModifierSymbol,
                            name: String,
                            description: String)

/**
 * The unique identifier of the modifier.
 */
sealed trait WaypointModifierSymbol extends EnumEntry

//noinspection ScalaUnusedSymbol
object WaypointModifierSymbol extends Enum[WaypointModifierSymbol] with CirceEnum[WaypointModifierSymbol] {
  case object STRIPPED extends WaypointModifierSymbol

  case object UNSTABLE extends WaypointModifierSymbol

  case object RADIATION_LEAK extends WaypointModifierSymbol

  case object CRITICAL_LIMIT extends WaypointModifierSymbol

  case object CIVIL_UNREST extends WaypointModifierSymbol

  val values = findValues
}
