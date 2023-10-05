package service

import file.FileManager
import file.FileManager._
import model._

object OwnerService {
  def getReservations() = FileManager.readJson[Reservation](FILE_RESERVATION)

  def closeReservation(reservationId: String) = for {
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

  def getReviews() = FileManager.readJson[Review](FILE_REVIEW)
}
