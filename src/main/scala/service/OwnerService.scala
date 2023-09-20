package service

import file.FileManager
import file.FileManager._
import model._

object OwnerService {
  def getReservations() = for {
    reservations <- FileManager.readJson[Reservation](FILE_RESERVATION)
  } yield reservations

  def closeReservation(reservationId: Int) = for {
    reservations <- getReservations()
    changedList = reservations.map { reservation =>
      if (reservationId == reservation.id) {
        reservation.close()
      } else {
        reservation
      }
    }
    _ <- FileManager.writeJson(FILE_RESERVATION, changedList)
    changedReservation = changedList.filter(r => r.id == reservationId)
  } yield changedReservation.head

  def getReviews() = for {
    reviews <- FileManager.readJson[Review](FILE_REVIEW)
  } yield reviews
}
