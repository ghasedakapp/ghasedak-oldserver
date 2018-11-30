package ir.sndu

import sbt._

object Dependencies {

  object V {

    val akka = "2.5.14"
    val slick = "3.2.1"
    val slickPg = "0.16.0"
    val postgres = "42.2.1"
    val flyway = "5.0.7"
    val config = "1.3.2"
    val persistCassandra = "0.87"

  }

  object Compile {

    val akkaTyped = "com.typesafe.akka" %% "akka-actor-typed" % V.akka
    val actor = "com.typesafe.akka" %% "akka-actor" % V.akka
    val cluster = "com.typesafe.akka" %% "akka-cluster" % V.akka
    val sharding = "com.typesafe.akka" %% "akka-cluster-sharding" % V.akka
    val ddata = "com.typesafe.akka" %% "akka-distributed-data" % V.akka
    val shardingTyped = "com.typesafe.akka" %% "akka-cluster-sharding-typed" % V.akka
    val persistTyped = "com.typesafe.akka" %% "akka-persistence-typed" % V.akka
    val akkaPersist = "com.typesafe.akka" %% "akka-persistence" % V.akka
    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % V.akka
    val persistCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % V.persistCassandra

    val config = "com.typesafe" % "config" % V.config

    val postgres = "org.postgresql" % "postgresql" % V.postgres
    val slick = "com.typesafe.slick" %% "slick" % V.slick
    val slickPg = "com.github.tminglei" %% "slick-pg" % V.slickPg
    val hikariCp = "com.typesafe.slick" %% "slick-hikaricp" % V.slick
    val flyway = "org.flywaydb" % "flyway-core" % V.flyway

    val scalapbRuntime = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
    val grpc = Seq("io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion)

    val betterFile = "com.github.pathikrit" %% "better-files" % "3.4.0"

    val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

    val jwt = "com.auth0" % "java-jwt" % "3.4.1"

    val scopt = "com.github.scopt" %% "scopt" % "3.7.0"
    val picoCli = "info.picocli" % "picocli" % "3.0.2"
    val textIo = "org.beryx" % "text-io" % "3.1.3" % "runtime"
    val levelDB = "org.iq80.leveldb" % "leveldb" % "0.10"

  }

  object Test {
    val scalatic = "org.scalactic" %% "scalactic" % "3.0.5"
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    val akkaTest = "com.typesafe.akka" %% "akka-testkit" % V.akka % "test"
    val persistCassTest = "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % V.persistCassandra % "test"
  }

  import Compile._
  import Test._

  val shared = Seq(
    scalapbRuntime,
    betterFile,
    logback
  )

  val sdk: Seq[ModuleID] = shared ++ grpc

  val struct: Seq[ModuleID] = shared ++ Seq(
    actor,
    akkaTyped
  )

  val model: Seq[ModuleID] = shared

  val core: Seq[ModuleID] = shared ++ Seq(
    actor,
    ddata,
    cluster,
    sharding,
    akkaSlf4j,
    akkaTyped,
    akkaPersist,
    shardingTyped,
    persistTyped,
    persistCassandra
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

  val commons: Seq[ModuleID] = shared ++ Seq(config)

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