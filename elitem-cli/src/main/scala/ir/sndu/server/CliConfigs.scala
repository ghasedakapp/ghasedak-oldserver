package ir.sndu.server

import ir.sndu.server.config.{ AppType, ElitemConfigFactory }

object CliConfigs {
  private val conf = ElitemConfigFactory.load(AppType.Cli)
  val host = conf.getString("elitem.host")
  val port = conf.getInt("elitem.port")
}
