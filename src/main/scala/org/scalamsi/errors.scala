package org.scalamsi

sealed trait UserError extends Exception
case class InvalidTrip(trip: Trip, msg: String) extends UserError
case class UnknownSortField(field: String) extends UserError
