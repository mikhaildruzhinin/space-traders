package ru.mikhaildruzhinin.spacetraders

import io.circe.{Error => CirceError}
import ru.mikhaildruzhinin.spacetraders.Client._
import ru.mikhaildruzhinin.spacetraders.Exceptions._
import ru.mikhaildruzhinin.spacetraders.Schemas._
import sttp.client3._

import scala.util.{Failure, Success, Try}

class Service private (implicit backend: SttpBackend[Identity, Any]) {
  private implicit class Request[A](request: RequestT[Identity, Either[ResponseException[String, CirceError], A], Any]) {
    def sendRequest()(implicit backend: SttpBackend[Identity, Any]): Try[A] = request
      .send(backend)
      .body
      .toTry
  }

  def register(registrationRequestSchema: RegistrationRequest): Try[RegistrationResponse] = {
    DefaultClient
      .register(registrationRequestSchema)
      .sendRequest()
  }

  def getAgent(token: String): Try[GetAgentResponse] = {
    AgentClient
      .getAgent()(token)
      .sendRequest()
  }

  private def parseWaypointSymbol(waypointSymbol: String): Try[(String, String)] = {
    val pattern = """(\S+)-(\S+)-(\S+)""".r
    waypointSymbol match {
      case pattern(sector, system, _) => Success((sector, sector + "-" + system))
      case _ => Failure(
        new WaypointSymbolParsingException(s"Could not parse waypoint symbol: $waypointSymbol with pattern $pattern")
      )
    }
  }

  def getWaypoint(waypointSymbol: String, token: String): Try[GetWaypointResponse] = {
    parseWaypointSymbol(waypointSymbol)
      .fold(
        error => {
          Failure(error)
        },
        parsedWaypointSymbol => SystemClient
          .getWaypoint(
            systemSymbol = parsedWaypointSymbol._2,
            waypointSymbol = waypointSymbol
          )(token)
          .sendRequest()
      )
  }

  def getAllContracts(limit: Int = 10, page: Int = 1, token: String): Try[GetAllContractResponse] = {
    ContractClient
      .getAllContracts(limit, page)(token)
      .sendRequest()
  }

  def acceptContract(contractId: String, token: String): Try[AcceptContractResponse] = {
    ContractClient
      .acceptContract(contractId)(token)
      .sendRequest()
  }
}

object Service {
  def apply(backend: SttpBackend[Identity, Any]) = new Service()(backend)
}
