resolvers += "Flyway" at "https://davidmweber.github.io/flyway-sbt.repo"

// todo: remove this after akka grpc power api version
resolvers += Resolver.url("ghasedak-repo", url("https://raw.github.com/ghasedakapp/ghasedak-repositories/master"))(Resolver.ivyStylePatterns)

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "4.2.0")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.2")

// todo: change this after akka grpc power api version
addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "0.4.3-ghasedak" withSources())



