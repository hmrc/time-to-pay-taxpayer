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
  .dependsOn(cor)
  .aggregate(cor)


lazy val cor = Project(appName + "-cor", file("cor"))
  .enablePlugins(
    SbtAutoBuildPlugin
  )
  .settings(commonSettings *)
  .settings(
    libraryDependencies ++= List(
      "com.typesafe.play" %% "play" % play.core.PlayVersion.current % Provided,
      "uk.gov.hmrc" %% "bootstrap-backend-play-28" % AppDependencies.bootstrapVersion % Provided
    )
  )
