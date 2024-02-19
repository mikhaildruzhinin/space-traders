package ru.mikhaildruzhinin.spacetraders

import enumeratum._
import ru.mikhaildruzhinin.spacetraders.domain.FactionDomain.FactionSymbol

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
}
