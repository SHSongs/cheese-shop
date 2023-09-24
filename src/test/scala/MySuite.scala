import model.{Reservation, Review}
import zio.test._
import zio.http.{Root, _}
import zio.json.{DecoderOps, EncoderOps, JsonEncoder}

object MySuite extends ZIOSpecDefault {
  def spec = suite("MySuite")(
    test("root로 요청을 보내면 Hello World! 를 body로 응답한다") {
      for {
        response <- Main.helloApp.runZIO(Request.get(URL(Root)))
        result <- response.body.asString
      } yield assertTrue(result == "Hello World!")
    },
    test("사장님은 모든 예약 목록을 확인할 수 있다") {
      for {
        response <- Main.apps.runZIO(Request.get(URL(Root / "owner" / "reservations")))
        result <- response.body.asString.map(_.fromJson[List[Reservation]])
      } yield assertTrue(result.isRight)
    },
    test("사장님은 모든 리뷰 목록을 확인할 수 있다") {
      for {
        response <- Main.apps.runZIO(Request.get(URL(Root / "owner" / "reviews")))
        result <- response.body.asString.map(_.fromJson[List[Review]])
      } yield assertTrue(result.isRight)
    },
    test("사장님은 예약을 종료할 수 있다.") {
      for {
        response <- Main.apps.runZIO(Request.put(Body.empty, URL(Root / "owner" / "reservations" / "1")))
        _ = assertTrue(response.status == Status.Ok)
        result <- response.body.asString.map(_.fromJson[Reservation])
      } yield assertTrue(result.isRight)
    }
  )
}
