package ru.mikhaildruzhinin.spacetraders

import io.circe.{Error=>CirceError}
import io.circe.generic.auto._
import ru.mikhaildruzhinin.spacetraders.Schemas._
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
    def register(registrationRequestSchema: RegistrationRequest): RequestT[Identity, Either[ResponseException[String, CirceError], RegistrationResponse], Any] = {

      basicRequest
        .post(uri"$baseUrl/register")
        .headers(Map("Accept" -> "application/json", "Content-Type" -> "application/json"))
        .body(registrationRequestSchema)
        .response(asJson[RegistrationResponse])
    }
  }

  object AgentClient extends BaseClient {
    /**
     * Fetch your agent's details.
     *
     * @param token A private bearer token which grants authorization to use the API.
     * @return Agent details.
     */
    def getAgent()(implicit token: String): RequestT[Identity, Either[ResponseException[String, CirceError], GetAgentResponse], Any] = {

      basicRequest
        .get(uri"$baseUrl/my/agent")
        .headers(Map("Accept" -> "application/json", "Authorization" -> s"Bearer $token"))
        .response(asJson[GetAgentResponse])
    }
  }

  object SystemClient extends BaseClient {
    /**
     * View the details of a waypoint.
     *
     * If the waypoint is uncharted, it will return the 'Uncharted' trait instead of its actual traits.
     *
     * @param systemSymbol The system symbol
     * @param waypointSymbol The waypoint symbol
     * @param token A private bearer token which grants authorization to use the API.
     * @return The waypoint.
     */
    def getWaypoint(systemSymbol: String,
                    waypointSymbol: String)
                   (implicit token: String): RequestT[Identity, Either[ResponseException[String, CirceError], GetWaypointResponse], Any] = {

      basicRequest
        .get(uri"$baseUrl/systems/$systemSymbol/waypoints/$waypointSymbol")
        .headers(Map("Accept" -> "application/json", "Authorization" -> s"Bearer $token"))
        .response(asJson[GetWaypointResponse])
    }
  }

  object ContractClient extends BaseClient {
    /**
     * Return a paginated list of all your contracts.
     *
     * @param limit How many entries to return per page
     * @param page What entry offset to request
     * @param token A private bearer token which grants authorization to use the API.
     * @return A paginated list of all your contracts.
     */
    def getAllContracts(limit: Int, page: Int)
                       (implicit token: String): RequestT[Identity, Either[ResponseException[String, CirceError], GetAllContractResponse], Any] = {

      val queryParams = Map(
        "limit" -> limit,
        "page" -> page
      )

      basicRequest
        .get(uri"$baseUrl/my/contracts?$queryParams")
        .headers(Map("Accept" -> "application/json", "Authorization" -> s"Bearer $token"))
        .response(asJson[GetAllContractResponse])
    }

    /**
     * Accept a contract by ID.
     *
     * You can only accept contracts that were offered to you, were not accepted yet,
     * and whose deadlines has not passed yet.
     *
     * @param contractId The contract ID to accept.
     * @param token A private bearer token which grants authorization to use the API.
     * @return Successfully accepted contract.
     */
    def acceptContract(contractId: String)
                      (implicit token: String): RequestT[Identity, Either[ResponseException[String, CirceError], AcceptContractResponse], Any] = {

      basicRequest
        .post(uri"$baseUrl/my/contracts/$contractId/accept")
        .headers(Map("Accept" -> "application/json", "Authorization" -> s"Bearer $token"))
        .response(asJson[AcceptContractResponse])
    }
  }
}
