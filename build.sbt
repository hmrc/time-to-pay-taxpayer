import BuildSettings.{commonSettings, scalaV}
import wartremover.WartRemover.autoImport.wartremoverExcluded

lazy val appName = "time-to-pay-taxpayer"
scalaVersion := scalaV

lazy val microservice = Project(appName, file("."))
  .enablePlugins(
    play.sbt.PlayScala,
    SbtAutoBuildPlugin,
    SbtDistributablesPlugin
  )
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .settings(SbtUpdatesSettings.sbtUpdatesSettings)
  .settings(commonSettings *)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    routesGenerator := InjectedRoutesGenerator,
    majorVersion := 0,
    PlayKeys.playDefaultPort := 9857,
    wartremoverExcluded ++= (Compile / routes).value,
    routesImport ++= Seq(
      "timetopaytaxpayer.cor.model.SaUtr"
    )
  )
  .settings(Compile / doc / scalacOptions := Seq())
  .dependsOn(cor)
  .aggregate(cor)


lazy val cor = Project(appName + "-cor", file("cor"))
  .enablePlugins(
    SbtAutoBuildPlugin
  )
  .settings(commonSettings *)
  .settings(libraryDependencies ++= AppDependencies.corDependencies)

