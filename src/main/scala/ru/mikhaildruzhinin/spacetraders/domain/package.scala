package ru.mikhaildruzhinin.spacetraders

import enumeratum._

import java.time.Instant

package object domain {
  /**
   * The activity level of a trade good. If the good is an import, this represents how strong consumption is.
   * If the good is an export, this represents how strong the production is for the good. When activity is strong,
   * consumption or production is near maximum capacity. When activity is weak, consumption or production
   * is near minimum capacity.
   */
  sealed trait ActivityLevel extends EnumEntry

  //noinspection ScalaUnusedSymbol
  case object ActivityLevel extends Enum[ActivityLevel] with CirceEnum[ActivityLevel] {
    case object WEAK extends ActivityLevel

    case object GROWING extends ActivityLevel

    case object STRONG extends ActivityLevel

    case object RESTRICTED extends ActivityLevel

    val values = findValues
  }

  /**
   * Agent details.
   *
   * @param accountId       Account ID that is tied to this agent. Only included on your own agent.
   * @param symbol          Symbol of the agent.
   * @param headquarters    The headquarters of the agent.
   * @param credits         The number of credits the agent has available. Credits can be negative if funds have been overdrawn.
   * @param startingFaction The faction the agent started with.
   * @param shipCount       How many ships are owned by the agent.
   */
  case class Agent(accountId: String,
                   symbol: String,
                   headquarters: String,
                   credits: Long,
                   startingFaction: FactionSymbol,
                   shipCount: Int)

  /**
   * The chart of a system or waypoint, which makes the location visible to other agents.
   *
   * @param waypointSymbol The symbol of the waypoint.
   * @param submittedBy    The agent that submitted the chart for this waypoint.
   * @param submittedOn    The time the chart for this waypoint was submitted.
   */
  case class Chart(waypointSymbol: String,
                   submittedBy: String,
                   submittedOn: Instant)

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
}
