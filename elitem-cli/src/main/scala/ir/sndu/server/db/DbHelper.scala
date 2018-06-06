package ir.sndu.server.db

import java.io._

import ir.sndu.server.ElitemConsole.withColor
import org.iq80.leveldb.{ DB, _ }
import org.iq80.leveldb.impl.Iq80DBFactory._

object DbHelper {

  def leveldb(path: Option[String] = None)(f: DB => Unit)(implicit log: org.slf4j.Logger) = {
    val options = new Options()
    options.createIfMissing(true)
    val db = factory.open(new File(path.getOrElse(".") + "/session"), options)
    try {
      f(db)
    } catch {
      case e: Throwable =>
        log.error(e.getMessage, e)
        withColor(scala.Console.RED) {
          System.err.println(s"Error: ${e.getMessage}")
        }
    } finally {
      db.close()
    }

  }

  implicit class RichLevelDB(db: DB) {
    def put(key: String, value: String): Unit = db.put(bytes(key), bytes(value))
    def get(key: String): Option[String] = Option(asString(db.get(bytes(key))))
  }
}
