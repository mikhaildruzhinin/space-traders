package ru.mikhaildruzhinin.spacetraders

import io.circe.{Error=>CirceError}
import io.circe.generic.auto._
import ru.mikhaildruzhinin.spacetraders.RequestSchemas._
import ru.mikhaildruzhinin.spacetraders.ResponseSchemas._
import sttp.client3._
import sttp.client3.circe._

object Client {
  trait BaseClient {
    protected val baseUrl: String = "https://api.spacetraders.io/v2/"
  }

  object DefaultClient extends BaseClient {
    /**
     * Creates a new agent and ties it to an account.
     * The agent symbol must consist of a 3-14 character string, and will be used to represent your agent.
     * This symbol will prefix the symbol of every ship you own. Agent symbols will be cast to all uppercase characters.
     *
     * This new agent will be tied to a starting faction of your choice, which determines your starting location,
     * and will be granted an authorization token, a contract with their starting faction,
     * a command ship that can fly across space with advanced capabilities,
     * a small probe ship that can be used for reconnaissance, and 150,000 credits.
     *
     * Keep your token safe and secure
     * Save your token during the alpha phase. There is no way to regenerate this token without starting a new agent.
     * In the future you will be able to generate and manage your tokens from the SpaceTraders website.
     *
     * If you are new to SpaceTraders, It is recommended to register with the COSMIC faction,
     * a faction that is well connected to the rest of the universe.
     * After registering, you should try our interactive quickstart guide which will walk you through basic API requests
     * in just a few minutes.
     *
     * @param registrationRequestSchema
     * @return
     */
    def register(registrationRequestSchema: RegistrationRequestSchema): RequestT[Identity, Either[ResponseException[String, CirceError], RegistrationResponseSchema], Any] = {

      basicRequest
        .post(uri"${baseUrl}/register")
        .headers(Map("Accept" -> "application/json", "Content-Type" -> "application/json"))
        .body(registrationRequestSchema)
        .response(asJson[RegistrationResponseSchema])
    }
  }

  object AgentClient extends BaseClient {
    /**
     * Fetch your agent's details.
     *
     * @param token A private bearer token which grants authorization to use the API.
     * @return
     */
    def getAgent(implicit token: String): RequestT[Identity, Either[ResponseException[String, CirceError], GetAgentResponseSchema], Any] = {

      basicRequest
        .get(uri"${baseUrl}/my/agent")
        .headers(Map("Accept" -> "application/json", "Authorization" -> s"Bearer $token"))
        .response(asJson[GetAgentResponseSchema])
    }
  }
}
