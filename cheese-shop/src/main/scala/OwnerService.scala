import zio.ZIO

import java.io.IOException

object OwnerService {
  def run(): ZIO[Any, IOException, Unit] = userProg()

  private def userProg() = for {
    _ <- zio.Console.printLine("사장님 프로그램입니다.")
    menu <- inputMenu()
    _ <- menu match {
      case "1" => readReservation()
      case "2" => close()
      case "3" => readReview()
      case _ => zio.Console.printLine("잘못된 입력입니다. 프로그램을 종료합니다.")
    }
  } yield ()

  private def inputMenu() = for {
    _ <- zio.Console.printLine("1. 예약 조회")
    _ <- zio.Console.printLine("2. 예약 종료")
    _ <- zio.Console.printLine("3. 리뷰 조회")
    choice <- zio.Console.readLine("입력: ")
  } yield choice

  private def readReservation() = for {
    _ <- printReservations()
  } yield ()

  private def close() = for {
    _ <- printReservations()
    _ <- closeReservation()
  } yield ()

  private def readReview() = for {
    _ <- zio.Console.printLine("전체 리뷰 목록을 가져옵니다...")
  } yield ()

  private def printReservations() = for {
    _ <- zio.Console.printLine("DB에서 예약 목록을 불러와서 출력합니다 ..")
  } yield ()

  private def closeReservation() = for {
    target <- zio.Console.readLine("종료할 예약을 선택하세요 : ")
    _ <- zio.Console.printLine("DB에서 예약을 종료 상태로 변경합니다 ..")
    _ <- zio.Console.printLine(s"$target 예약이 종료되었습니다.")
  } yield ()
}
