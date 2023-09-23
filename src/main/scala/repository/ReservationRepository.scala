package repository

import zio._
import model._
import file.FileManager._

import java.util.UUID

object ReservationRepository {
  def getReservations() = for {
    reservations <- readJson[Reservation](FILE_RESERVATION)
  } yield reservations

  def saveReservations(reservations: List[Reservation]) = for {
    _ <- writeJson(FILE_RESERVATION, reservations)
  } yield ()

  def addReservation(data: ReservationData) = for {
    reservations <- getReservations()

    uuid = UUID.randomUUID().toString()

    (user, date, time, guests) = data match {
      case ReservationData(user, date, time, guests) =>
        (user, date, time, guests)
    }

    newReservation = Reservation(uuid, user, date, time, guests, false, false)

    nextReservations = reservations.appended(newReservation)

    _ <- saveReservations(nextReservations)

  } yield newReservation
}
