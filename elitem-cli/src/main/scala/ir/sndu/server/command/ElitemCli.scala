package ir.sndu.server.command

import picocli.CommandLine.{ Command, HelpCommand }

@Command(
  name = "elitem",
  version = Array("Elitem v0.1 demo"),
  mixinStandardHelpOptions = true, // add --help and --version options
  description = Array("@|bold Elitem|@ @|underline messenger|@ demo"),
  subcommands = Array(
    classOf[HelpCommand],
    classOf[LoginOrSignup],
    classOf[ClearCache]))
class ElitemCli extends Runnable {

  @picocli.CommandLine.Option(
    names = Array("-d", "--database"),
    description = Array("Databse file path"))
  private var database: String = _

  def run(): Unit = {
  }
}