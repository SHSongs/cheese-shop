import zio._
import zio.config.typesafe.TypesafeConfigProvider

import java.io.IOException

object UserService {
  def run(): ZIO[Any, Exception, Unit] = userProg()
  val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Runtime.setConfigProvider(
    TypesafeConfigProvider.fromResourcePath()
  )
  private def userProg(): ZIO[Any, Exception, Unit] = for {
    _ <- zio.Console.printLine("손님 프로그램입니다.")
    user <- login()
    menu <- inputMenu()
    _ <- menu match {
      case "1" => reservation(user)
      case "2" => pay(user)
      case "3" => writeReview(user)
      case "4" => readReview()
      case _ => zio.Console.printLine("잘못된 입력입니다. 프로그램을 종료합니다.")
    }
  } yield ()

  private def login() = for {
    _ <- ZIO.config[UserConfig](UserConfig.config).flatMap {
      config => Console.printLine(s"${config.name} ${config.phone}")
    }
    name <- zio.Console.readLine("이름: ")
    phone <- zio.Console.readLine("휴대전화번호: ")
  } yield User(name, phone)

  private def inputMenu() = for {
    _ <- zio.Console.printLine("1. 예약")
    _ <- zio.Console.printLine("2. 결제")
    _ <- zio.Console.printLine("3. 리뷰 작성")
    _ <- zio.Console.printLine("4. 리뷰 조회")
    choice <- zio.Console.readLine("입력: ")
  } yield choice

  private def reservation(user: User) = for {
    reservation <- inputReservation(user)
    _ <- saveReservation(reservation)
  } yield ()

  def inputReservation(user: User): ZIO[Any, IOException, Reservation] = for {
    date <- zio.Console.readLine("날짜: ")
    time <- zio.Console.readLine("시간: ")
    guests <- zio.Console.readLine("인원수: ")
  } yield Reservation(0, user, date, time, guests)

  def saveReservation(reservation: Reservation): ZIO[Any, IOException, Unit] = for {
    _ <- zio.Console.printLine("DB에 예약 정보를 저장...")
    _ <- zio.Console.printLine("예약 완료.")
  } yield ()

  def pay(user: User) = for {
    _ <- zio.Console.printLine(s"$user 의 종료된 예약을 DB에서 가져옵니다...")
    choice <- zio.Console.readLine("결제할 예약을 선택하세요: ")
    _ <- zio.Console.printLine(s"DB에서 $user 가 $choice 결제 처리를 합니다...")
    _ <- zio.Console.printLine("결제 완료.")
  } yield ()

  private def writeReview(user: User) = for {
    _ <- zio.Console.printLine("$user 의 결제된 && 리뷰 작성되지 않은 예약을 DB에서 가져옵니다...")
    choice <- zio.Console.readLine("리뷰를 작성할 예약을 선택하세요: ")
    reservation_id = 1  // 예약 아이디
    score <- zio.Console.readLine("별점을 입력해주세요 (0~5): ")
    content <- zio.Console.readLine("리뷰를 남겨주세요: ")
    review = Review(reservation_id, score, content)
    _ <- zio.Console.printLine(s"DB에 $user 가 입력한 $review 를 등록합니다...")
    _ <- zio.Console.printLine("작성 완료.")
  } yield ()

  private def readReview() = for {
    _ <- zio.Console.printLine("전체 리뷰 목록을 가져옵니다...")
  } yield ()

}
