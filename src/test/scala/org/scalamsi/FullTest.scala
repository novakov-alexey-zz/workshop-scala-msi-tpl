package org.scalamsi

import java.io.File
import java.time.LocalDate

import cats.effect._
import cats.implicits._
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions}
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.jsonEncoderOf
import org.http4s.implicits._
import org.scalamsi.TestData._
import org.scalamsi.json.CirceJsonCodecs
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

class FullTest
    extends WordSpec
    with Matchers
    with BeforeAndAfter
    with ForAllTestContainer
    with CirceJsonCodecs
    with ResponseCheck {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val jsonIoEncoder: EntityEncoder[IO, Json] = jsonEncoderOf[IO, Json]

  override val container = PostgreSQLContainer("postgres:10.4")

  def containerCfg: Config = ConfigFactory.load(
    ConfigFactory
      .parseMap(
        Map(
          "url" -> container.jdbcUrl,
          "user" -> container.username,
          "password" -> container.password,
          "driver" -> container.driverClassName
        ).asJava
      )
      .atKey("storage")
  )

  private def loadJdbcCfg = {
    val cfgPath = "src/test/resources/testcontainers-application.conf"
    val parseOptions = ConfigParseOptions.defaults().setAllowMissing(false)
    val baseCfg = ConfigFactory.parseFile(new File(cfgPath), parseOptions).resolve()
    val cfg = baseCfg.withFallback(containerCfg.resolve())
    AppConfig
      .load(cfg)
      .fold(e => sys.error(s"Failed to load configuration:\n${e.toList.mkString("\n")}"), identity)
      ._2
  }

  lazy val mod: Module[IO] = {
    val jdbc = loadJdbcCfg
    new Module[IO](jdbc)
  }

  before {
    mod.repo.dropSchema().flatMap(_ => mod.createDatabase()).unsafeRunSync()
  }

  "Trips service" should {
    "insert new trip" in {
      insertData()
    }

    "select trips sorted by any field" in {
      insertData()

      checkSorting("id", _.id)
      checkSorting("city", _.city)
      checkSorting("vehicle", _.vehicle.toString)
      checkSorting("price", _.price)
      checkSorting("completed", _.completed)

      // None/Null is the highest order in Postgres, so default is MaxValue then
      implicit val distanceOrdering: Ordering[Option[Int]] = Ordering.by(_.getOrElse(Int.MaxValue))
      checkSorting("distance", _.distance)

      // None/Null is the highest order in Postgres, so default is Max LocalDate then
      implicit val endDateOrdering: Ordering[Option[LocalDate]] = Ordering.by(_.getOrElse(LocalDate.MAX).toEpochDay)
      checkSorting("end_date", _.endDate)
    }

    "select trip by id" in {
      insertData()
      (1 to TestData.mockData.length).foreach(selectAndCheck)
    }

    "update trip by id" in {
      insertData()
      mockData.indices.foreach(i => updateAndCheck(i + 1))
    }

    "delete trip by id" in {
      insertData()
      deleteData()
    }
  }

  private def deleteData(): Unit =
    (1 to TestData.mockData.length).foreach(deleteAndCheck)

  private def insertData(): Unit =
    mockData.foreach { t =>
      val req = Request(method = Method.POST, uri = serviceUri()).withEntity(t.asJson)
      val res = mod.routes.orNotFound.run(req)
      check[Json](res, Status.Ok, Some(CommandResult(1).asJson))
    }

  private def deleteAndCheck(id: Int): Unit = {
    val delete: Request[IO] = Request(method = Method.DELETE, uri = serviceUri(Some(s"/$id")))
    val deleted = mod.routes.orNotFound.run(delete)
    check[Json](deleted, Status.Ok, Some(CommandResult(1).asJson))

    val select: Request[IO] = Request(method = Method.GET, uri = serviceUri(Some(s"/$id")))
    val selected = mod.routes.orNotFound.run(select)
    check[Json](selected, Status.NotFound, None)
  }

  private def updateAndCheck(id: Int): Unit = {
    val prefix = "updated"
    val trip = mockData(id - 1)
    val updatedTrip = trip.copy(id, prefix + trip.city)

    val update = Request(method = Method.PUT, uri = serviceUri(Some(s"/$id"))).withEntity(updatedTrip.asJson)
    val updated = mod.routes.orNotFound.run(update)
    check[Json](updated, Status.Ok, Some(CommandResult(1).asJson))

    val select = Request[IO](method = Method.GET, uri = serviceUri(Some(s"/$id")))
    val selected = mod.routes.orNotFound.run(select)
    check[Json](selected, Status.Ok, Some(updatedTrip.asJson))
  }

  private def selectAndCheck(id: Int): Unit = {
    val select = Request[IO](method = Method.GET, uri = serviceUri(Some(s"/$id")))
    val selected = mod.routes.orNotFound.run(select)
    check[Json](selected, Status.Ok, Some(mockData(id - 1).copy(id).asJson))
  }

  private def checkSorting[T](field: String, sort: Trip => T)(implicit ev: Ordering[T]): Unit = {
    val sorted = mockData.sortBy(sort)
    val isSortedByField = (seq: Seq[Trip]) => seq.map(sort) === sorted.map(sort)
    val selectAll: Request[IO] = Request(method = Method.GET, uri = serviceUri(Some(s"?sort=$field")))
    selectAndCheck(selectAll, sorted, isSortedByField, field)
  }

  private def selectAndCheck(
    select: Request[IO],
    expected: Seq[Trip],
    verify: Seq[Trip] => Boolean,
    field: String
  ): Unit = {
    val selected = mod.routes.orNotFound.run(select)
    val trips = selected.unsafeRunSync().as[Trips].unsafeRunSync().trips

    trips.length should ===(expected.length)
    trips.map(_.city).toSet should ===(expected.map(_.city).toSet)

    val res = verify(trips)
    if (!res) println(s"verify sorted sequence by $field: $trips")
    res should ===(true)
  }
}
