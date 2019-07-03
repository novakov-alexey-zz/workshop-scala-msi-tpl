package org.scalamsi

import java.io.File

import shapeless.syntax.std.tuple._
import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions}
import com.typesafe.scalalogging.StrictLogging
import pureconfig.generic.ProductHint
import pureconfig.error.ConfigReaderFailures
import pureconfig.{loadConfig, CamelCase, ConfigFieldMapping}
import pureconfig.generic.auto._
import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.pureconfig._
import org.scalamsi.refined._

object refined {
  type ConnectionTimeout = Int Refined Interval.OpenClosed[W.`0`.T, W.`100000`.T]
  //TODO: implement these types
  type MaxPoolSize = Int Refined Interval.OpenClosed[W.`1`.T, W.`100`.T]
  type JdbcUrl = String Refined MatchesRegex[W.`"""jdbc:\\w+://\\w+:[0-9]{4,5}/\\w+"""`.T]
}

//TODO: implement JdbcConfig
final case class JdbcConfig(
  url: JdbcUrl,
  driver: NonEmptyString,
  user: NonEmptyString,
  password: NonEmptyString,
  connectionTimeout: ConnectionTimeout = 3000,
  maximumPoolSize: MaxPoolSize = 100
)

final case class Server(host: NonEmptyString = "localhost", port: UserPortNumber = 8080)

object AppConfig extends StrictLogging {
  private val parseOptions = ConfigParseOptions.defaults().setAllowMissing(false)

  private val cfgPath: String = sys.env.getOrElse("APP_CONFIG_PATH", "src/main/resources/application.conf")

  implicit def hint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  def load(cfg: Config): Either[ConfigReaderFailures, (Server, JdbcConfig)] = {
    for {
      j <- loadConfig[JdbcConfig](cfg, "storage")
      c <- loadConfig[Server](cfg, "server")
    } yield (c, j)
  }

  def loadJdbc(cfg: Config): Either[ConfigReaderFailures, JdbcConfig] =
    loadConfig[JdbcConfig](cfg, "storage")

  def load(path: String = cfgPath): Either[ConfigReaderFailures, (Server, JdbcConfig, Config)] = {
    logger.info(s"loading config file at $path")
    val config = ConfigFactory.parseFile(new File(path), parseOptions).resolve()
    load(config).map(_ :+ config)
  }
}
