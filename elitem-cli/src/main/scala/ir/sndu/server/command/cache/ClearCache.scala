package ir.sndu.server.command.cache

import java.io.File

import ir.sndu.server.CliConfigs
import ir.sndu.server.command.CommandBase
import ir.sndu.server.utils.FileHelper
import picocli.CommandLine.Command

@Command(
  name = "clearcache",
  description = Array("Clear DB"))
class ClearCache extends CommandBase {

  private def getPath: String = Option(parentCmd.database).getOrElse(CliConfigs.dbPath + "/session")

  override def run(): Unit = {
    FileHelper.deleteRecursively(new File(getPath))
  }
}

