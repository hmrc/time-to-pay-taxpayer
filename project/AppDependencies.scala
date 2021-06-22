import sbt._
import play.sbt.PlayImport.ws

object AppDependencies {
  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-26" % "3.4.0"
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.0.4" % Test,
    "org.pegdown" % "pegdown" % "1.6.0" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
    "com.github.tomakehurst" % "wiremock-jre8" % "2.21.0" % Test,
    "org.mockito" % "mockito-core" % "2.23.0" % Test
  )
}
