package ir.sndu.server

import ir.sndu.server.config.{ AppType, ElitemConfigFactory }

object Main extends App {

  val config = ElitemConfigFactory.load(AppType.Server)

  ElitemServerBuilder.start(config)

}