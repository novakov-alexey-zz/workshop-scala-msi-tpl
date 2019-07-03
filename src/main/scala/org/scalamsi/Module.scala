package org.scalamsi

import cats.effect.{Async, ContextShift}
import cats.implicits._
import com.softwaremill.macwire.wire
import doobie.Transactor
import org.scalamsi.data.{TripRepository, Repository}
import org.scalamsi.http.{CommandRoutes, HttpErrorHandler, QueryRoutes, UserHttpErrorHandler}
import org.http4s.HttpRoutes
import org.http4s.server.Router
import Module._

class Module[F[_]: Async: ContextShift](cfg: JdbcConfig) {
  val xa = Transactor.fromDriverManager[F](cfg.driver.value, cfg.url.value, cfg.user.value, cfg.password.value)

  var repo: Repository[F] = new TripRepository[F](xa)

  val service: TripServiceAlg[F] = new TripService[F](repo)

  implicit val errorHandler: HttpErrorHandler[F, UserError] = new UserHttpErrorHandler[F]()

  val qr = new QueryRoutes[F](service)
  val cr = new CommandRoutes[F](service)
  val routes: HttpRoutes[F] = Router(apiPrefix -> (qr.routes <+> cr.routes))

  def resetDatabase(): F[Unit] =
    for {
      _ <- repo.dropSchema()
      _ <- createDatabase()
    } yield ()


  def createDatabase(): F[Unit] =
    repo
      .schemaExists()
      .handleErrorWith(_ => repo.createSchema())
      .adaptError { case e => new RuntimeException("Failed to initialize Trips module", e) }      
}

object Module {
  val apiPrefix = "/api/v1/trips"
}
