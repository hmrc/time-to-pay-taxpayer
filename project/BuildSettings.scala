import sbt.Keys._
import sbt._
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import wartremover.{Wart, wartremoverErrors, wartremoverExcluded}

object BuildSettings {
  val scalaV = "2.12.12"

  val scalaCompilerOptions = Seq(
    "-Xfatal-warnings",
    "-Xlint:-missing-interpolator,_",
    "-Yno-adapted-args",
    "-Ywarn-value-discard",
    "-Ywarn-dead-code",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Ywarn-unused:-imports",
    "-language:implicitConversions",
    "-Ypartial-unification" //required by cats
  )

  lazy val commonSettings =
    uk.gov.hmrc.DefaultBuildSettings.scalaSettings ++
      uk.gov.hmrc.DefaultBuildSettings.defaultSettings() ++ Seq(
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
}