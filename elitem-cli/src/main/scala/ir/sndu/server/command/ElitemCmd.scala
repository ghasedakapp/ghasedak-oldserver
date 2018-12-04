package ir.sndu.server.command

import ir.sndu.server.command.auth.LoginOrSignup
import ir.sndu.server.command.cache.ClearCache
import ir.sndu.server.command.dialog.LoadDialog
import ir.sndu.server.command.history.LoadHistory
import ir.sndu.server.command.messaging.SendMesage
import picocli.CommandLine.{ Command, HelpCommand }

@Command(
  name = "elitem",
  version = Array("Elitem v0.1 demo"),
  mixinStandardHelpOptions = true, // add --help and --version options
  description = Array("@|bold Elitem|@ @|underline messenger|@ demo"),
  subcommands = Array(
    classOf[HelpCommand],
    classOf[LoginOrSignup],
    classOf[ClearCache],
    classOf[LoadDialog],
    classOf[SendMesage],
    classOf[LoadHistory]))
class ElitemCmd extends Runnable {

  @picocli.CommandLine.Option(
    names = Array("-d", "--database"),
    description = Array("Database file path"))
  var database: String = _

  def run(): Unit = {
  }
}