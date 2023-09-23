package service

import zio._

import file._
import file.FileManager._

import model._
import repository._

object UserService {
  // ---- V 단순 매핑 코드 V ----

  private def doesUserExists(user: User) = for {
    users <- UserRepository.getUsers()
  } yield users.contains(user)

  private def getClosedReservationsOfUser(user: User) = for {
    reservations <- ReservationRepository.getReservations()

    closedReservationsOfUser = reservations
      .filter(_.user == user)
      .filter(_.isClosed)
  } yield closedReservationsOfUser

  private def getPaidButNotReviewedReservationsOfUser(user: User) = for {
    reservations <- ReservationRepository.getReservations()
    paidReservationsOfUser = reservations
      .filter(_.user == user)
      .filter(_.isPaied)

  } yield paidReservationsOfUser

  private def checkIfReservationExistById(targetId: String) = for {
    reservations <- FileManager.readJson[Reservation](
      FileManager.FILE_RESERVATION
    )

    doesExist = reservations.exists(r => r.id == targetId)

  } yield doesExist

  // ---- V 내부 로직 코드 V ----

  private def addReview(review: Review) = for {
    reviews <- ReviewRepository.getReviews()
    // 이미 reservation id에 대한 리뷰가 존재하면
    // 에러를 던져준다.
    doesExist <- checkIfReservationExistById(review.reservationId)
    nextReviews <- doesExist match {
      case true  => ZIO.fail(s"이미 존재하는 리뷰가 있습니다.")
      case false => ZIO.succeed(reviews.appended(review))
    }
    _ <- ReviewRepository.saveReviews(nextReviews)
  } yield ()

  // ---- V 외부 공개 코드 V ----

  def findReservationsByUser(user: Either[String, User]) = for {
    users <- UserRepository.getUsers()
    reservations <- ReservationRepository.getReservations()

    result <- user match {
      case Left(error) => ZIO.left(error)
      case Right(data) =>
        ZIO.right(reservations.filter(reservation => reservation.user == data))
    }
  } yield result

  def makeReservation(
      user: User,
      date: String,
      time: String,
      guestCount: Int
  ) = for {
    _ <- ZIO.unit

    data = ReservationData(user, date, time, guestCount)

    result <- ReservationRepository.addReservation(data)

    _ <- UserRepository.addUser(user)

  } yield result

  def pay(reservationId: String) = for {
    reservations <- ReservationRepository.getReservations()

    changed = reservations.map { reservation =>
      if (reservation.id == reservationId) reservation.copy(isPaied = true)
      else reservation
    }

    _ <- ReservationRepository.saveReservations(changed)

    result <- ZIO.attempt(changed.head)

  } yield result

  def writeReview(reservationId: String, point: Int, content: String) =
    for {
      _ <- ZIO.unit
      review = Review(reservationId, point, content)
      _ <- addReview(review)
    } yield review
}
