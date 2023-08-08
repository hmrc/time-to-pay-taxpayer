import sbt.Keys.*
import sbt.*
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import wartremover.Wart
import wartremover.WartRemover.autoImport.{wartremoverErrors, wartremoverExcluded}

object BuildSettings {
  val scalaV = "2.13.8"

  val scalaCompilerOptions = Seq(
    "-Xfatal-warnings",
    "-Xlint:-missing-interpolator,_",
    "-Xlint:-byname-implicit",
    "-Ywarn-value-discard",
    "-Ywarn-dead-code",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Ywarn-unused:-imports",
    "-language:implicitConversions"
  )

  lazy val commonSettings =
    uk.gov.hmrc.DefaultBuildSettings.scalaSettings ++
      uk.gov.hmrc.DefaultBuildSettings.defaultSettings() ++ Seq(
      scalaVersion := "2.13.11",
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