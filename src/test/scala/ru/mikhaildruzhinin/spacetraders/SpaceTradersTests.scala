package ru.mikhaildruzhinin.spacetraders

import org.scalatest.funsuite.AnyFunSuite
import ru.mikhaildruzhinin.spacetraders.Client.DefaultClient
import ru.mikhaildruzhinin.spacetraders.RequestSchemas.RegistrationRequestSchema
import ru.mikhaildruzhinin.spacetraders.domain.FactionDomain.FactionSymbol
import sttp.client3._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SpaceTradersTests extends AnyFunSuite {
  test("register") {
    val callSign = s"test${DateTimeFormatter.ofPattern("HHmmss").format(LocalDateTime.now())}"
    val registrationRequestSchema = RegistrationRequestSchema(callSign, FactionSymbol.COSMIC)
    val backend = HttpClientSyncBackend()
    val registrationResponse = DefaultClient
      .register(registrationRequestSchema)
      .send(backend)
      .body
      .fold(
        error => {
          println(error)
          sys.exit(1)
        },
        v => v
      )
    println(registrationResponse)
  }
}
