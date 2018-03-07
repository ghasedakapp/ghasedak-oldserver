logLevel := Level.Warn

resolvers += "Flyway" at "https://davidmweber.github.io/flyway-sbt.repo"

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "4.2.0")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")



