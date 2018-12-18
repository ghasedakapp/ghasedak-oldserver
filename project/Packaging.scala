import com.typesafe.sbt.packager.Keys.bashScriptExtraDefines

object Packaging {

 lazy val packagingSettings = Seq(
   bashScriptExtraDefines += """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml""""
 )

}
