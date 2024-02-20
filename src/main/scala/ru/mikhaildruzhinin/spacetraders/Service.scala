package ru.mikhaildruzhinin.spacetraders

import ru.mikhaildruzhinin.spacetraders.Client.{AgentClient, DefaultClient}
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

  def getAgent()(implicit backend: SttpBackend[Identity, Any], token: String) = {
    AgentClient
      .getAgent(token)
      .send(backend)
      .body
  }
}
