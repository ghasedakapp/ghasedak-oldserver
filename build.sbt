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
    scalapb.gen() -> (sourceManaged in Compile).value),
  PB.includePaths in Compile ++= Seq(
    file("elitem-model/src/main/protobuf"),
    file("elitem-sdk/src/main/protobuf")
  ),
  scalaSource in ProtocPlugin.ProtobufConfig := sourceManaged.value,
  scalariformPreferences := scalariformPreferences.value
    .setPreference(RewriteArrowSymbols, true)
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(SpacesAroundMultiImports, true)
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    Packaging.packagingSettings
  )
  .dependsOn(model, sdk, struct, core, rpc, persist, commons, runtime, test, cli)
  .aggregate(model, sdk, struct, core, rpc, persist, commons, runtime, test, cli)

// Every protobuf that transfer between client and server
lazy val sdk = elitemModule("elitem-sdk")
  .settings(
    libraryDependencies ++= Dependencies.sdk
  )

// Every protobuf that transfer between just servers
lazy val struct = elitemModule("elitem-struct")
  .settings(
    libraryDependencies ++= Dependencies.struct
  )
  .dependsOn(sdk)

lazy val model = elitemModule("elitem-model")
  .settings(
    libraryDependencies ++= Dependencies.model
  )
  .dependsOn(sdk, struct)

lazy val core = elitemModule("elitem-core")
  .settings(
    libraryDependencies ++= Dependencies.core
  ).dependsOn(persist)

lazy val rpc = elitemModule("elitem-rpc")
  .settings(
    libraryDependencies ++= Dependencies.rpc
  ).dependsOn(core, persist)

lazy val persist = elitemModule("elitem-persist")
  .settings(
    libraryDependencies ++= Dependencies.persist
  ).dependsOn(model, commons)

lazy val commons = elitemModule("elitem-commons")
  .settings(
    libraryDependencies ++= Dependencies.commons
  )

lazy val runtime = elitemModule("elitem-runtime")
  .dependsOn(model, sdk, struct, core, rpc, persist, commons)

lazy val test = elitemModule("elitem-test")
  .settings(
    libraryDependencies ++= Dependencies.test
  )
  .dependsOn(runtime)

lazy val cli = elitemModule("elitem-cli")
  .settings(
    libraryDependencies ++= Dependencies.cli,
    PB.protoSources in Compile ++= Seq(
      file("elitem-concurrent/src/main/protobuf")
    ),
    mainClass in Compile := Some("ir.sndu.server.CliMain")
  )
  .dependsOn(runtime)

def elitemModule(name: String): Project = {
  Project(id = name, base = file(name))
    .settings(commonSettings)
}