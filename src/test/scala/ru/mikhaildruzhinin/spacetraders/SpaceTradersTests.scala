package ru.mikhaildruzhinin.spacetraders

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.funsuite.AnyFunSuite
import ru.mikhaildruzhinin.spacetraders.Schemas.RegistrationRequest
import ru.mikhaildruzhinin.spacetraders.domain._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Try}

class SpaceTradersTests extends AnyFunSuite with Wired with StrictLogging {
  implicit class ConvertibleOption[A](option: Option[A]) {
    def toTry(errorMessage: String): Try[A] = option.toRight(new RuntimeException(errorMessage)).toTry
  }

  test("quickstart") {
    val callSign = s"test${DateTimeFormatter.ofPattern("HHmmss").format(LocalDateTime.now())}"
    val registrationRequestSchema = RegistrationRequest(callSign)

    val r = for {
      token <- service.register(registrationRequestSchema).map(_.data.token)
      agent <- service.getAgent()(token).map(_.data)
      (_, systemSymbol) <- Waypoint.parseSymbol(agent.headquarters)
      currentLocation <- service.getWaypoint(systemSymbol, agent.headquarters)(token).map(_.data)
      _ = logger.info(s"Current location: ${currentLocation.getLocation}")

      contracts <- service.getAllContracts()(token).map(_.data)
      _ = contracts.foreach(contract => logger.info(contract.getDescription))
      contractId <- contracts.headOption.toTry("No contracts available").map(_.id)
      acceptedContract <- service.acceptContract(contractId)(token).map(_.data.contract)
      _ = logger.info(s"Accepted contract ${acceptedContract.id}")

      ships <- service.getAllShips()(token).map(_.data)
      satellite <- ships.find(_.registration.role == ShipRole.SATELLITE).toTry("No satellite available")
      shipyards <- service.getAllWaypoints(
          systemSymbol = systemSymbol,
          waypointTraitSymbols = Some(Seq(WaypointTraitSymbol.SHIPYARD))
        )(token).map(_.data)
      s <- shipyards.find(_.symbol == satellite.nav.waypointSymbol).toTry("No shipyard available")
      _ = logger.info(s"Purchasing ship at location: ${s.getLocation})")
    } yield ()

    assert(
      r.recoverWith {
          case exception => logger.error(exception.getMessage); Failure(exception)
      }.isSuccess
    )
  }
}
