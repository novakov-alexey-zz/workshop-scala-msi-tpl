package org.scalamsi.http

import cats.{ApplicativeError, MonadError}
import cats.data.{Kleisli, OptionT}
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import org.scalamsi.{InvalidTrip, UnknownSortField, UserError}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request, Response}

object ErrorHandler {

  def apply[F[_], E <: Throwable](
    routes: HttpRoutes[F]
  )(handler: E => F[Response[F]])(implicit ev: ApplicativeError[F, E]): HttpRoutes[F] =
    Kleisli { req: Request[F] =>
      OptionT {
        routes.run(req).value.handleErrorWith { e =>
          handler(e).map(Option(_))
        }
      }
    }
}

trait HttpErrorHandler[F[_], E <: Throwable] {
  def handle(routes: HttpRoutes[F]): HttpRoutes[F]
}

class UserHttpErrorHandler[F[_]](implicit M: MonadError[F, Throwable])
    extends HttpErrorHandler[F, UserError]
    with Http4sDsl[F] with StrictLogging {

  private val handler: Throwable => F[Response[F]] = {
    case InvalidTrip(t, m) => BadRequest(s"Invalid trip: $t. Cause: $m")
    case UnknownSortField(f) => BadRequest(s"Unknown sorting field: $f")
    case t =>
      logger.error("Unexpected error", t)
      InternalServerError(s"Something bad happened: $t")
  }

  override def handle(routes: HttpRoutes[F]): HttpRoutes[F] =
    ErrorHandler(routes)(handler)
}
