package ir.sndu

import sbt._


object Dependencies {
  object V {
    val akka = "2.5.8"
    val slick = "3.2.1"
    val slickPg = "0.16.0"
    val scalapb = "0.7.1"
    val postgres = "42.2.1"
    val flyway = "5.0.7"
  }

  object Compile {

    val actor =  "com.typesafe.akka" %% "akka-actor" % V.akka
    val cluster =  "com.typesafe.akka" %% "akka-cluster" % V.akka
    val sharding =  "com.typesafe.akka" %% "akka-cluster-sharding" % V.akka
    val akkaPersist =  "com.typesafe.akka" %% "akka-persistence" % V.akka
    val akkaSlf4j =  "com.typesafe.akka" %% "akka-slf4j" % V.akka



    val postgres = "org.postgresql" % "postgresql" % V.postgres
    val slick  = "com.typesafe.slick" %% "slick" % V.slick
    val slickPg = "com.github.tminglei" %% "slick-pg" % V.slickPg
    val hikariCp = "com.typesafe.slick" %% "slick-hikaricp" % V.slick
    val flyway = "org.flywaydb" % "flyway-core" % V.flyway


    val scalap = "com.thesamet.scalapb" %% "compilerplugin" % V.scalapb
    val scalapbRuntime = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
    val grpc = Seq("io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion)

    val betterFile = "com.github.pathikrit" %% "better-files" % "3.4.0"

    val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

  }

  object Test {
    val scalatic = "org.scalactic" %% "scalactic" % "3.0.5"
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    val akkaTest = "com.typesafe.akka" %% "akka-testkit" % V.akka % "test"
  }


  import Compile._
  import Test._

  val shared = Seq(betterFile, logback)

  val core = shared ++ Seq(
    actor,cluster,sharding,akkaPersist,
    scalap, scalapbRuntime, akkaSlf4j
  )

  val persist = shared ++ Seq(
      slick, postgres, hikariCp, flyway, slickPg
    )

  val rpc = shared ++ grpc ++ Seq()
  val model = shared

  val commons = shared

  val test = shared ++ Seq(scalatic, scalaTest, akkaTest)

}