package ir.sndu.persist.db

import javax.sql.DataSource
import org.flywaydb.core.Flyway
import slick.jdbc.{ DataSourceJdbcDataSource, PostgresProfile }
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.hikaricp.HikariCPJdbcDataSource

object PostgresDb extends FlywayInit {

  val db: PostgresProfile.backend.Database = Database.forConfig("services.postgresql")

  val ds: DataSource = db.source match {
    case s: HikariCPJdbcDataSource   ⇒ s.ds
    case s: DataSourceJdbcDataSource ⇒ s.ds
    case s                           ⇒ throw new IllegalArgumentException(s"Unknown DataSource: ${s.getClass.getName}")
  }

  val flyway: Flyway = initFlyway(ds)

  flyway.migrate()

}
