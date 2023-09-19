package service

import file.FileManager
import model.Reservation

object OwnerService {
  private val FILE_RESERVATION = "reservation.json"

  private def inputMenu() = for {
    _ <- zio.Console.printLine("1. 예약 조회")
    _ <- zio.Console.printLine("2. 예약 종료")
    _ <- zio.Console.printLine("3. 리뷰 조회")
    choice <- zio.Console.readLine("입력: ")
  } yield choice

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
}
