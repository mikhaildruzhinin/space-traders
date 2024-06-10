package ru.mikhaildruzhinin.spacetraders

import com.softwaremill.macwire._
import ru.mikhaildruzhinin.spacetraders.client._
import sttp.client3._

trait Wired {
  implicit lazy val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()
  lazy val defaultClient: DefaultClient = wire[DefaultClient]
  lazy val agentClient: AgentClient = wire[AgentClient]
  lazy val systemClient: SystemClient = wire[SystemClient]
  lazy val contractClient: ContractClient = wire[ContractClient]
  lazy val fleetClient: FleetClient = wire[FleetClient]
  lazy val service: Service = wire[Service]
}
