package ir.sndu.server

import ir.sndu.server.command.ElitemCli
import picocli.CommandLine

object CliMain extends App {
  CommandLine.run(new ElitemCli(), args: _*)
}

