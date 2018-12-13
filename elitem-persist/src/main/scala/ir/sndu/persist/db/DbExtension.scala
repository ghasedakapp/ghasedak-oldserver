package ir.sndu.persist.db

import akka.actor.{ ExtendedActorSystem, Extension, ExtensionId }
import javax.sql.DataSource
import org.flywaydb.core.Flyway
import slick.jdbc.PostgresProfile.backend.Database
import slick.jdbc.hikaricp.HikariCPJdbcDataSource
import slick.jdbc.{ DataSourceJdbcDataSource, PostgresProfile }

import scala.util.{ Failure, Success, Try }

object DbExtension extends ExtensionId[DbExtensionImpl] {

  override def createExtension(system: ExtendedActorSystem): DbExtensionImpl = {
    val db: PostgresProfile.backend.Database = Database.forConfig("services.postgresql")
    system.registerOnTermination {
      db.close()
    }
    val ext = new DbExtensionImpl(db)
    val migrationEnable = system.settings.config.getBoolean("services.postgresql.migration.enable")
    if (migrationEnable) {
      Try(ext.migrate()) match {
        case Success(_) ⇒
        case Failure(e) ⇒
          system.log.error(e, "Migration failed")
          throw e
      }
    }
    ext
  }

}

class DbExtensionImpl(val db: Database) extends Extension with FlywayInit {

  private lazy val flyway: Flyway = {
    val ds: DataSource = db.source match {
      case s: HikariCPJdbcDataSource   ⇒ s.ds
      case s: DataSourceJdbcDataSource ⇒ s.ds
      case s                           ⇒ throw new IllegalArgumentException(s"Unknown DataSource: ${s.getClass.getName}")
    }
    initFlyway(ds)
  }

  def clean(): Unit = flyway.clean()

  def migrate(): Unit = flyway.migrate()

}
