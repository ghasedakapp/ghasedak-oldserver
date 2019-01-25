import im.ghasedak.BuildSettings._
import im.ghasedak.Dependencies

name := "ghasedak"

enablePlugins(JavaAppPackaging)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    packagingSettings
  )
  .dependsOn(model, sdk, core, update, rpc, persist, commons, runtime, test)
  .aggregate(model, sdk, core, update, rpc, persist, commons, runtime, test)

// Every protobuf that transfer between client and server
lazy val sdk = ghasedakModule("ghasedak-sdk")
  .settings(
    libraryDependencies ++= Dependencies.sdk,
    akkaGrpcCodeGeneratorSettings += "server_power_apis",
    akkaGrpcGeneratedSources := Seq(AkkaGrpc.Server, AkkaGrpc.Client),
    // todo: remove this after akka grpc power api version
    resolvers += Resolver.url("ghasedak-repo", url("https://raw.github.com/ghasedakapp/ghasedak-repositories/master"))(Resolver.ivyStylePatterns)
  )
  .enablePlugins(AkkaGrpcPlugin)

lazy val model = ghasedakModule("ghasedak-model")
  .settings(
    libraryDependencies ++= Dependencies.model
  )
  .dependsOn(sdk)

lazy val core = ghasedakModule("ghasedak-core")
  .settings(
    libraryDependencies ++= Dependencies.core
  )
  .dependsOn(persist, update)

lazy val update = ghasedakModule("ghasedak-update")
  .settings(
    libraryDependencies ++= Dependencies.update
  )
  .dependsOn(sdk, model, commons)
  .enablePlugins(AkkaGrpcPlugin)

lazy val rpc = ghasedakModule("ghasedak-rpc")
  .settings(
    libraryDependencies ++= Dependencies.rpc
  )
  .dependsOn(core, persist)

lazy val persist = ghasedakModule("ghasedak-persist")
  .settings(
    libraryDependencies ++= Dependencies.persist
  )
  .dependsOn(model, commons)

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

def ghasedakModule(name: String): Project =
  Project(id = name, base = file(name))
    .settings(commonSettings)