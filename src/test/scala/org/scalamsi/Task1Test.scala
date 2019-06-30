package org.scalamsi

import cats.effect._
import cats.implicits._
import org.http4s.Uri
import org.http4s.client.blaze.BlazeClientBuilder
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class Task1Test extends FlatSpec with Matchers with Eventually {
  implicit val timer: Timer[IO] = IO.timer(global)
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  override implicit val patienceConfig =
       PatienceConfig(timeout = scaled(Span(4, Seconds)), interval = scaled(Span(5, Millis)))

  def startServer: CancelToken[IO] = {
    val s = Main.stream[IO].compile.drain.as(ExitCode.Success)
    s.unsafeRunCancelable {
      case Left(e) => fail(e)
      case Right(ec) => println(s"exit code: ${ec.code}")
    }
  }

  def stopServer(token: CancelToken[IO]): Unit =
    token.unsafeRunSync()

  it should "return pong as reply" in {
    val token = startServer

    eventually {
      val reply = BlazeClientBuilder[IO](global).resource.use { client =>
        client.expect[String](getUri("/ping"))
      }.unsafeRunSync()

      reply should ===("pong")
    }

    stopServer(token)
  }

  private def getUri(endpoint: String) =
    Uri.fromString(s"http://localhost:8080${Module.apiPrefix}$endpoint")
      .fold(e => sys.error(s"Wrong Uri: $e"), identity)
}
