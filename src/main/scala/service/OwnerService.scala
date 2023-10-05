package service

import repository._

object OwnerService {
  def getReservations() = ReservationRepository.getReservations()

  def closeReservation(reservationId: String) = for {
    reservations <- getReservations()
    changedList = reservations.map { reservation =>
      if (reservationId == reservation.id) {
        reservation.close()
      } else {
        reservation
      }
    }
    _ <- ReservationRepository.saveReservations(changedList)
    changedReservation = changedList.filter(r => r.id == reservationId)
  } yield changedReservation.head

  def getReviews() = ReviewRepository.getReviews()
}
