package ru.mikhaildruzhinin.spacetraders.client

import io.circe.generic.auto._
import ru.mikhaildruzhinin.spacetraders.Schemas._
import sttp.client3._
import sttp.client3.circe._

import scala.util.Try

class ContractClient private (implicit backend: SttpBackend[Identity, Any]) extends BaseClient {
  /**
   * Return a paginated list of all your contracts.
   *
   * @param limit How many entries to return per page
   * @param page What entry offset to request
   * @param token A private bearer token which grants authorization to use the API.
   * @return A paginated list of all your contracts.
   */
  def getAllContracts(limit: Int, page: Int)
                     (implicit token: String): Try[GetAllContractResponse] = {

    val queryParams = Map(
      "limit" -> limit,
      "page" -> page
    )

    basicRequest
      .get(uri"$baseUrl/my/contracts?$queryParams")
      .headers(Map("Accept" -> "application/json", "Authorization" -> s"Bearer $token"))
      .response(asJson[GetAllContractResponse])
      .sendRequest()
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
                    (implicit token: String): Try[AcceptContractResponse] = basicRequest
      .post(uri"$baseUrl/my/contracts/$contractId/accept")
      .headers(Map("Accept" -> "application/json", "Authorization" -> s"Bearer $token"))
      .response(asJson[AcceptContractResponse])
      .sendRequest()
}
object ContractClient {
  def apply(backend: SttpBackend[Identity, Any]) = new ContractClient()(backend)
}
