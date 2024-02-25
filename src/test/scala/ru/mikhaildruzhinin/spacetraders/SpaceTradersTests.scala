package ru.mikhaildruzhinin.spacetraders

import org.scalatest.funsuite.AnyFunSuite
import ru.mikhaildruzhinin.spacetraders.RequestSchemas.RegistrationRequestSchema
import ru.mikhaildruzhinin.spacetraders.domain.FactionSymbol
import sttp.client3._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

class SpaceTradersTests extends AnyFunSuite {
  test("register") {
    val callSign = s"test${DateTimeFormatter.ofPattern("HHmmss").format(LocalDateTime.now())}"
    val registrationRequestSchema = RegistrationRequestSchema(callSign, FactionSymbol.COSMIC)
    implicit val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

    for {
      token <- Service.register(registrationRequestSchema).map(_.data.token)
      agent <- Service.getAgent(token).map(_.data)
      _ <- Try(println(agent.headquarters))
    } yield ()
  }
}
