package ru.mikhaildruzhinin.spacetraders

import ru.mikhaildruzhinin.spacetraders.domain.FactionSymbol

object RequestSchemas {

  /**
   * @param symbol Your desired agent symbol. This will be a unique name used to represent your agent,
   *               and will be the prefix for your ships.
   * @param faction The symbol of the faction.
   * @param email Your email address. This is used if you reserved your call sign between resets.
   */
  case class RegistrationRequestSchema(symbol: String,
                                       faction: FactionSymbol,
                                       email: Option[String] = None)
}
