import com.typesafe.sbt.packager.Keys.bashScriptExtraDefines
import ir.sndu.Dependencies
import scalariform.formatter.preferences._

name := "elitem"

scalaSource in ProtocPlugin.ProtobufConfig := sourceManaged.value

enablePlugins(JavaAppPackaging)



lazy val commonSettings = Seq(
  organization := "ir.elitem",
  scalaVersion := "2.12.3",
  mainClass in Compile := Some("ir.sndu.server.Main"),
  PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged  in Compile).value),
  PB.includePaths in Compile ++= Seq(
    file("elitem-model/src/main/protobuf")
  ),
    scalaSource in ProtocPlugin.ProtobufConfig := sourceManaged.value,
  scalariformPreferences := scalariformPreferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(DoubleIndentConstructorArguments, true)
    .setPreference(DanglingCloseParenthesis, Preserve)
)


lazy val root = (project in file("."))
  .settings(
    commonSettings,
    Packaging.packagingSettings
  )
  .dependsOn(commons, core, rpc, cli)
  .aggregate(core,persist,rpc, commons, model, cli)

lazy val core = elitemModule("elitem-core")
  .settings(
    libraryDependencies ++= Dependencies.core
  ).dependsOn(model, persist)

lazy val persist = elitemModule("elitem-persist")
  .settings(
    libraryDependencies ++= Dependencies.persist
  ).dependsOn(model, commons)

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

lazy val cli = elitemModule("elitem-cli")
  .settings(
    libraryDependencies ++= Dependencies.cli,
    PB.protoSources in Compile ++= Seq(
      file("elitem-rpc/src/main/protobuf")
    ),
    mainClass in Compile := Some("ir.sndu.server.CliMain")
  )
  .dependsOn(model, commons)

lazy val test = elitemModule("elitem-test")
  .settings(
    libraryDependencies ++= Dependencies.test
  )
  .dependsOn(root, commons, core, rpc, persist, model)


def elitemModule(name: String): Project = {
  Project(id = name, base = file(name))
    .settings(commonSettings)
}