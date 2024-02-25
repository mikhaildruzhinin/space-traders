package ru.mikhaildruzhinin.spacetraders

import org.scalatest.funsuite.AnyFunSuite
import ru.mikhaildruzhinin.spacetraders.RequestSchemas.RegistrationRequestSchema
import ru.mikhaildruzhinin.spacetraders.domain.FactionSymbol
import sttp.client3._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

class SpaceTradersTests extends AnyFunSuite {
  test("quickstart") {
    val callSign = s"test${DateTimeFormatter.ofPattern("HHmmss").format(LocalDateTime.now())}"
    val registrationRequestSchema = RegistrationRequestSchema(callSign, FactionSymbol.COSMIC)
    val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()
    val service = Service(backend)

    for {
      token <- service.register(registrationRequestSchema).map(_.data.token)
      agent <- service.getAgent(token).map(_.data)
      currentLocation <- service.getWaypoint(agent.headquarters, token).map(_.data)
      _ <- Try { println(currentLocation.symbol) }
      _ <- Try { currentLocation.traits.foreach(println) }
      _ <- Try { currentLocation.modifiers.foreach(println) }
    } yield ()
  }
}
