package ir.sndu.server

import ir.sndu.server.config.{ AppType, ElitemConfigFactory }

import scala.util.Try

object CliConfigs {
  private val conf = ElitemConfigFactory.load(AppType.Cli)
  val host = conf.getString("elitem.host")
  val port = conf.getInt("elitem.port")
  var dbPath = Try(conf.getString("services.leveldb.path")).getOrElse(".")
}
