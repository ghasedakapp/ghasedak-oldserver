package ir.sndu

import sbt._

object Dependencies {

  object V {
    val akka = "2.5.19"
    val slick = "3.2.1"
    val slickPg = "0.16.0"
    val postgres = "42.2.1"
    val flyway = "5.0.7"
    val config = "1.3.2"
    val persistCassandra = "0.87"
  }

  object Compile {
    val actor = "com.typesafe.akka" %% "akka-actor" % V.akka
    val cluster = "com.typesafe.akka" %% "akka-cluster" % V.akka
    val sharding = "com.typesafe.akka" %% "akka-cluster-sharding" % V.akka
    val ddata = "com.typesafe.akka" %% "akka-distributed-data" % V.akka
    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % V.akka

    val config = "com.typesafe" % "config" % V.config

    val postgres = "org.postgresql" % "postgresql" % V.postgres
    val slick = "com.typesafe.slick" %% "slick" % V.slick
    val slickPg = "com.github.tminglei" %% "slick-pg" % V.slickPg
    val hikariCp = "com.typesafe.slick" %% "slick-hikaricp" % V.slick
    val flyway = "org.flywaydb" % "flyway-core" % V.flyway

    val scalapbRuntime = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
    val grpc = Seq("io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion)

    val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

    val jwt = "com.auth0" % "java-jwt" % "3.4.1"

    val scopt = "com.github.scopt" %% "scopt" % "3.7.0"
    val picoCli = "info.picocli" % "picocli" % "3.0.2"
    val textIo = "org.beryx" % "text-io" % "3.1.3" % "runtime"
    val levelDB = "org.iq80.leveldb" % "leveldb" % "0.10"

    val libPhoneNumber = "com.googlecode.libphonenumber" % "libphonenumber" % "7.0.+"
    val cats = "org.typelevel" %% "cats-core" % "1.5.0"

    val caffeine =   "com.github.ben-manes.caffeine" % "caffeine" % "2.6.2",
  }

  object Test {
    val scalatic = "org.scalactic" %% "scalactic" % "3.0.5"
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    val akkaTest = "com.typesafe.akka" %% "akka-testkit" % V.akka % "test"
  }

  import Compile._
  import Test._

  val shared = Seq(
    scalapbRuntime,
    logback
  )

  val sdk: Seq[ModuleID] = shared ++ grpc

  val struct: Seq[ModuleID] = shared ++ Seq(
    actor
  )

  val model: Seq[ModuleID] = shared

  val core: Seq[ModuleID] = shared ++ Seq(
    actor,
    ddata,
    cluster,
    sharding,
    akkaSlf4j,
    caffeine
  )

  val rpc: Seq[ModuleID] = shared ++ Seq(
    jwt
  ) ++ grpc

  val persist: Seq[ModuleID] = shared ++ Seq(
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
    akkaTest
  )

  val cli: Seq[ModuleID] = shared ++ Seq(
    scopt,
    picoCli,
    textIo,
    levelDB
  ) ++ grpc

}