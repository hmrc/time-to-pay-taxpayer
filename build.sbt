import TestPhases.oneForkedJvmPerTest
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import sbt.Def
import sbt.Keys.scalacOptions
import scalariform.formatter.preferences._
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}
import uk.gov.hmrc.SbtArtifactory
import wartremover.{Wart, wartremoverErrors, wartremoverExcluded, wartremoverWarnings}

lazy val appName = "time-to-pay-taxpayer"
//scalaVersion := "2.12.12"

resolvers += Resolver.bintrayRepo("hmrc", "releases")

val scalaCompilerOptions = Seq(
  "-Xfatal-warnings",
  "-Xlint:-missing-interpolator,_",
  "-Yno-adapted-args",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-Ypartial-unification" //required by cats
)

lazy val commonSettings = Seq(
  scalaVersion := "2.12.12",
  majorVersion := 0,
  scalacOptions ++= scalaCompilerOptions,
  resolvers ++= Seq(
    Resolver.jcenterRepo
  ),
  evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
  wartremoverExcluded ++=
    (baseDirectory.value / "it").get ++
      (baseDirectory.value / "test").get ++
      Seq(sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala"),
  ScalariformSettings()
)
  .++(WartRemoverSettings.wartRemoverError)
  .++(WartRemoverSettings.wartRemoverWarning)
  .++(Seq(
    wartremoverErrors in(Test, compile) --= Seq(Wart.Any, Wart.Equals, Wart.Null, Wart.NonUnitStatements, Wart.PublicInference)
  ))
  .++(ScoverageSettings())
  .++(scalaSettings)
  .++(uk.gov.hmrc.DefaultBuildSettings.defaultSettings())


lazy val microservice = Project(appName, file("."))
  .enablePlugins(
    play.sbt.PlayScala,
    SbtAutoBuildPlugin,
    SbtGitVersioning,
    SbtDistributablesPlugin,
    SbtArtifactory
  )
  .settings(commonSettings: _*)
  .settings(SbtDistributablesPlugin.publishingSettings: _*)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    routesGenerator := InjectedRoutesGenerator,
    majorVersion := 0,
    PlayKeys.playDefaultPort := 9857,
    wartremoverExcluded ++= routes.in(Compile).value,
    routesImport ++= Seq(
      "timetopaytaxpayer.cor.model._"
    )
  )
  .dependsOn(cor)
  .aggregate(cor)


lazy val cor = Project(appName + "-cor", file("cor"))
  .enablePlugins(
    SbtAutoBuildPlugin,
    SbtGitVersioning,
    SbtArtifactory
  )
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= List(
      "com.typesafe.play" %% "play" % play.core.PlayVersion.current % Provided,
      "uk.gov.hmrc" %% "bootstrap-backend-play-26" % "3.4.0" % Provided
    )
  )
