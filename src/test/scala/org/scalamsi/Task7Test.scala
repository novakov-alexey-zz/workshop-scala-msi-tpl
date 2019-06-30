package org.scalamsi

import cats.effect.{ContextShift, IO}
import eu.timepit.refined.auto._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import com.softwaremill.macwire._
import org.http4s.HttpRoutes
import org.scalamsi.data.Repository
import org.scalamsi.http.{CommandRoutes, HttpErrorHandler, QueryRoutes}

import scala.concurrent.ExecutionContext.Implicits.global

class Task7Test extends FlatSpec with Matchers with BeforeAndAfterAll {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  val jdbc = JdbcConfig("jdbc:postgresql://localhost:5432/trips", "org.postgresql.Driver", "trips", "trips")
  val mod = wire[Module[IO]]

  it should "lookup all wired dependencies" in {
    val wired = wiredInModule(mod)
    wired.lookup(classOf[Repository[IO]]) shouldNot be(Nil)
    wired.lookup(classOf[TripServiceAlg[IO]]) shouldNot be(Nil)
    wired.lookup(classOf[HttpErrorHandler[IO, UserError]]) shouldNot be(Nil)
    wired.lookup(classOf[QueryRoutes[IO]]) shouldNot be(Nil)
    wired.lookup(classOf[CommandRoutes[IO]]) shouldNot be(Nil)
    wired.lookup(classOf[HttpRoutes[IO]]) shouldNot be(Nil)
  }
}