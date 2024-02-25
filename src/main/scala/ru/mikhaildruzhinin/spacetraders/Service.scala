package ru.mikhaildruzhinin.spacetraders

import io.circe.{Error=>CirceError}
import ru.mikhaildruzhinin.spacetraders.Client.{AgentClient, DefaultClient}
import ru.mikhaildruzhinin.spacetraders.RequestSchemas.RegistrationRequestSchema
import sttp.client3.{Identity, RequestT, ResponseException, SttpBackend}

import scala.util.Try

object Service {
  private implicit class Request[A](request: RequestT[Identity, Either[ResponseException[String, CirceError], A], Any]) {
    def sendRequest()(implicit backend: SttpBackend[Identity, Any]): Try[A] = request
      .send(backend)
      .body
      .toTry
  }

  def register(registrationRequestSchema: RegistrationRequestSchema)
              (implicit backend: SttpBackend[Identity, Any]) = {

    DefaultClient
      .register(registrationRequestSchema)
      .sendRequest()
  }

  def getAgent(token: String)(implicit backend: SttpBackend[Identity, Any]) = {
    AgentClient
      .getAgent(token)
      .sendRequest()
  }
}
