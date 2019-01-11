package im.ghasedak.server

import im.ghasedak.server.config.{ AppType, GhasedakConfigFactory }

object Main extends App {

  val config = GhasedakConfigFactory.load(AppType.Server)

  GhasedakServerBuilder.start(config)

}