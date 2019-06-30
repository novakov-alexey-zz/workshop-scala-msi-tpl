package org.scalamsi

import java.time.LocalDate

object TestData {
  val tripId = 1

  val berlin =
    Trip(tripId, "berlin", Vehicle.Car, 20000, completed = false, Some(20000), Some(LocalDate.of(2010, 4, 22)))
  val frankfurt =
    Trip(2, "frankfurt", Vehicle.Taxi, 2000, completed = false, Some(20000), Some(LocalDate.of(2000, 4, 22)))
  val lisbon = Trip(3, "lisbon", Vehicle.Bike, 2000, completed = false, None, None)

  val mockData: IndexedSeq[Trip] =
    IndexedSeq(berlin, frankfurt, lisbon)
}
