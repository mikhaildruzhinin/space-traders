package ru.mikhaildruzhinin.spacetraders.client

import io.circe.generic.auto._
import ru.mikhaildruzhinin.spacetraders.Schemas._
import sttp.client3._
import sttp.client3.circe._

import scala.util.Try

class AgentClient (implicit backend: SttpBackend[Identity, Any]) extends BaseClient {
  /**
   * Fetch your agent's details.
   *
   * @param token A private bearer token which grants authorization to use the API.
   * @return Agent details.
   */
  def getAgent()(implicit token: String): Try[GetAgentResponse] = basicRequest
      .get(uri"$baseUrl/my/agent")
      .headers(getDefaultHeaders(token))
      .response(asJson[GetAgentResponse])
      .sendRequest()
}
