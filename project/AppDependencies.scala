
import sbt.*
import play.sbt.PlayImport.ws
import sbt.librarymanagement.Configurations.Provided

object AppDependencies {

  val bootstrapVersion = "8.0.0"
  val cryptoVersion = "7.6.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapVersion
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapVersion,
    "org.scalatest"          %% "scalatest"               % "3.2.17",
    "com.vladsch.flexmark"    % "flexmark-all"            % "0.64.8",
    "org.pegdown"             % "pegdown"                 % "1.6.0",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0",
    "org.wiremock"            % "wiremock-standalone"     % "3.3.1",
    "org.mockito"             % "mockito-core"            % "5.7.0"
  ).map(_ % Test)

  val corDependencies = List(
    // format: OFF
    "com.typesafe.play" %% "play" % play.core.PlayVersion.current % Provided,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % AppDependencies.bootstrapVersion % Provided
    // format: ON
  )
}
