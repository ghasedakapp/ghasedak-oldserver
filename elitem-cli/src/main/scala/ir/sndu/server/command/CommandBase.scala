package ir.sndu.server.command

import org.slf4j.LoggerFactory

trait CommandBase extends Runnable {
  protected implicit val log = LoggerFactory.getLogger(getClass)
}
