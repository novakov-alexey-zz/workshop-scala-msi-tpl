package org.scalamsi.http

import cats.implicits._
import cats.effect.Sync
import org.scalamsi.json.CirceJsonCodecs
import org.scalamsi.{CommandResult, Trip, TripServiceAlg, UserError}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class CommandRoutes[F[_]: Sync](service: TripServiceAlg[F])(implicit H: HttpErrorHandler[F, UserError])
    extends Http4sDsl[F] with CirceJsonCodecs {

  val routes: HttpRoutes[F] = H.handle(HttpRoutes.of[F] {

    // Add: POST /api/v1/trips, body = JSON -> CommandResult(n)
    case req @ POST -> Root =>
      for {
        trip <- req.as[Trip]
        i <- service.insert(trip)
        res <- Ok(CommandResult(i))
      } yield res

    // Update: PUT /api/v1/trips/<id>, body = JSON -> CommandResult(n)
    case req @ PUT -> Root / IntVar(id) =>
      for {
        trip <- req.as[Trip]
        i <- service.update(id, trip)
        res <- Ok(CommandResult(i))
      } yield res

    // Delete: DELETE /api/v1/trips/<id> -> CommandResult(n)
    case DELETE -> Root / IntVar(id) =>
      for {
        i <- service.delete(id)
        res <- Ok(CommandResult(i))
      } yield res
  })
}
