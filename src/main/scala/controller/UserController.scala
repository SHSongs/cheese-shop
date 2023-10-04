package controller

import DTO.{RegisterReservationDTO, RegisterReviewDTO}
import zio._
import zio.http._
import zio.json.{DecoderOps, EncoderOps}
import service.UserService
import model._

object UserController {
  def apply() = Http.collectZIO[Request] {
    case req @ (Method.GET -> Root / "user" / "reservations") =>
      for {
        _ <- Console.printLine("/user/reservations endpoint!")
        queryParams = req.url.queryParams

        userData = queryParams.isEmpty match {
          case true  => Left("회원 정보를 입력해주세요.")
          case false => Right(queryParams)
        }

        user <- parseUserData(userData)

        response <- UserService.findReservationsByUser(user)

          .map(r => Response.json(r.toJson))
          .catchAll(e => ZIO.succeed(BadRequestResponse(e)))

      } yield response

    // 손님은 이름, 휴대폰번호, 예약일시, 인원수를 입력하여 치즈식당을 예약할 수 있다.
    case req @ (Method.POST -> Root / "user" / "register" / "review") =>
      for {
        _ <- Console.printLine("/user/register review endpoint!")

        // request body 파싱
        reservation <- req.body.asString.map(_.fromJson[RegisterReservationDTO])

        // reservation 만들기
        result = ZIO
          .fromEither(reservation)
          .flatMap(r =>
            UserService.makeReservation(r.user, r.date, r.time, r.guestCount)
          )

        // TODO: POST 요청에 응답하는 것에 대한 협의 필요
        response <- result
          .map(r => Response.text(s"reservation created: ${r.id}"))
          .catchAll(e => ZIO.succeed(BadRequestResponse(e)))

      } yield response

    // 손님은 자신의 종료된 예약을 결제할 수 있다.
    case req @ (Method.PATCH -> Root / "user" / "pay" / "reservation" / reservationId) =>
      for {
        _ <- Console.printLine("/user/pay reservation endpoint!")
        reservation = UserService.pay(reservationId)

        response <- reservation
          .map(r => Response.text(s"예약이 결제되었습니다.${r.id}"))
          .catchAll(e => ZIO.succeed(BadRequestResponse(s"결제에 실패했습니다. : ${e}")))

      } yield response

    // 손님은 자신의 결제된 예약에 리뷰할 수 있다.

    case req @ (Method.POST -> Root / "user" / "review" / reservationId) =>
      for {
        _ <- Console.printLine("/user/add review endpoint!")

        review <- req.body.asString.map(_.fromJson[RegisterReviewDTO])
        result <- ZIO.fromEither(review)

        response <- UserService
          .writeReview(reservationId, result.point, result.content)
          .map(r => Response.text(s"리뷰를 작성하였습니다.${r.reservationId}"))
          .catchAll(e =>
            ZIO.succeed(BadRequestResponse(s"리뷰 작성에 실패했습니다. : ${e}"))
          )
      } yield response

  }
  private def parseUserData(userData: Either[String, QueryParams]) =
    userData match {
      case Left(error) => ZIO.fail(error)
      case Right(data) =>
        for {
          name <- parseParam(data, "name")
          phone <- parseParam(data, "phone")
        } yield User(name, phone)
    }

  private def parseParam(queryParams: QueryParams, parameter: String) =
    queryParams.get(parameter) match {
      case None => ZIO.fail(s"다음 정보를 입력해주세요: ${parameter}")
      case Some(param) =>
        param.headOption match {
          case None        => ZIO.fail(s"다음 정보를 입력해주세요: ${parameter}")
          case Some(value) => ZIO.succeed(value)
        }
    }

  private def BadRequestResponse(errorMessage: String) =
    Response
      .text(errorMessage)
      .withStatus(Status.BadRequest)
      .addHeader(
        Header.ContentType(MediaType.text.plain, None, Some(Charsets.Utf8))
      )
}
