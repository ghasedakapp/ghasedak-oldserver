package ir.sndu.server

object CliMain extends App {
  val parser = ElitemOptionParser()
  parser.repl(Config())
}
