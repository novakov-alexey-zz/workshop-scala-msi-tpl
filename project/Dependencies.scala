import sbt._

object Dependencies extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    object DependenciesVersion {
      val catsVersion                      = "1.6.0"
      val circeVersion                     = "0.11.1"
      val logbackClassicVersion            = "1.2.3"
      val pureConfigVersion                = "0.10.1"
      val refinedPureconfigVersion         = "0.9.4"
      val scalaTestVersion                 = "3.0.5"
      val slf4jVersion                     = "1.7.25"
      val typesafeConfigVersion            = "1.3.3"
      val scalacheckVersion                = "1.13.4"
      val argonautVersion                  = "6.2.2"
      val akkaVersion                      = "2.5.19"
      val akkaHttpVersion                  = "10.1.6"
      val akkaHttpUpickleVersion           = "1.23.0"
      val scalaLoggingVersion              = "3.9.0"
      val elasticVersion                   = "6.3.7"
      val log4jVersion                     = "2.11.1"
      val doobieVersion                    = "0.6.0"
      val http4sVersion                    = "0.20.1"
      val slickVersion                     = "3.2.3"
      val macwireVersion                   = "2.3.2"
      val testContainersVersion            = "0.25.0"
      val testContainersPostgresVersion    = "1.11.3"
      val upickleVersion                   = "0.6.7"
      val scalameterVersion                = "0.10.1"
    }

    import DependenciesVersion._

    val cats                     = "org.typelevel"             %%  "cats-core"                 % catsVersion

    val circeCore                = "io.circe"                  %% "circe-core"                 % circeVersion
    val circeGeneric             = "io.circe"                  %% "circe-generic"              % circeVersion
    val circeJava8               = "io.circe"                  %% "circe-java8"                % circeVersion

    val scalameter               = "com.storm-enroute"         %% "scalameter-core"            % scalameterVersion % Test
    val scalaTest                = "org.scalatest"             %%  "scalatest"                 % scalaTestVersion % Test
    val testContainers           = "com.dimafeng"              %% "testcontainers-scala"       % testContainersVersion % Test
    val testContainersPostgres   = "org.testcontainers"        %  "postgresql"                 % testContainersPostgresVersion % Test
    
    val logbackClassic           = "ch.qos.logback"            %   "logback-classic"           % logbackClassicVersion
    val scalaLogging             = "com.typesafe.scala-logging" %% "scala-logging"             % scalaLoggingVersion
    val slf4jApi                 = "org.slf4j"                 %   "slf4j-api"                 % slf4jVersion

    val pureConfig               = "com.github.pureconfig"     %%  "pureconfig"                % pureConfigVersion
    val refinedPureconfig        = "eu.timepit"                %%  "refined-pureconfig"        % refinedPureconfigVersion
    val typesafeConfig           = "com.typesafe"              %   "config"                    % typesafeConfigVersion

    val akka                     = "com.typesafe.akka"         %% "akka-actor"                 % akkaVersion
    val akkaTestKit              = "com.typesafe.akka"         %% "akka-testkit"               % akkaVersion % Test
    val akkaStreams              = "com.typesafe.akka"         %% "akka-stream"                % akkaVersion
    val akkaHttp                 = "com.typesafe.akka"         %% "akka-http"                  % akkaHttpVersion
    val akkaHttpSpray            = "com.typesafe.akka"         %% "akka-http-spray-json"       % akkaHttpVersion
    val akkaHttpTestKit          = "com.typesafe.akka"         %% "akka-http-testkit"          % akkaHttpVersion % Test
    val akkaHttpUpickle          = "de.heikoseeberger"         %% "akka-http-upickle"          % akkaHttpUpickleVersion

    val doobieCore               = "org.tpolecat"              %% "doobie-core"                % doobieVersion
    val doobiePostgres           = "org.tpolecat"              %% "doobie-postgres"            % doobieVersion
    val doobieScalatest          = "org.tpolecat"              %% "doobie-scalatest"           % doobieVersion % Test

    val slick                    = "com.typesafe.slick"         %% "slick"                     % slickVersion
    val slickHikari              = "com.typesafe.slick"         %% "slick-hikaricp"            % slickVersion

    val http4sBlaze              = "org.http4s"                 %% "http4s-blaze-server"       % http4sVersion
    val http4sCirce              = "org.http4s"                 %% "http4s-circe"              % http4sVersion
    val http4sDsl                = "org.http4s"                 %% "http4s-dsl"                % http4sVersion
    val http4sClient             = "org.http4s"                 %% "http4s-blaze-client"       % http4sVersion % Test

    val upickle                  = "com.lihaoyi"                %% "upickle"                   % upickleVersion
    val ujson                    = "com.lihaoyi"                %% "ujson"                     % upickleVersion

    val macwire                  = "com.softwaremill.macwire"   %% "macros"                    % macwireVersion
    val macwireUtil              = "com.softwaremill.macwire"   %% "util"                      % macwireVersion
  }
}
