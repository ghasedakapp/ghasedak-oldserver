package ir.sndu.server.command

import java.io.File

import ir.sndu.server.utils.FileHelper
import picocli.CommandLine.Command

@Command(
  name = "clearcache",
  description = Array("Clear DB"))
class ClearCache extends Runnable {

  override def run(): Unit = {
    FileHelper.deleteRecursively(new File("./session"))
  }
}

