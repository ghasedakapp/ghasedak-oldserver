package ir.sndu.server.db

import java.io._

import ir.sndu.server.CliConfigs
import ir.sndu.server.ElitemConsole.{ withColor, withWarning }
import ir.sndu.server.command.ElitemCmd
import org.iq80.leveldb.{ DB, _ }
import org.iq80.leveldb.impl.Iq80DBFactory._

object DbHelper {
  private def overwriteDbPath(parent: ElitemCmd): Unit = {
    Option(parent.database) match {
      case Some(path) =>
        withWarning(println("Overwriting db path..."))
        CliConfigs.dbPath = path
      case None =>
    }
  }
  def leveldb[T](f: DB => T)(
    implicit
    log: org.slf4j.Logger,
    parent: ElitemCmd): Option[T] = {
    overwriteDbPath(parent)
    val path = CliConfigs.dbPath
    val options = new Options()
    options.createIfMissing(true)
    val db = factory.open(new File(s"$path/session"), options)
    try {
      Some(f(db))
    } catch {
      case e: Throwable =>
        log.error(e.getMessage, e)
        withColor(scala.Console.RED) {
          System.err.println(s"Error: ${e.getMessage}")
        }
        None
    } finally {
      db.close()
      None
    }

  }

  implicit class RichLevelDB(db: DB) {
    def put(key: String, value: String): Unit = db.put(bytes(key), bytes(value))
    def get(key: String): Option[String] = Option(asString(db.get(bytes(key))))
    def getBytes(key: String): Option[Array[Byte]] = Option(db.get(bytes(key)))
    def putBytes(key: String, value: Array[Byte]): Unit = db.put(bytes(key), value)
  }
}
