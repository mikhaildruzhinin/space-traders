package ru.mikhaildruzhinin.spacetraders

import ru.mikhaildruzhinin.spacetraders.Schemas._
import ru.mikhaildruzhinin.spacetraders.client._
import ru.mikhaildruzhinin.spacetraders.domain._

import scala.util.{Failure, Try}

class Service (defaultClient: DefaultClient,
               agentClient: AgentClient,
               systemClient: SystemClient,
               contractClient: ContractClient) {

  def register(registrationRequest: RegistrationRequest): Try[RegistrationResponse] = defaultClient
    .register(registrationRequest)

  def getAgent()(implicit token: String): Try[GetAgentResponse] = agentClient.getAgent()

  def getWaypoint(waypointSymbol: String)
                 (implicit token: String): Try[GetWaypointResponse] = Waypoint
    .parseSymbol(waypointSymbol)
    .fold(
      error => Failure(error),
      parsedWaypointSymbol => systemClient.getWaypoint(
        systemSymbol = parsedWaypointSymbol._2,
        waypointSymbol = waypointSymbol
      )
    )

  def getWaypoint(systemSymbol: String, waypointSymbol: String)
                 (implicit token: String): Try[GetWaypointResponse] = systemClient
    .getWaypoint(systemSymbol, waypointSymbol)

  def getAllContracts(limit: Int = 10, page: Int = 1)
                     (implicit token: String): Try[GetAllContractResponse] = contractClient.getAllContracts(limit, page)

  def acceptContract(contractId: String)
                    (implicit token: String): Try[AcceptContractResponse] = contractClient.acceptContract(contractId)

  def getAllWaypoints(systemSymbol: String,
                      limit: Int = 10,
                      page: Int = 1,
                      waypointTraitSymbols: Option[Seq[WaypointTraitSymbol]] = None,
                      waypointType: Option[WaypointType] = None)
                     (implicit token: String): Try[GetAllWaypointsResponse] = systemClient.getAllWaypoints(
    systemSymbol,
    limit,
    page,
    waypointTraitSymbols,
    waypointType
  )
}
