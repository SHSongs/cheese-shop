import zio.test._
import zio.http.{Root, _}

object MySuite extends ZIOSpecDefault {
  def spec = suite("MySuite")(
    test("root로 요청을 보내면 Hello World! 를 body로 응답한다") {
      val app = Main.app
      for {
        response <- app.runZIO(Request.get(URL(Root)))
        result <- response.body.asString
      } yield assertTrue(result == "Hello World!")
    }
  )
}
