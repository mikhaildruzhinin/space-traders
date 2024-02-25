package ru.mikhaildruzhinin.spacetraders

import ru.mikhaildruzhinin.spacetraders.Exceptions._
import ru.mikhaildruzhinin.spacetraders.Schemas._
import ru.mikhaildruzhinin.spacetraders.client._
import sttp.client3._

import scala.util.{Failure, Success, Try}

class Service private (implicit backend: SttpBackend[Identity, Any]) {
  def register(registrationRequest: RegistrationRequest): Try[RegistrationResponse] = DefaultClient(backend)
    .register(registrationRequest)

  def getAgent()(implicit token: String): Try[GetAgentResponse] = AgentClient(backend).getAgent()

  private def parseWaypointSymbol(waypointSymbol: String): Try[(String, String)] = {
    val pattern = """(\S+)-(\S+)-(\S+)""".r
    waypointSymbol match {
      case pattern(sector, system, _) => Success((sector, sector + "-" + system))
      case _ => Failure(
        new WaypointSymbolParsingException(s"Could not parse waypoint symbol: $waypointSymbol with pattern $pattern")
      )
    }
  }

  def getWaypoint(waypointSymbol: String)(implicit token: String): Try[GetWaypointResponse] = {
    parseWaypointSymbol(waypointSymbol)
      .fold(
        error => Failure(error),
        parsedWaypointSymbol => SystemClient(backend).getWaypoint(
          systemSymbol = parsedWaypointSymbol._2,
          waypointSymbol = waypointSymbol
        )
      )
  }

  def getAllContracts(limit: Int = 10, page: Int = 1)
                     (implicit token: String): Try[GetAllContractResponse] = ContractClient(backend)
    .getAllContracts(limit, page)

  def acceptContract(contractId: String)
                    (implicit token: String): Try[AcceptContractResponse] = ContractClient(backend)
    .acceptContract(contractId)
}

object Service {
  def apply(backend: SttpBackend[Identity, Any]) = new Service()(backend)
}
