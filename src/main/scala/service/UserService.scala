package service

import zio._

import file._
import file.FileManager._

import model._
import repository._

object UserService {

  // Conflict나서 주석처리함

  private def checkIfReviewHasCreatedById(id: String) = for {
    reviews <- ReviewRepository.getReviews()
    isExistReview = reviews.exists(r => r.reservationId == id)
  } yield isExistReview

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

  // 어디서 씀?
  private def getPaidButNotReviewedReservationById(id: String) = for {
    reservations <- ReservationRepository.getReservations().map(r => r.filter(item => item.id == id))

    paidReservation <- reservations match {
      case Nil => ZIO.fail("ID에 해당하는 예약을 찾을 수 없습니다.")
      case reservation :: Nil if reservation.isPaid == true =>
        ZIO.succeed(reservation)
      case reservation :: Nil if reservation.isPaid == false =>
        ZIO.fail("결제가 완료되지 않은 예약입니다.")
      case _ :: _ => ZIO.fail("동일한 ID로 여러 개의 예약이 조회됩니다.")
    }
    isExists <- checkIfReviewHasCreatedById(paidReservation.id)
    pendingReservation <- isExists match {
      case  false => ZIO.succeed(paidReservation)
      case true => ZIO.fail("이미 리뷰를 작성한 예약입니다.")
    }

  } yield pendingReservation


//  private def getPaidButNotReviewedReservationsOfUser(user: User) = for {
//    reservations <- ReservationRepository.getReservations()
//    paidReservationsOfUser = reservations
//      .filter(_.user == user)
//      .filter(_.isPaid)
//
//  } yield paidReservationsOfUser

  private def checkIfReservationExistById(targetId: String) = for {
    reservations <- FileManager.readJson[Reservation](
      FileManager.FILE_RESERVATION
    )

    doesExist = reservations.exists(r => r.id == targetId)

  } yield doesExist

  // ---- V 내부 로직 코드 V ----

  private def addReview(review: Review) = for {
    // 결제 한 예약인지 확인하기
    // paidReservation <- getPaidButNotReviewedReservationById(review.reservationId)
    reviews <- ReviewRepository.getReviews()
    _ <- ReviewRepository.saveReviews(reviews.appended(review))
  } yield (review)

  // ---- V 외부 공개 코드 V ----

  def findReservationsByUser(user: User) = for {
    reservations <- ReservationRepository.getReservations()
    result = reservations
      .filter(reservation => user == reservation.user)
    reservation <- result.headOption match {
      case Some(value) => ZIO.succeed(value)
      case None => ZIO.fail("예약을 찾지 못했습니다.")
    }
  } yield reservation

  def makeReservation(
      user: User,
      date: String,
      time: String,
      guestCount: Int
  ) = ReservationRepository.addReservation(ReservationData(user, date, time, guestCount))

  def pay(reservationId: String) = for {
    //저장된 reservation
    reservations <- ReservationRepository.getReservations()
    r <- UserService.getPaidButNotReviewedReservationById(reservationId)
    result = reservations.appended(r)
    _ <- ReservationRepository.saveReservations(result)

  } yield r

  def writeReview(reservationId: String, point: Int, content: String) = addReview(Review(reservationId, point, content))
}
