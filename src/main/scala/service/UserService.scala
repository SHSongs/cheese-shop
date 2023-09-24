package service

import zio._

import file._
import file.FileManager._

import model._
import repository._

object UserService {
  
// Conflict나서 주석처리함 
//   def checkIfReviewHasCreatedById(id : Int) = for{
//       reviews <- getReviews()
//       isExistReview = reviews.exists(r => r.reservation_id == id)
//   } yield isExistReview


//   private def getPaidButNotReviewedReservationById(id : Int) = for{
//     reservations <- getReservations().map(r => r.filter(item => item.id == id))

//     paidReservation <- reservations match {
//       case Nil => ZIO.fail("ID에 해당하는 예약을 찾을 수 없습니다.")
//       case reservation :: Nil if reservation.isPaied == true => ZIO.succeed(reservation)
//       case reservation :: Nil if reservation.isPaied == false => ZIO.fail("결제가 완료되지 않은 예약입니다.")
//       case _ :: _ => ZIO.fail("동일한 ID로 여러 개의 예약이 조회됩니다.")
//     }
//     pendingReservation <- paidReservation match {
//       case pendingReservation if checkIfReviewHasCreatedById(pendingReservation.id) == true => ZIO.succeed(pendingReservation)
//       case pendingReservation if checkIfReviewHasCreatedById(pendingReservation.id) == false => ZIO.fail("이미 리뷰를 작성한 예약입니다.")
//     }


//   } yield pendingReservation


//   private def addReview(review: Review) = for {
//     reviews <- getReviews()
//     reservation <- getPaidButNotReviewedReservationById(review.reservation_id)
//     nextReviews <- reservation match {
//       case true => ZIO.fail(s"이미 존재하는 리뷰가 있습니다.")
//       case false => ZIO.succeed(reviews.appended(review))
//     }
//     _ <- saveReview(nextReviews)
//   } yield ()

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

//   def findReservationsByUser(user: User) = for {
//     // TODO: reservation이 빈 List일 때 에러처리
//     reservations <- getReservations()

  def findReservationsByUser(user: Either[String, User]) = for {
    users <- UserRepository.getUsers()
    reservations <- ReservationRepository.getReservations()
    result = reservations
          .filter(reservation => user == reservation.user)

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

  } yield reservation

//     def pay(reservationId: Int) = for {
//     reservations <- getReservations()
//     _ <- UserRepository.addUser(user)

//     } yield result

  def pay(reservationId: String) = for {
    reservations <- ReservationRepository.getReservations()
    changed = reservations.map { reservation =>
      if (reservation.id == reservationId && reservation.isClosed == true) reservation.copy(isPaied = true)
      else reservation
    }

    _ <- ReservationRepository.saveReservations(changed)

    result <- ZIO.attempt(changed.head)

  } yield result

  // 리뷰는 예약 1건 당 1개만 등록할 수 있다.

  def writeReview(reservationId: String, point: Int, content: String) =
    for {
      _ <- ZIO.unit
      review = Review(reservationId, point, content)
      _ <- addReview(review)
    } yield review
}
