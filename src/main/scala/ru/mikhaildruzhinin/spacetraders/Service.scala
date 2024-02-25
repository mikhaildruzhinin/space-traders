package ru.mikhaildruzhinin.spacetraders

import ru.mikhaildruzhinin.spacetraders.Exceptions._
import ru.mikhaildruzhinin.spacetraders.Schemas._
import ru.mikhaildruzhinin.spacetraders.client._

import scala.util.{Failure, Success, Try}

class Service (defaultClient: DefaultClient,
               agentClient: AgentClient,
               systemClient: SystemClient,
               contractClient: ContractClient) {

  def register(registrationRequest: RegistrationRequest): Try[RegistrationResponse] = defaultClient
    .register(registrationRequest)

  def getAgent()(implicit token: String): Try[GetAgentResponse] = agentClient.getAgent()

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
        parsedWaypointSymbol => systemClient.getWaypoint(
          systemSymbol = parsedWaypointSymbol._2,
          waypointSymbol = waypointSymbol
        )
      )
  }

  def getAllContracts(limit: Int = 10, page: Int = 1)
                     (implicit token: String): Try[GetAllContractResponse] = contractClient.getAllContracts(limit, page)

  def acceptContract(contractId: String)
                    (implicit token: String): Try[AcceptContractResponse] = contractClient.acceptContract(contractId)
}
