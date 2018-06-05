package ir.sndu.server

import ir.sndu.server.config.ElitemConfigFactory

object CliConfigs {
  private val conf = ElitemConfigFactory.load("cli.conf")
  val host = conf.getString("elitem.host")
  val port = conf.getInt("elitem.port")
}
