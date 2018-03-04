import ir.sndu.Dependencies

name := "elitem"

scalaSource in ProtocPlugin.ProtobufConfig := sourceManaged.value

lazy val commonSettings = Seq(
  organization := "ir.elitem",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.3",
  mainClass in Compile := Some("ir.sndu.server.Main"),
  PB.targets in Compile := {
    println((sourceManaged  in Compile).value)
    println((sourceManaged).value)
    Seq(
      scalapb.gen() -> (sourceManaged  in Compile).value
    )
  },
  {
    println(scalaSource)
    println(scalaSource in ProtocPlugin.ProtobufConfig)
    scalaSource in ProtocPlugin.ProtobufConfig := sourceManaged.value
  }

)


lazy val root = (project in file("."))
  .settings(
    commonSettings
  )
  .dependsOn(commons, core, rpc)
  .aggregate(core,persist,rpc, commons, model)

lazy val core = elitemModule("elitem-core")
  .settings(
    libraryDependencies ++= Dependencies.core
  ).dependsOn(model)

lazy val persist = elitemModule("elitem-persist")
  .settings(
    libraryDependencies ++= Dependencies.persist
  ).dependsOn(model)

lazy val rpc = elitemModule("elitem-rpc")
  .settings(
    libraryDependencies ++= Dependencies.rpc
  ).dependsOn(core, persist)

lazy val model = elitemModule("elitem-model")
  .settings(
    libraryDependencies ++= Dependencies.model
  )

lazy val commons = elitemModule("elitem-commons")
  .settings(
    libraryDependencies ++= Dependencies.commons
  )

lazy val test = elitemModule("elitem-test")
  .settings(
    libraryDependencies ++= Dependencies.test
  )
  .dependsOn(commons, core, rpc, persist)


def elitemModule(name: String): Project = {
  Project(id = name, base = file(name))
    .settings(commonSettings)
}