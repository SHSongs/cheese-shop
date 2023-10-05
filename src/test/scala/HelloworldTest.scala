import model.{Reservation, Review}
import zio.http._
import zio.json.DecoderOps
import zio.test._

object HelloworldTest extends ZIOSpecDefault {
  def spec = suite("Helloworld E2E Test")(
    test("root로 요청을 보내면 Hello World! 를 body로 응답한다") {
      for {
        response <- Main.helloApp.runZIO(Request.get(URL(Root)))
        result <- response.body.asString
      } yield assertTrue(result == "Hello World!")
    }
  )
}
