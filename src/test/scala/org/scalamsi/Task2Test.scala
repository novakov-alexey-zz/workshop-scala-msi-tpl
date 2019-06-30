package org.scalamsi

import cats.Applicative
import cats.effect.{ContextShift, IO, Sync, Timer}
import cats.implicits._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Method, Request, Status, Uri}
import org.scalamsi.TestData._
import org.scalamsi.http.{CommandRoutes, HttpErrorHandler, QueryRoutes, UserHttpErrorHandler}
import org.scalamsi.json.CirceJsonCodecs
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

class Task2Test extends FlatSpec with Matchers with ResponseCheck with CirceJsonCodecs {

  implicit val timer: Timer[IO] = IO.timer(global)
  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  private val routes = createRoutes[IO].orNotFound

  def createRoutes[F[_]: Sync]: HttpRoutes[F] = {
    implicit val errorHandler: HttpErrorHandler[F, UserError] = new UserHttpErrorHandler[F]()
    val service: TripServiceAlg[F] = stubService[F]
    val qr = new QueryRoutes[F](service)
    val cr = new CommandRoutes[F](service)
    Router(Module.apiPrefix -> (qr.routes <+> cr.routes))
  }

  it should "select a trip by id" in {
    insertCheck(lisbon)

    val req = Request[IO](method = Method.GET, uri = getUri(s"/${lisbon.id}"))
    val res = routes.run(req)

    check(res, Status.Ok, Some(lisbon))
  }

  it should "select all trips" in {
    val req = Request[IO](method = Method.GET, uri = getUri(s"/?sort=id&page=1&pageSize=5"))
    val res = routes.run(req)

    val expected = Some(Trips(List(berlin, frankfurt, lisbon)))
    check(res, Status.Ok, expected)
  }

  it should "add new trip" in {
    insertCheck(lisbon)
  }

  private def insertCheck(trip: Trip): Unit = {
    val req = Request[IO](method = Method.POST, uri = getUri("")).withEntity(trip)
    val res = routes.run(req)

    check(res, Status.Ok, Some(CommandResult(1)))
  }

  it should "update a trip" in {
    val uri = getUri(s"/${lisbon.id}")

    val select = Request[IO](method = Method.GET, uri = uri)
    val selected = routes.run(select)
    check(selected, Status.Ok, Some(lisbon.copy(completed = false)))

    val update = Request[IO](method = Method.PUT, uri = uri).withEntity(lisbon.copy(completed = true))
    val updated = routes.run(update)
    check(updated, Status.Ok, Some(CommandResult(1)))

    val select2 = Request[IO](method = Method.GET, uri = uri)
    val selected2 = routes.run(select2)
    check(selected2, Status.Ok, Some(lisbon.copy(completed = true)))
  }

  it should "delete a trip" in {
    insertCheck(frankfurt)
    val uri = getUri(s"/${frankfurt.id}")

    val delete = Request[IO](method = Method.DELETE, uri = uri)
    val deleted = routes.run(delete)
    check(deleted, Status.Ok, Some(CommandResult(1)))

    val select = Request[IO](method = Method.GET, uri = uri)
    val selected = routes.run(select)
    check[Trip](selected, Status.NotFound, None)
  }

  def getUri(suffix: String): Uri =
    Uri.fromString(Module.apiPrefix + suffix).toOption.getOrElse(sys.error("Wrong Uri"))

  private def stubService[F[_]: Applicative] = new TripServiceAlg[F] {
    val data = mutable.Map[Int, Trip]()

    override def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): F[Trips] =
      Trips(mockData.sortBy(_.id)).pure[F]

    override def select(id: Int): F[Option[Trip]] = data.get(id).pure[F]

    override def insert(trip: Trip): F[Int] = {
      data += trip.id -> trip
      1.pure[F]
    }

    override def update(id: Int, trip: Trip): F[Int] =
      data.get(id).fold(0.pure[F]) { t =>
        data += id -> trip
        1.pure[F]
      }

    override def delete(id: Int): F[Int] =
      data.get(id).fold(0.pure[F]) { t =>
        data -= id
        1.pure[F]
      }
  }
}
