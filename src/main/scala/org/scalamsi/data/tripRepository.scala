package org.scalamsi.data

import cats.effect.Sync
import doobie._
import doobie.implicits._
import org.scalamsi.{Trip, Vehicle}
import org.scalamsi.Vehicle.Vehicle
import org.scalamsi.data.TripRepositoryQueries._

import scala.collection.mutable

class TripRepository[F[_]: Sync](xa: Transactor[F]) extends Repository[F] {

  override def delete(id: Int): F[Int] =
    sql"DELETE FROM trips WHERE id = $id".update.run.transact(xa)

  //TODO: implement update
  override def update(id: Int, trip: Trip): F[Int] =
    updateQuery(id, trip).run.transact(xa)

  override def createSchema(): F[Unit] = createDdl.update.run.map(_ => ()).transact(xa)

  override def dropSchema(): F[Unit] = dropDdl.update.run.map(_ => ()).transact(xa)

  override def insert(row: Trip): F[Int] = {
    val values =
      fr"VALUES (${row.id}, ${row.city}, ${row.vehicle}, ${row.price}, " ++
        fr"${row.completed}, ${row.distance}, ${row.endDate})"

    (insertFrag ++ values).update.run.transact(xa)
  }

  //TODO: implement selectAll
  override def selectAll(page: Int, pageSize: Int, sort: String): F[Seq[Trip]] =
    selectAllQuery(sort).stream.drop(page * pageSize).take(pageSize).compile.to[Seq].transact(xa)

  override def select(id: Int): F[Option[Trip]] =
    sql"SELECT * FROM trips WHERE id = $id"
      .query[Trip]
      .option
      .transact(xa)

  def schemaExists(): F[Unit] =
    sql"""
        SELECT 1
        FROM   information_schema.tables
        WHERE  table_catalog = 'trips'
        AND    table_name = 'trips';"""
      .query[Unit]
      .unique
      .transact(xa)
}

object TripRepositoryQueries {
  implicit val vehicleMeta: Meta[Vehicle] = Meta[String].timap(Vehicle.withName)(_.toString)

  val (columns, columnsWithComma) = {
    val columns = mutable.LinkedHashSet[String]("id", "city", "vehicle", "price", "completed", "distance", "end_date")
    (columns.toSet, columns.mkString(","))
  }

  val createDdl = sql"""
         CREATE TABLE trips (
            id SERIAL,
            city VARCHAR NOT NULL,
            vehicle VARCHAR NOT NULL,
            price INT NOT NULL,
            completed BOOLEAN NOT NULL,
            distance INT,
            end_date DATE)
       """

  val dropDdl = sql"DROP TABLE IF EXISTS trips"

  val insertFrag: Fragment = fr"INSERT INTO trips (" ++ Fragment.const(columnsWithComma) ++ fr")"
  val updateFrag: Fragment = fr"UPDATE trips SET (" ++ Fragment.const(columnsWithComma) ++ fr") = "

  def selectAllQuery(sortField: String): doobie.Query0[Trip] =
    Fragment
      .const(s"select * from trips order by $sortField")
      .query

  def updateQuery(id: Int, trip: Trip): doobie.Update0 = {
    val valuesFrag =
      fr"(${trip.id}, ${trip.city}, ${trip.vehicle}, ${trip.price}, ${trip.completed}, ${trip.distance}, ${trip.endDate})"

    val predicate = fr" WHERE id = $id"

    (updateFrag ++ valuesFrag ++ predicate).update
  }
}
