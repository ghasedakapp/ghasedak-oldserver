package im.ghasedak.server.config

import java.io.File
import java.nio.file.{ Files, Paths }

import com.typesafe.config.{ Config, ConfigFactory }
import im.ghasedak.server.config.AppType.AppType

object AppType extends Enumeration {
  type AppType = Value
  val Cli, Server, Test = Value
}

object GhasedakConfigFactory {
  def load(app: AppType): Config = {
    val (configPath, logbackPath) = getPathes(app)

    setConfig(configPath)
    setLogback(logbackPath)

    System.setProperty("file.encoding", "UTF-8")

    ConfigFactory.load()
  }

  private def getPathes(app: AppType): (String, String) = {
    val path = new File(".").getCanonicalPath
    app match {
      case AppType.Server ⇒ (path + "/conf/server.conf", path + "/conf/logback.xml")
      case AppType.Cli    ⇒ (path + "/conf/cli/cli.conf", path + "/conf/cli/logback.xml")
      case AppType.Test   ⇒ (path + "/conf/test/test.conf", path + "/conf/test/logback.xml")
    }
  }

  private def setConfig(configPath: String): Unit = {
    if (Files.exists(Paths.get(configPath))) {
      System.setProperty("config.file", configPath)
    }
  }

  private def setLogback(logbackPath: String): Unit = {
    if (Files.exists(Paths.get(logbackPath))) {
      System.setProperty("logback.configurationFile", logbackPath)
    }
  }

}
