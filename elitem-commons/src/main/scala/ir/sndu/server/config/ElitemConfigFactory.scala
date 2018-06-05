package ir.sndu.server.config

import java.io.File

import com.typesafe.config.{ Config, ConfigFactory }

object ElitemConfigFactory {
  def load(fileName: String): Config = {
    val configPath = new File(".").getCanonicalPath + s"/conf/$fileName"
    System.setProperty("config.file", configPath.toString)
    System.setProperty("file.encoding", "UTF-8")
    ConfigFactory.load()
  }

}
