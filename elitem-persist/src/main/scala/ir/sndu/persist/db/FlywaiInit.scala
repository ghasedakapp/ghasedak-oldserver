package ir.sndu.persist.db

import javax.sql.DataSource

import org.flywaydb.core.Flyway

trait FlywaiInit {
  def initFlyway(ds: DataSource): Flyway = {
    val flyway = new Flyway()
    flyway.setLocations("sql.migration")
    flyway.setDataSource(ds)
    flyway
  }

}
