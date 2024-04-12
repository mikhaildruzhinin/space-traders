package ru.mikhaildruzhinin.spacetraders

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.funsuite.AnyFunSuite
import ru.mikhaildruzhinin.spacetraders.Schemas.RegistrationRequest
import ru.mikhaildruzhinin.spacetraders.client._
import ru.mikhaildruzhinin.spacetraders.domain._
import sttp.client3._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Try}

class SpaceTradersTests extends AnyFunSuite with StrictLogging {
  implicit class ConvertibleOption[A](option: Option[A]) {
    def toTry(errorMessage: String): Try[A] = option.toRight(new RuntimeException(errorMessage)).toTry
  }

  test("quickstart") {
    import com.softwaremill.macwire._

    val callSign = s"test${DateTimeFormatter.ofPattern("HHmmss").format(LocalDateTime.now())}"
    val registrationRequestSchema = RegistrationRequest(callSign)

    implicit lazy val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()
    lazy val defaultClient = wire[DefaultClient]
    lazy val agentClient = wire[AgentClient]
    lazy val systemClient = wire[SystemClient]
    lazy val contractClient = wire[ContractClient]
    lazy val fleetClient = wire[FleetClient]
    lazy val service: Service = wire[Service]

    val r = for {
      token <- service.register(registrationRequestSchema).map(_.data.token)
      agent <- service.getAgent()(token).map(_.data)
      (_, systemSymbol) <- Waypoint.parseSymbol(agent.headquarters)
      currentLocation <- service.getWaypoint(systemSymbol, agent.headquarters)(token).map(_.data)
      _ <- Try { logger.info(s"Current location: ${currentLocation.symbol}") }

      contracts <- service.getAllContracts()(token).map(_.data)
      _ <- Try { contracts.foreach(contract => logger.info(contract.getDescription)) }
      contractId <- contracts.headOption.toTry("test").map(_.id)
      acceptedContract <- service.acceptContract(contractId)(token).map(_.data.contract)
      _ <- Try { logger.info(s"Accepted contract ${acceptedContract.id}") }

      ships <- service.getAllShips()(token).map(_.data)
      satellite <- ships.find(_.registration.role == ShipRole.SATELLITE).toTry("test")
      shipyards <- service.getAllWaypoints(
          systemSymbol = systemSymbol,
          waypointTraitSymbols = Some(Seq(WaypointTraitSymbol.SHIPYARD))
        )(token).map(_.data)
      s <- shipyards.find(_.symbol == satellite.nav.waypointSymbol).toTry("test")
      _ <- Try { logger.info(Seq(s.symbol, s.x, s.y).mkString(", ")) }
    } yield ()

    assert(
      r.recoverWith { case exception => logger.error(exception.getMessage); Failure(exception) }
        .isSuccess
    )
  }
}
