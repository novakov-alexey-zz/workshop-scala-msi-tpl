package org.scalamsi

import cats.implicits._
import cats.{Applicative, Id, MonadError}
import cats.effect.{ContextShift, IO, Timer}
import org.scalamsi.TestData.{lisbon, mockData}
import org.scalamsi.data.Repository
import org.scalamsi.json.CirceJsonCodecs
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.collection.mutable
import scala.language.higherKinds
import scala.concurrent.ExecutionContext.Implicits.global

class Task3Test extends FlatSpec with Matchers with CirceJsonCodecs with ResponseCheck with BeforeAndAfterAll {

  implicit val timer: Timer[IO] = IO.timer(global)
  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  implicit val idMonadError = new MonadError[Id, Throwable]{
    override def raiseError[A](e: Throwable): Id[A] = throw e

    override def handleErrorWith[A](fa: Id[A])(f: Throwable => Id[A]): Id[A] = fa

    override def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B] = f(fa)

    override def tailRecM[A, B](a: A)(f: A => Id[Either[A, B]]): Id[B] =
      f(a) match {
        case Left(a1) => tailRecM(a1)(f)
        case Right(b) => b
      }

    override def pure[A](x: A): Id[A] = x
  }

  val service = new TripService[Id](stubRepo)

  override protected def beforeAll(): Unit = {
    mockData.foreach(t => service.insert(t))
  }

  it should "select a trip by id" in {
    val trip = service.select(lisbon.id)
    trip should ===(Some(lisbon))

    val noTrip = service.select(-1)
    noTrip should ===(None)
  }

  it should "select all trips" in {
    val trips = service.selectAll(Some(0), Some(10), Some("city"))
    trips should ===(Trips(mockData.sortBy(_.city)))

    val trips2 = service.selectAll(Some(0), Some(10), Some("id"))
    trips2 should ===(Trips(mockData.sortBy(_.id)))
  }

  it should "add new trip" in {
    val cr = service.insert(mockData.head.copy(city = "Madrid", id = mockData.length))
    cr should ===(1)
  }

  it should "update a trip" in {
    val t = mockData.head
    val updated = t.copy(price = 0, distance = Some(24))
    val cr = service.update(t.id, updated)
    cr should ===(1)

    val t2 = service.select(t.id)
    t2 should ===(Some(updated))
  }

  it should "delete a trip" in {
    val id = mockData.head.id
    val cr = service.delete(id)
    cr should ===(1)

    val trip = service.select(id)
    trip should ===(None)
  }

  private def stubRepo[F[_]: Applicative] = new Repository[F] {
    private val data = mutable.Map[Int, Trip]()

    override def delete(id: Int): F[Int] =
      data.get(id).fold(0.pure[F]) { t =>
        data -= id
        1.pure[F]
      }

    override def update(id: Int, trip: Trip): F[Int] =
      data.get(id).fold(0.pure[F]) { t =>
        data += id -> trip
        1.pure[F]
      }

    override def createSchema(): F[Unit] = ???

    override def dropSchema(): F[Unit] = ???

    override def schemaExists(): F[Unit] = ???

    override def insert(trip: Trip): F[Int] = {
      data += trip.id -> trip
      1.pure[F]
    }

    override def selectAll(page: Int, pageSize: Int, sort: String): F[Seq[Trip]] =
      (sort match {
        case "city" => mockData.sortBy(_.city)
        case "id" => mockData.sortBy(_.id)
      }).toSeq.pure[F]

    override def select(id: Int): F[Option[Trip]] = data.get(id).pure[F]
  }
}
