import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

import java.io.IOException

object Main extends ZIOAppDefault {
  val inputType: ZIO[Any, IOException, String] = for {
    _ <- zio.Console.printLine("1. 사장님")
    _ <- zio.Console.printLine("2. 손님")
    userType <- zio.Console.readLine("사용자 유형을 선택하세요: ")
  } yield userType

  override def run: ZIO[Any with ZIOAppArgs with Scope, Exception, Unit] = for {
    userType <- inputType
    _ <- userType match {
      case "1" => OwnerService.run()
      case "2" => UserService.run()
      case _ => zio.Console.printLine("잘못된 입력입니다. 프로그램을 종료합니다.")
    }
  } yield ()
}
