package org.scalamsi.http

import cats.implicits._
import cats.effect.Sync
import io.circe.{Decoder, ObjectEncoder}
import org.scalamsi.json.CirceJsonCodecs
import org.scalamsi.{CommandResult, Trip, TripServiceAlg, UserError}
import org.http4s.{EntityDecoder, HttpRoutes}
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl

class CommandRoutes[F[_]: Sync](service: TripServiceAlg[F])(implicit H: HttpErrorHandler[F, UserError])
    extends Http4sDsl[F] {

  //TODO: Task1 - remove below implicits. This is only to make it compile
  implicit val tripDecoder: Decoder[Trip] = ???
  implicit def circeEntityDecoder[A: Decoder]: EntityDecoder[F, Trip] = jsonOf[F, Trip]

  val routes: HttpRoutes[F] = H.handle(HttpRoutes.of[F] {

    // Add: POST /api/v1/trips, body = JSON -> CommandResult(n)
    case req @ POST -> Root =>
      for {
        trip <- req.as[Trip]

      } yield ???


    // Update: PUT /api/v1/trips/<id>, body = JSON -> CommandResult(n)
    case req @ PUT -> Root / IntVar(id) =>
      ???

    // Delete: DELETE /api/v1/trips/<id> -> CommandResult(n)
    case DELETE -> Root / IntVar(id) =>
      ???
  })
}
