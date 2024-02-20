package ru.mikhaildruzhinin.spacetraders

import org.scalatest.funsuite.AnyFunSuite
import ru.mikhaildruzhinin.spacetraders.RequestSchemas._
import ru.mikhaildruzhinin.spacetraders.domain.FactionSymbol
import sttp.client3._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SpaceTradersTests extends AnyFunSuite {
  test("register") {
    val callSign = s"test${DateTimeFormatter.ofPattern("HHmmss").format(LocalDateTime.now())}"
    val registrationRequestSchema = RegistrationRequestSchema(callSign, FactionSymbol.COSMIC)
    implicit val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

    assert(Service.register(registrationRequestSchema).isRight)
  }
}
