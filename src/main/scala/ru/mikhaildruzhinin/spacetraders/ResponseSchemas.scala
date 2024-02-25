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

  /**
   * Succesfully listed contracts.
   *
   * @param data Contract details.
   * @param meta Meta details for pagination.
   */
  case class GetAllContractResponseSchema(data: List[Contract],
                                          meta: GetAllContractResponseMetadata)

  /**
   * Meta details for pagination.
   *
   * @param total Shows the total amount of items of this kind that exist.
   * @param page A page denotes an amount of items, offset from the first item.
   *             Each page holds an amount of items equal to the limit.
   * @param limit The amount of items in each page. Limits how many items can be fetched at once.
   */
  case class GetAllContractResponseMetadata(total: Int,
                                            page: Int,
                                            limit: Int)
}
