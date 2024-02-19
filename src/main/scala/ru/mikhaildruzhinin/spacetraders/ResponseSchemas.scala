package ru.mikhaildruzhinin.spacetraders

import ru.mikhaildruzhinin.spacetraders.domain.Agent
import ru.mikhaildruzhinin.spacetraders.domain.ContractDomain.Contract
import ru.mikhaildruzhinin.spacetraders.domain.FactionDomain.Faction
import ru.mikhaildruzhinin.spacetraders.domain.ShipDomain.Ship

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
}
