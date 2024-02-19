package ru.mikhaildruzhinin.spacetraders

import ru.mikhaildruzhinin.spacetraders.Client.DefaultClient
import ru.mikhaildruzhinin.spacetraders.RequestSchemas.RegistrationRequestSchema
import sttp.client3.{Identity, SttpBackend}

object Service {
  def register(registrationRequestSchema: RegistrationRequestSchema)
              (implicit backend: SttpBackend[Identity, Any]) = {

    DefaultClient
      .register(registrationRequestSchema)
      .send(backend)
      .body
  }
}
