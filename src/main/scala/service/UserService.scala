package service

import model.{Reservation, Review, User, ExistingUser, NewUser}
import zio._

import java.io.IOException
import file.FileManager

object UserService {
  // 생각보다 하나의 서비스에서 여러 가지 파일을 읽을 일이 많은 것 같습니다
  private def getReservations() = for {
    reservations <- FileManager.readJson[Reservation](
      FileManager.FILE_RESERVATION
    )
  } yield reservations

  private def addReservation(reservation: Reservation) = for {
    reservations <- getReservations()

    nextReservations = reservations.appended(reservation)

    _ <- FileManager.writeJson(FileManager.FILE_RESERVATION, nextReservations)
  } yield ()

  private def saveReservations(reservations: List[Reservation]) = for {
    _ <- FileManager.writeJson(FileManager.FILE_RESERVATION, reservations)
  } yield ()

  private def getExistingUsers() = for {
    users <- FileManager.readJson[ExistingUser](FileManager.FILE_USER)
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

  private def addUser(user: NewUser) = for {
    users <- getExistingUsers()

    nextUsers = users.appended(ExistingUser(user.name, user.phone))

    _ <- FileManager.writeJson(FileManager.FILE_USER, nextUsers)
  } yield ()

  private def getReviews() = for {
    reviews <- FileManager.readJson[Review](FileManager.FILE_REVIEW)
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


  def saveReview(nextReviews: List[Review]) = for {
    _ <- FileManager.writeJson(FileManager.FILE_REVIEW, nextReviews)
  } yield ()



  def login(name: String, phone: String) = for {
    users <- getExistingUsers()

    result = users.contains(ExistingUser(name, phone)) match {
      case true  => ExistingUser(name, phone)
      case false => NewUser(name, phone)
    }

  } yield result

  def makeReservation(
      user: User,
      date: String,
      time: String,
      // 인원수는 정수형이어야 하지 않을지?
      guestCount: String
  ) = for {
    // ID 생성을 어디서 해야 할지?
    uuid <- Random.nextInt
    reservation = Reservation(uuid, user, date, time, guestCount)

    _ <- addReservation(reservation)

    _ <- user match {
      case ExistingUser(name, phone) => ZIO.unit
      case newUser: NewUser          => addUser(newUser)
    }

  } yield reservation

  def getClosedReservationsOfUser(user: User) = for {
    reservations <- getReservations()

    closedReservationsOfUser = reservations
      .filter(_.user == user)
      .filter(_.isClosed)
  } yield closedReservationsOfUser

  def pay(reservationId: Int) = for {
    reservations <- getReservations()

    changed = reservations.map { reservation =>
      if (reservation.id == reservationId) reservation.copy(isPaied = true)
      else reservation
    }

    _ <- saveReservations(changed)

    result <- ZIO.attempt(changed.head)

  } yield result

  def getPaidButNotReviewedReservationsOfUser(user: User) = for {
    reservations <- getReservations()
    paidReservationsOfUser = reservations
      .filter(_.user == user)
      .filter(_.isPaied)

  } yield paidReservationsOfUser

  // point는 정수형이어야 하지 않을지?
  private def writeReview(reservationId: Int, point: String, content: String) =
    for {
      _ <- ZIO.unit
      review = Review(reservationId, point, content)
      _ <- addReview(review)
    } yield review
}
