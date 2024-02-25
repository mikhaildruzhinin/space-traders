package ru.mikhaildruzhinin.spacetraders

import ru.mikhaildruzhinin.spacetraders.domain._

object Schemas {
  /**
   * @param symbol  Your desired agent symbol. This will be a unique name used to represent your agent,
   *                and will be the prefix for your ships.
   * @param faction The symbol of the faction.
   * @param email   Your email address. This is used if you reserved your call sign between resets.
   */
  case class RegistrationRequest(symbol: String,
                                 faction: FactionSymbol = FactionSymbol.COSMIC,
                                 email: Option[String] = None)

  /**
   * Successfully registered.
   */
  case class RegistrationResponse(data: RegistrationResponseData)

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
  case class GetAgentResponse(data: Agent)

  /**
   * Successfully fetched waypoint.
   *
   * @param data A waypoint is a location that ships can travel to such as a Planet, Moon or Space Station.
   */
  case class GetWaypointResponse(data: Waypoint)

  /**
   * Succesfully listed contracts.
   *
   * @param data Contract details.
   * @param meta Meta details for pagination.
   */
  case class GetAllContractResponse(data: List[Contract],
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

  /**
   * Succesfully accepted contract.
   */
  case class AcceptContractResponse(data: AcceptContractResponseData)

  /**
   * Succesfully accepted contract.
   *
   * @param agent Agent details.
   * @param contract Contract details.
   */
  case class AcceptContractResponseData(agent: Agent,
                                        contract: Contract)
}
