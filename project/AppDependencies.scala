
import sbt.*
import play.sbt.PlayImport.ws
import sbt.librarymanagement.Configurations.Provided

object AppDependencies {

  val bootstrapVersion = "9.0.0"
  val cryptoVersion = "7.6.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapVersion,
    "org.scalatest"          %% "scalatest"               % "3.2.18",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "7.0.1",
    "org.wiremock"            % "wiremock-standalone"     % "3.6.0",
    "org.mockito"             % "mockito-core"            % "5.12.0"
  ).map(_ % Test)

  val corDependencies = List(
    // format: OFF
    "org.playframework" %% "play" % play.core.PlayVersion.current % Provided,
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % AppDependencies.bootstrapVersion % Provided
    // format: ON
  )
}
