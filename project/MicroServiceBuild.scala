import sbt._

object MicroServiceBuild extends Build with MicroService {

  val appName = "time-to-pay-taxpayer"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val microserviceBootstrapVersion = "10.0.0"
  private val playUrlBindersVersion = "2.1.0"
  private val domainVersion = "5.2.0"
  private val scalaTestVersion = "3.0.7"
  private val pegdownVersion = "1.6.0"
  private val wiremockVersion = "1.58"
  private val catsVersion = "0.8.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "0.40.0",
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "org.typelevel" %% "cats" % catsVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test: Seq[ModuleID] = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.github.tomakehurst" % "wiremock" % wiremockVersion % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply(): Seq[ModuleID] = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test: Seq[ModuleID] = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
