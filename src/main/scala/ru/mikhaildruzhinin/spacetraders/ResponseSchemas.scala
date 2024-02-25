package ru.mikhaildruzhinin.spacetraders

import ru.mikhaildruzhinin.spacetraders.domain._

object ResponseSchemas {
  /**
   * Successfully registered.
   */
  case class RegistrationResponseSchema(data: RegistrationResponseData)

  /**
   * Successfully registered.
   *
   * @param agent Agent details.
   * @param contract Contract details.
   * @param faction Faction details.
   * @param ship Ship details.
   * @param token A Bearer token for accessing secured API endpoints.
   */
  case class RegistrationResponseData(agent: Agent,
                                      contract: Contract,
                                      faction: Faction,
                                      ship: Ship,
                                      token: String)

  /**
   * Successfully fetched agent details.
   *
   * @param data Agent details.
   */
  case class GetAgentResponseSchema(data: Agent)

  /**
   * Successfully fetched waypoint.
   *
   * @param data A waypoint is a location that ships can travel to such as a Planet, Moon or Space Station.
   */
  case class GetWaypointResponseSchema(data: Waypoint)
}
