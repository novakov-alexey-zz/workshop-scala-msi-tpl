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
  val xa = Transactor.fromDriverManager[F](cfg.driver, cfg.url, cfg.user, cfg.password)

  var repo: Repository[F] = new TripRepository[F](xa){
    //TODO-Task5: remove overridden method
    override def createSchema(): F[Unit] = ().pure[F]
  }
  var service: TripServiceAlg[F] = _

  implicit val errorHandler: HttpErrorHandler[F, UserError] = new UserHttpErrorHandler[F]()

  val routes: HttpRoutes[F] = ???

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
