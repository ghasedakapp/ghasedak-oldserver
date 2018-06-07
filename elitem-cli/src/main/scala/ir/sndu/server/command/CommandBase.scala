package ir.sndu.server.command

import org.slf4j.LoggerFactory
import picocli.CommandLine.ParentCommand

trait CommandBase extends Runnable {
  protected implicit val log = LoggerFactory.getLogger(getClass)

  @ParentCommand
  protected implicit var parentCmd: ElitemCmd = _
}
