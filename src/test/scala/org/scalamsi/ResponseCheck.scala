package org.scalamsi

import cats.effect.IO
import org.http4s.{EntityDecoder, Response, Status}
import org.scalatest.Matchers

trait ResponseCheck extends Matchers {

  def check[A](actual:        IO[Response[IO]],
               expectedStatus: Status,
               expectedBody:   Option[A])(
                implicit ev: EntityDecoder[IO, A]
              ): Unit =  {
    val actualResp = actual.unsafeRunSync
    actualResp.status should ===(expectedStatus)

    expectedBody.fold[Unit](
      actualResp.body.compile.toVector.unsafeRunSync.isEmpty should ===(true))(
      expected => actualResp.as[A].unsafeRunSync should ===(expected)
    )
  }
}
