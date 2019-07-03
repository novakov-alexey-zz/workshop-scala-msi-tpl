package org.scalamsi.http

import cats.effect.Sync
import cats.implicits.toFlatMapOps
import org.scalamsi.{TripServiceAlg, UserError}
import org.scalamsi.json.CirceJsonCodecs
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

object OptSort extends OptionalQueryParamDecoderMatcher[String]("sort")
object OptPage extends OptionalQueryParamDecoderMatcher[Int]("page")
object OptPageSize extends OptionalQueryParamDecoderMatcher[Int]("pageSize")

class QueryRoutes[F[_]: Sync](service: TripServiceAlg[F])(implicit H: HttpErrorHandler[F, UserError])
    extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = H.handle(HttpRoutes.of[F] {
    // Test: GET /api/v1/trips/ping
    case GET -> Root / "ping" =>
      Ok("pong")

    // Select one: GET /api/v1/trips/<id>
    case GET -> Root / IntVar(id) =>
      ???

    // Select all: GET /api/v1/trips?sort=id&page=1&pageSize=100
    case GET -> Root :? OptSort(sort) +& OptPage(page) +& OptPageSize(pageSize) =>
      ???
  })
}
