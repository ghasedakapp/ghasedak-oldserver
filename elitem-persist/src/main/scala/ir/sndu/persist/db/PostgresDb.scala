package ir.sndu.persist.db

import slick.jdbc.DataSourceJdbcDataSource
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.hikaricp.HikariCPJdbcDataSource

object PostgresDb extends FlywaiInit {
  val db = Database.forConfig("services.postgresql")
  val ds = db.source match {
    case s: HikariCPJdbcDataSource ⇒ s.ds
    case s: DataSourceJdbcDataSource ⇒ s.ds
    case s ⇒ throw new IllegalArgumentException(s"Unknown DataSource: ${s.getClass.getName}")
  }

  val flyway = initFlyway(ds)

  flyway.migrate()

}
