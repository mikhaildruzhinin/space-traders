package ru.mikhaildruzhinin.spacetraders.client

import io.circe.generic.auto._
import ru.mikhaildruzhinin.spacetraders.Schemas._
import ru.mikhaildruzhinin.spacetraders.domain._
import sttp.client3._
import sttp.client3.circe._

import scala.util.Try

class SystemClient (implicit backend: SttpBackend[Identity, Any]) extends BaseClient {
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

  /**
   * Return a paginated list of all of the waypoints for a given system.
   *
   * If a waypoint is uncharted, it will return the Uncharted trait instead of its actual traits.
   *
   * @param systemSymbol The system symbol
   * @param limit How many entries to return per page
   * @param page What entry offset to request
   * @param waypointTraitSymbols Filter waypoints by one or more traits.
   * @param waypointType Filter waypoints by type.
   * @param token A private bearer token which grants authorization to use the API.
   * @return A paginated list of all of the waypoints for a given system.
   */
  def getAllWaypoints(systemSymbol: String,
                      limit: Int,
                      page: Int,
                      waypointTraitSymbols: Option[Seq[WaypointTraitSymbol]] = None,
                      waypointType: Option[WaypointType] = None)
                     (implicit token: String): Try[GetAllWaypointsResponse] = {

    val queryParams = Map(
      "limit" -> limit,
      "page" -> page,
      "type" -> waypointType
    )

    val waypointTraitParams: Seq[WaypointTraitSymbol] = waypointTraitSymbols
      .fold(Seq.empty[WaypointTraitSymbol])(x => x)

    basicRequest
      .get(uri"$baseUrl/systems/$systemSymbol/waypoints?$queryParams&traits=$waypointTraitParams")
      .headers(Map("Accept" -> "application/json", "Authorization" -> s"Bearer $token"))
      .response(asJson[GetAllWaypointsResponse])
      .sendRequest()
  }
}
