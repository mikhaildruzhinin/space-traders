package ru.mikhaildruzhinin.spacetraders.client

import io.circe.{Error => CirceError}
import sttp.client3._

import scala.util.Try

trait BaseClient {
  protected implicit class Request[A](request: RequestT[Identity, Either[ResponseException[String, CirceError], A], Any]) {
    def sendRequest()(implicit backend: SttpBackend[Identity, Any]): Try[A] = request
      .send(backend)
      .body
      .toTry
  }

  protected val baseUrl: String = "https://api.spacetraders.io/v2/"

  protected def getDefaultHeaders(token: String): Map[String, String] = {
    Map("Accept" -> "application/json", "Authorization" -> s"Bearer $token")
  }
}
