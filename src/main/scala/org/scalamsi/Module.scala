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

  val repo: Repository[F] = wire[TripRepository[F]]

  val service: TripServiceAlg[F] = wire[TripService[F]]

  implicit val errorHandler: HttpErrorHandler[F, UserError] = wire[UserHttpErrorHandler[F]]

  val qr = wire[QueryRoutes[F]]
  val cr = wire[CommandRoutes[F]]
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
