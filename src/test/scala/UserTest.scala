import DTO.{RegisterReservationDTO, RegisterReviewDTO}
import model.{Reservation, Review, User}
import zio.{Chunk, ZIO}
import zio.http._
import zio.json.{DecoderOps, EncoderOps}
import zio.test._


object UserTest extends ZIOSpecDefault {

  val reservationId = "b44b354b-6811-4432-bb7f-45107d83187a"
  def spec = suite("User에 대한 E2E Test")(
    test("손님은 이름, 휴대폰번호, 예약일시, 인원수를 입력하여 치즈식당을 예약할 수 있다.") {
      for {
        response <- Main.apps.runZIO(Request.post(Body.fromString(RegisterReservationDTO(User("윤채은", "1234"), "1104", "14:00", 2).toJson), URL(Root / "user" / "register" / "review")))
        result <- response.body.asString
      } yield assertTrue(result.startsWith("reservation created:"))
    },
    test("유저는 이름과 전화번호를 입력하여 예약을 확인할 수 있다.") {
      for {
        _ <- ZIO.unit
        user = QueryParams(Map("name"-> Chunk("윤채은"), "phone"-> Chunk("1234")))
        response <- Main.apps.runZIO(Request.get(URL(Root / "user" / "reservations").withQueryParams(user)))
        _ = assertTrue(response.status == Status.Ok).debug
        result <- response.body.asString.map(_.fromJson[Reservation])
      } yield assertTrue(result.isRight)
    },
    test("손님은 자신의 종료된 예약을 결제할 수 있다.") {
      for {
        response <- Main.apps.runZIO(Request.patch(Body.empty, URL(Root / "user" / "pay" / "reservation" / reservationId )))
        result <- response.body.asString
      } yield assertTrue(result.startsWith("예약이 결제되었습니다."))
    },
    test("손님은 자신의 결제된 예약에 리뷰할 수 있다.") {
      for {
        response <- Main.apps.runZIO(Request.post(Body.fromString(RegisterReviewDTO(2, "test").toJson), URL(Root / "user" / "review" / reservationId)))
        result <- response.body.asString
      } yield assertTrue(result.startsWith("리뷰를 작성하였습니다."))
    }

  )
}
