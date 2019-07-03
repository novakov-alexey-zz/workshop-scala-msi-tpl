package org.scalamsi

import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicative._
import org.scalamsi.TripService._
import org.scalamsi.data.Repository

import scala.language.higherKinds

class TripService[F[_]](repo: Repository[F])(implicit M: MonadError[F, Throwable]) extends TripServiceAlg[F] {

  override def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): F[Trips] = {
    val sortBy = sort
      .map(
        s =>
          if (Repository.sortingFields.contains(s)) M.pure(s)
          else M.raiseError[String](UnknownSortField(s))
      )
      .getOrElse(DefaultSortField.pure[F])

    for {
      s <- sortBy
      ts <- repo.selectAll(page.getOrElse(0), pageSize.getOrElse(10), s)
    } yield Trips(ts)
  }

  override def select(id: Int): F[Option[Trip]] = repo.select(id)

  override def insert(trip: Trip): F[Int] =
    validateTrip(trip).flatMap(_ => repo.insert(trip))

  override def update(id: Int, trip: Trip): F[Int] =
    validateTrip(trip).flatMap(_ => repo.update(id, trip))

  private val validateTrip: Trip => F[Unit] = {
    case t @ Trip(_, _, _, _, true, None, _) =>
      M.raiseError(InvalidTrip(t, "completed trip must have non-empty 'distance'"))
    case t @ Trip(_, _, _, _, true, _, None) =>
      M.raiseError(InvalidTrip(t, "completed trip must have non-empty 'end_date'"))
    case t @ Trip(_, _, _, _, false, None, Some(_)) =>
      M.raiseError(InvalidTrip(t, "non-completed trip must have empty 'end_date'"))
    case _ => M.pure(())
  }

  override def delete(id: Int): F[Int] = repo.delete(id)
}

object TripService {
  val DefaultPage = 0
  val DefaultPageSize = 10
  val DefaultSortField: String = "id"
}
