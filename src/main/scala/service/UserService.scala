package service

import zio._

import file._
import file.FileManager._

import model._

object UserService {
  // 생각보다 하나의 서비스에서 여러 가지 파일을 읽을 일이 많은 것 같습니다

  // ---- V 파일 접근 코드 V ----
  private def getReservations() = for {
    reservations <- FileManager.readJson[Reservation](FILE_RESERVATION)
  } yield reservations

  private def addReservation(reservation: Reservation) = for {
    reservations <- getReservations()

    nextReservations = reservations.appended(reservation)

    _ <- FileManager.writeJson(FILE_RESERVATION, nextReservations)
  } yield ()

  private def saveReservations(reservations: List[Reservation]) = for {
    _ <- FileManager.writeJson(FILE_RESERVATION, reservations)
  } yield ()

  private def getUsers() = for {
    users <- FileManager.readJson[User](FILE_USER)
  } yield users

  def checkIfReviewHasCreatedById(id : Int) = for{
      reviews <- getReviews()
      isExistReview = reviews.exists(r => r.reservation_id == id)
  } yield isExistReview


  private def getPaidButNotReviewedReservationById(id : Int) = for{
    reservations <- getReservations().map(r => r.filter(item => item.id == id))

    paidReservation <- reservations match {
      case Nil => ZIO.fail("ID에 해당하는 예약을 찾을 수 없습니다.")
      case reservation :: Nil if reservation.isPaied == true => ZIO.succeed(reservation)
      case reservation :: Nil if reservation.isPaied == false => ZIO.fail("결제가 완료되지 않은 예약입니다.")
      case _ :: _ => ZIO.fail("동일한 ID로 여러 개의 예약이 조회됩니다.")
    }
    pendingReservation <- paidReservation match {
      case pendingReservation if checkIfReviewHasCreatedById(pendingReservation.id) == true => ZIO.succeed(pendingReservation)
      case pendingReservation if checkIfReviewHasCreatedById(pendingReservation.id) == false => ZIO.fail("이미 리뷰를 작성한 예약입니다.")
    }


  } yield pendingReservation


  private def addUser(user: User) = for {
    users <- getUsers()

    nextUsers = users.appended(User(user.name, user.phone))

    _ <- FileManager.writeJson(FILE_USER, nextUsers)
  } yield ()

  private def getReviews() = for {
    reviews <- FileManager.readJson[Review](FILE_REVIEW)
  } yield reviews

  private def addReview(review: Review) = for {
    reviews <- getReviews()
    reservation <- getPaidButNotReviewedReservationById(review.reservation_id)
    nextReviews <- reservation match {
      case true => ZIO.fail(s"이미 존재하는 리뷰가 있습니다.")
      case false => ZIO.succeed(reviews.appended(review))
    }
    _ <- saveReview(nextReviews)
  } yield ()


  private def saveReview(nextReviews: List[Review]) = for {
    _ <- FileManager.writeJson(FileManager.FILE_REVIEW, nextReviews)
  } yield ()


  // ---- V 단순 매핑 코드 V ----


  private def doesUserExists(user: User) = for {
    users <- getUsers()
  } yield users.contains(user)

  private def getClosedReservationsOfUser(user: User) = for {
    reservations <- getReservations()

    closedReservationsOfUser = reservations
      .filter(_.user == user)
      .filter(_.isClosed)
  } yield closedReservationsOfUser

  private def getPaidButNotReviewedReservationsOfUser(user: User) = for {
    reservations <- getReservations()
    paidReservationsOfUser = reservations
      .filter(_.user == user)
      .filter(_.isPaied)

  } yield paidReservationsOfUser

  // ---- V 외부 공개 코드 V ----

  def findReservationsByUser(user: User) = for {
    // TODO: reservation이 빈 List일 때 에러처리
    reservations <- getReservations()

    result = reservations
          .filter(reservation => user == reservation.user)

  } yield result

  // TODO: UUID 생성 관련 코드가 작성되고 나서 수정 예정
  def makeReservation(
      user: User,
      date: String,
      time: String,
      guestCount: Int
  ) = for {
    uuid <- Random.nextInt
    reservation = Reservation(uuid, user, date, time, guestCount)

    _ <- addReservation(reservation)

  } yield reservation
  def pay(reservationId: Int) = for {
    reservations <- getReservations()

    changed = reservations.map { reservation =>
      if (reservation.id == reservationId && reservation.isClosed == true) reservation.copy(isPaied = true)
      else reservation
    }

    _ <- saveReservations(changed)

    result <- ZIO.attempt(changed.head)

  } yield result

  // 리뷰는 예약 1건 당 1개만 등록할 수 있다.
  def writeReview(reservationId: Int, point: Int, content: String) =
    for {
      _ <- ZIO.unit
      review = Review(reservationId, point, content)
      _ <- addReview(review)
    } yield review
}
