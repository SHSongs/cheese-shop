import file.FileManager
import zio._

import java.io.IOException

object Main extends ZIOAppDefault {
  val inputType: ZIO[Any, IOException, String] = for {
    _ <- zio.Console.printLine("1. 사장님")
    _ <- zio.Console.printLine("2. 손님")
    _ <- zio.Console.printLine("3. (예제)파일 읽기")
    _ <- zio.Console.printLine("4. (예제)파일 쓰기")
    userType <- zio.Console.readLine("사용자 유형을 선택하세요: ")
  } yield userType

  override def run = for {
    userType <- inputType
    _ <- userType match {
      case "1" => OwnerService.run()
      case "2" => UserService.run()
      case "3" => FileManager.readJson("example", "hello.txt").debug("result => ")
      case "4" => FileManager.writeJson("example", "input.txt", User("익명", "010-1111-2222").toString).debug("result => ")
      case _ => zio.Console.printLine("잘못된 입력입니다. 프로그램을 종료합니다.")
    }
  } yield ()
}
