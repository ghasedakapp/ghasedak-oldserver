package ir.sndu.server

import ir.sndu.server.command.ElitemCmd
import ir.sndu.server.config.{ AppType, ElitemConfigFactory }
import picocli.CommandLine

object CliMain extends App {
  val conf = ElitemConfigFactory.load(AppType.Cli)

  CommandLine.run(new ElitemCmd(), args: _*)
}

