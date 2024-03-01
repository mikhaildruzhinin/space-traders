package ru.mikhaildruzhinin.spacetraders

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.funsuite.AnyFunSuite
import ru.mikhaildruzhinin.spacetraders.Schemas.RegistrationRequest
import ru.mikhaildruzhinin.spacetraders.client._
import sttp.client3._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

class SpaceTradersTests extends AnyFunSuite with StrictLogging {
  test("quickstart") {
    import com.softwaremill.macwire._

    val callSign = s"test${DateTimeFormatter.ofPattern("HHmmss").format(LocalDateTime.now())}"
    val registrationRequestSchema = RegistrationRequest(callSign)

    implicit lazy val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()
    lazy val defaultClient = wire[DefaultClient]
    lazy val agentClient = wire[AgentClient]
    lazy val systemClient = wire[SystemClient]
    lazy val contractClient = wire[ContractClient]
    lazy val service: Service = wire[Service]

    val r = for {
      token <- service.register(registrationRequestSchema).map(_.data.token)
      agent <- service.getAgent()(token).map(_.data)
      currentLocation <- service.getWaypoint(agent.headquarters)(token).map(_.data)
      _ <- Try { logger.info(s"Current location: ${currentLocation.symbol}") }

      contracts <- service.getAllContracts()(token).map(_.data)
      _ <- Try { contracts.foreach(contract => logger.info(contract.getDescription)) }
      contractId <- contracts.headOption.toRight(new NoSuchElementException).toTry.map(_.id)
      acceptedContract <- service.acceptContract(contractId)(token).map(_.data.contract)
      _ <- Try { logger.info(s"Accepted contract ${acceptedContract.id}") }
    } yield ()

    assert(r.isSuccess)
  }
}
