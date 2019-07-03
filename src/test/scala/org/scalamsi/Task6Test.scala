package org.scalamsi

import cats.effect._
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.syntax._
import org.http4s._
import org.http4s.implicits._
import org.scalamsi.TestData._
import org.scalamsi.json.CirceJsonCodecs
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

class Task6Test
    extends WordSpec
    with Matchers
    with BeforeAndAfter
    with ForAllTestContainer
    with ResponseCheck
    with CirceJsonCodecs {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  override val container = PostgreSQLContainer("postgres:10.4")

  def containerCfg(properties: Map[String, String]): Config =
    ConfigFactory.load(
      ConfigFactory
        .parseMap(properties.asJava)
        .atKey("storage")
    )

  lazy val mod: Module[IO] = {
    val dbProps = Map(
      "url" -> container.jdbcUrl,
      "user" -> container.username,
      "password" -> container.password,
      "driver" -> container.driverClassName
    )
    val cfg = containerCfg(dbProps).resolve()
    val jdbc = AppConfig
      .loadJdbc(cfg)
      .fold(e => sys.error(s"Failed to load configuration:\n${e.toList.mkString("\n")}"), identity)
    new Module[IO](jdbc)
  }

  before {
    mod.resetDatabase().unsafeRunSync()
  }

  "Trips service" should {
    "insert new trips and read them from database" in {
      val expResult = Some(CommandResult(1).asJson)

      mockData.foreach { t =>
        val req = Request[IO](method = Method.POST, uri = serviceUri()).withEntity(t.asJson)
        val res = mod.routes.orNotFound.run(req)
        check(res, Status.Ok, expResult)
      }

      (1 to TestData.mockData.length).foreach { id =>
        val select = Request[IO](method = Method.GET, uri = serviceUri(Some(s"/$id")))
        val selected = mod.routes.orNotFound.run(select)
        check(selected, Status.Ok, Some(mockData(id - 1).copy(id).asJson))
      }
    }
  }
}
