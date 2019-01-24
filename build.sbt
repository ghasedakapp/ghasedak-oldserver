import im.ghasedak.Dependencies
import scalariform.formatter.preferences._

name := "ghasedak"

scalaSource in ProtocPlugin.ProtobufConfig := sourceManaged.value

enablePlugins(JavaAppPackaging)

lazy val commonSettings = Seq(
  organization := "im.ghasedak",
  scalaVersion := "2.12.8",
  mainClass in Compile := Some("im.ghasedak.server.Main"),
//  PB.targets in Compile := Seq(
//    scalapb.gen() -> (sourceManaged in Compile).value),
//  PB.includePaths in Compile ++= Seq(
//    file("ghasedak-sdk/src/main/protobuf")
//  ),
//    scalaSource in ProtocPlugin.ProtobufConfig := sourceManaged.value,
  scalariformPreferences := scalariformPreferences.value
    .setPreference(RewriteArrowSymbols, true)
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(SpacesAroundMultiImports, true),
  parallelExecution in Test := false
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    Packaging.packagingSettings
  )
  .dependsOn(model, sdk, core, update, rpc, persist, commons, runtime, test)
  .aggregate(model, sdk, core, update, rpc, persist, commons, runtime, test)

// Every protobuf that transfer between client and server
lazy val sdk = ghasedakModule("ghasedak-sdk")
  .settings(
    libraryDependencies ++= Dependencies.sdk
  ).enablePlugins(AkkaGrpcPlugin)


lazy val model = ghasedakModule("ghasedak-model")
  .settings(
    libraryDependencies ++= Dependencies.model
  )
  .dependsOn(sdk)

lazy val core = ghasedakModule("ghasedak-core")
  .settings(
    libraryDependencies ++= Dependencies.core
  ).dependsOn(persist, update)

lazy val update = ghasedakModule("ghasedak-update")
  .settings(
    libraryDependencies ++= Dependencies.update
  )
  .dependsOn(sdk, model, commons)

lazy val rpc = ghasedakModule("ghasedak-rpc")
  .settings(
    libraryDependencies ++= Dependencies.rpc
  ).dependsOn(core, persist)

lazy val persist = ghasedakModule("ghasedak-persist")
  .settings(
    libraryDependencies ++= Dependencies.persist
  ).dependsOn(model, commons)

lazy val commons = ghasedakModule("ghasedak-commons")
  .settings(
    libraryDependencies ++= Dependencies.commons
  )

lazy val runtime = ghasedakModule("ghasedak-runtime")
  .dependsOn(model, sdk, core, update, rpc, persist, commons)

lazy val test = ghasedakModule("ghasedak-test")
  .settings(
    libraryDependencies ++= Dependencies.test
  )
  .dependsOn(runtime)

def ghasedakModule(name: String): Project = {
  Project(id = name, base = file(name))
    .settings(commonSettings)
}