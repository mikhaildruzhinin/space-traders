package ru.mikhaildruzhinin.spacetraders.client

import io.circe.generic.auto._
import ru.mikhaildruzhinin.spacetraders.Schemas._
import sttp.client3._
import sttp.client3.circe._

import scala.util.Try

class FleetClient (implicit backend: SttpBackend[Identity, Any]) extends BaseClient {
  /**
   * Return a paginated list of all of ships under your agent's ownership.
   *
   * @param token A private bearer token which grants authorization to use the API.
   * @return A paginated list of all of ships under your agent's ownership.
   */
  def getAllShips()(implicit token: String): Try[GetAllShipsResponse] = basicRequest
    .get(uri"$baseUrl/my/ships")
    .headers(getDefaultHeaders(token))
    .response(asJson[GetAllShipsResponse])
    .sendRequest()
}
