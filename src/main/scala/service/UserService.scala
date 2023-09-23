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

  private def findReservationById(id : Int) = for{
      reservations <- FileManager.readJson[Reservation](
        FileManager.FILE_RESERVATION
    )
     targetReservation = reservations.filter(r => r.id == id)
  } yield ()


  private def checkIfReservationExistById(targetId : Int) = for{
      reservations <- FileManager.readJson[Reservation](
        FileManager.FILE_RESERVATION
    )

    isExist = reservations.exists(r=> r.id == targetId)
    
  } yield isExist


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
  // 이미 reservation id에 대한 리뷰가 존재하면
    // 에러를 던져준다.
    isExist <- checkIfReservationExistById(review.reservation_id)
    nextReviews <- isExist match {
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

  def findReservationsByUser(user: Either[String, User]) = for {
    users <- getUsers()
    reservations <- getReservations()

    result <- user match {
      case Left(error) => ZIO.left(error)
      case Right(data) =>
        ZIO.right(reservations.filter(reservation => reservation.user == data))
    }
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
      if (reservation.id == reservationId) reservation.copy(isPaied = true)
      else reservation
    }

    _ <- saveReservations(changed)

    result <- ZIO.attempt(changed.head)

  } yield result

  def writeReview(reservationId: Int, point: Int, content: String) =
    for {
      _ <- ZIO.unit
      review = Review(reservationId, point, content)
      _ <- addReview(review)
    } yield review
}
