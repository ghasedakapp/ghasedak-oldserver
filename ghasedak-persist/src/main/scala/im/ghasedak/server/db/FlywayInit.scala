package im.ghasedak.server.db

import javax.sql.DataSource

import org.flywaydb.core.Flyway

trait FlywayInit {

  def initFlyway(ds: DataSource): Flyway = {
    val flyway = new Flyway()
    flyway.setLocations("sql.migration")
    flyway.setDataSource(ds)
    flyway
  }

}
