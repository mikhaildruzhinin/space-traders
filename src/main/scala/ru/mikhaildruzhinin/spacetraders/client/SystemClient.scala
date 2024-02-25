package ru.mikhaildruzhinin.spacetraders.client

import io.circe.generic.auto._
import ru.mikhaildruzhinin.spacetraders.Schemas._
import sttp.client3._
import sttp.client3.circe._

import scala.util.Try

class SystemClient private (implicit backend: SttpBackend[Identity, Any]) extends BaseClient {
  /**
   * View the details of a waypoint.
   *
   * If the waypoint is uncharted, it will return the 'Uncharted' trait instead of its actual traits.
   *
   * @param systemSymbol   The system symbol
   * @param waypointSymbol The waypoint symbol
   * @param token          A private bearer token which grants authorization to use the API.
   * @return The waypoint.
   */
  def getWaypoint(systemSymbol: String,
                  waypointSymbol: String)
                 (implicit token: String): Try[GetWaypointResponse] = basicRequest
      .get(uri"$baseUrl/systems/$systemSymbol/waypoints/$waypointSymbol")
      .headers(Map("Accept" -> "application/json", "Authorization" -> s"Bearer $token"))
      .response(asJson[GetWaypointResponse])
      .sendRequest()
}

object SystemClient {
  def apply(backend: SttpBackend[Identity, Any]) = new SystemClient()(backend)
}
