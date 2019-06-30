package org.scalamsi.data

import org.scalamsi.Trip

import scala.collection.mutable

trait Repository[F[_]] {
  def delete(id: Int): F[Int]

  def update(id: Int, row: Trip): F[Int]

  def createSchema(): F[Unit]

  def dropSchema(): F[Unit]

  def schemaExists(): F[Unit]

  def insert(row: Trip): F[Int]

  def selectAll(page: Int, pageSize: Int, sort: String): F[Seq[Trip]]

  def select(id: Int): F[Option[Trip]]
}

object Repository {
  val sortingFields: mutable.LinkedHashSet[String] =
    mutable.LinkedHashSet[String]("id", "city", "vehicle", "price", "completed", "distance", "end_date")
}
