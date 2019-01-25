package im.ghasedak

import com.typesafe.sbt.SbtScalariform.autoImport.scalariformPreferences
import com.typesafe.sbt.packager.Keys.bashScriptExtraDefines
import sbt.Keys._
import sbt._
import sbtprotoc.ProtocPlugin
import scalariform.formatter.preferences._

object BuildSettings {

  lazy val commonSettings = Seq(
    organization := "im.ghasedak",
    scalaVersion := "2.12.8",
    mainClass in Compile := Some("im.ghasedak.server.Main"),
    scalaSource in ProtocPlugin.ProtobufConfig := sourceManaged.value,
    scalariformPreferences := scalariformPreferences.value
      .setPreference(RewriteArrowSymbols, true)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(SpacesAroundMultiImports, true),
    parallelExecution in Test := false
  )

  lazy val packagingSettings = Seq(
    bashScriptExtraDefines += """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml""""
  )

}
