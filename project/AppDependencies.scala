import sbt.*
import play.sbt.PlayImport.ws

object AppDependencies {

  val bootstrapVersion = "7.21.0"
  val jacksonVersion = "2.13.2"
  val jacksonDatabindVersion = "2.13.2.2"

  val jacksonOverrides = Seq(
    // format: OFF
    "com.fasterxml.jackson.core" % "jackson-core",
    "com.fasterxml.jackson.core" % "jackson-annotations",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"
    // format: ON
  ).map(_ % jacksonVersion)

  val jacksonDatabindOverrides = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonDatabindVersion
  )

  val akkaSerializationJacksonOverrides = Seq(
    // format: OFF
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor",
    "com.fasterxml.jackson.module"     % "jackson-module-parameter-names",
    "com.fasterxml.jackson.module"    %% "jackson-module-scala"
    // format: ON
  ).map(_ % jacksonVersion)

  val compile = Seq(
    ws,
    "uk.gov.hmrc"         %% "bootstrap-backend-play-28" % bootstrapVersion,
    "com.vladsch.flexmark" % "flexmark-all"              % "0.64.6"
  ) ++ jacksonDatabindOverrides ++ jacksonOverrides ++ akkaSerializationJacksonOverrides

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapVersion,
    "org.scalatest"          %% "scalatest"               % "3.2.16",
    "org.pegdown"             % "pegdown"                 % "1.6.0",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0",
    "com.github.tomakehurst"  % "wiremock-jre8"           % "2.35.0",
    "org.mockito"             % "mockito-core"            % "5.4.0"
  ).map(_ % Test)
}
