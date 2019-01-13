package im.ghasedak

import sbt._

object Dependencies {

  object V {
    val akka = "2.5.19"
    val slick = "3.2.1"
    val slickPg = "0.16.0"
    val postgres = "42.2.1"
    val flyway = "5.0.7"
    val config = "1.3.2"
    val pulsar4s = "2.2.0"
  }

  object Compile {
    val actor = "com.typesafe.akka" %% "akka-actor" % V.akka
    val cluster = "com.typesafe.akka" %% "akka-cluster" % V.akka
    val sharding = "com.typesafe.akka" %% "akka-cluster-sharding" % V.akka
    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % V.akka

    val config = "com.typesafe" % "config" % V.config

    val postgres = "org.postgresql" % "postgresql" % V.postgres
    val slick = "com.typesafe.slick" %% "slick" % V.slick
    val slickPg = "com.github.tminglei" %% "slick-pg" % V.slickPg
    val hikariCp = "com.typesafe.slick" %% "slick-hikaricp" % V.slick
    val flyway = "org.flywaydb" % "flyway-core" % V.flyway

    val scalapbRuntime = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
    val grpc = Seq(
      "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
    )

    val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

    val jwt = "com.auth0" % "java-jwt" % "3.4.1"

    val libPhoneNumber = "com.googlecode.libphonenumber" % "libphonenumber" % "7.0.+"
    val cats = "org.typelevel" %% "cats-core" % "1.5.0"

    val caffeine = "com.github.ben-manes.caffeine" % "caffeine" % "2.6.2"

    val pulsar4s = Seq(
      "com.sksamuel.pulsar4s" %% "pulsar4s-core" % V.pulsar4s,
      "com.sksamuel.pulsar4s" %% "pulsar4s-akka-streams" % V.pulsar4s,
      "com.sksamuel.pulsar4s" %% "pulsar4s-circe" % V.pulsar4s,
      "com.sksamuel.pulsar4s" %% "pulsar4s-json4s" % V.pulsar4s,
      "com.sksamuel.pulsar4s" %% "pulsar4s-jackson" % V.pulsar4s,
      "com.sksamuel.pulsar4s" %% "pulsar4s-spray-json" % V.pulsar4s,
      "com.sksamuel.pulsar4s" %% "pulsar4s-play-json" % V.pulsar4s,
      "com.sksamuel.pulsar4s" %% "pulsar4s-monix" % V.pulsar4s,
      "com.sksamuel.pulsar4s" %% "pulsar4s-scalaz" % V.pulsar4s,
      "com.sksamuel.pulsar4s" %% "pulsar4s-cats-effect" % V.pulsar4s
    )
  }

  object Test {
    val scalatic = "org.scalactic" %% "scalactic" % "3.0.5"
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % V.akka % "test"
  }

  import Compile._
  import Test._

  val shared = Seq(
    scalapbRuntime,
    logback
  )

  val sdk: Seq[ModuleID] = shared ++ grpc

  val model: Seq[ModuleID] = shared ++ Seq(
    config
  )

  val core: Seq[ModuleID] = shared ++ Seq(
    actor,
    cluster,
    sharding,
    akkaSlf4j,
    caffeine
  )

  val update: Seq[ModuleID] = shared ++ Seq(
    actor
  ) ++ pulsar4s

  val rpc: Seq[ModuleID] = shared ++ Seq(
    jwt
  ) ++ grpc

  val persist: Seq[ModuleID] = shared ++ Seq(
    actor,
    slick,
    postgres,
    hikariCp,
    flyway,
    slickPg
  )

  val commons: Seq[ModuleID] = shared ++ Seq(
    cats,
    slick,
    config,
    libPhoneNumber
  )

  val test: Seq[ModuleID] = shared ++ Seq(
    scalatic,
    scalaTest,
    akkaTestkit
  )

}