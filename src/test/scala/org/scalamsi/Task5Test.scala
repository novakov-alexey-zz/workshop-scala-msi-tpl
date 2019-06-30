package org.scalamsi

import cats.effect.{ContextShift, IO}
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import eu.timepit.refined.auto._
import org.scalamsi.data.{TripRepository, TripRepositoryQueries}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class Task5Test extends FlatSpec with Matchers with IOChecker with BeforeAndAfterAll {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  val jdbc = JdbcConfig("jdbc:postgresql://localhost:5432/trips", "org.postgresql.Driver", "trips", "trips", 3000, 100)

  override protected def beforeAll(): Unit =
    repo.dropSchema().flatMap(_ => repo.createSchema()).unsafeRunSync()

  override def transactor: doobie.Transactor[IO] =
    Transactor.fromDriverManager[IO](jdbc.driver, jdbc.url, jdbc.user, jdbc.password)

  val repo = new TripRepository(transactor)

  it should "compile select all query" in {
    check(TripRepositoryQueries.selectAllQuery("city"))
  }

  it should "compile update by id query" in {
    check(TripRepositoryQueries.updateQuery(1, TestData.mockData.head))
  }
}
