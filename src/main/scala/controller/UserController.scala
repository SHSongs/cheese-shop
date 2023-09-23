package controller

import DTO.{RegisterReservationDTO, RegisterReviewDTO}
import zio._
import zio.http._
import zio.json.{DecoderOps, EncoderOps}
import service.UserService
import model._

import java.util.UUID

object UserController {
  // - [X] 리뷰는 예약 1건 당 1개만 등록할 수 있다.
  // - [X] 모든 손님은 이름, 휴대폰 번호를 최초 1회 입력한다.
  // - [X] 손님은 이름, 휴대폰번호, 예약일시, 인원수를 입력하여 치즈식당을 예약할 수 있다.
  // - [X] 손님은 자신의 종료된 예약을 결제할 수 있다.
  // - [X] 손님은 자신의 결제된 예약에 리뷰할 수 있다.


  // 회원정보로 예약을 조회하는 기능
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

        result = UserService.findReservationsByUser(user)

        response <- result match {
          case Left(error)  => ZIO.succeed(BadRequestResponse(error))
          case Right(value) => ZIO.succeed(Response.json(value.toJson))
        }

      } yield response
  }

  // User 정보(name, phone)을 request query params에 담아 User로 만드는 함수
  private def parseUserData(userData: Either[String, QueryParams]) = for {
    result <- userData match {
      case Left(error) => Left(error)
      case Right(data) =>
        for {
          name <- parseParam(data, "name")
          phone <- parseParam(data, "phone")
        } yield User(name, phone)
    }
  } yield result

  private def parseParam(queryParams: QueryParams, parameter: String) = for {
    result <- queryParams.get(parameter) match {
      case None => Left(s"다음 정보를 입력해주세요: ${parameter}")
      case Some(param) =>
        param.headOption match {
          case None        => Left(s"다음 정보를 입력해주세요: ${parameter}")
          case Some(value) => Right(value)
        }
    }
  } yield result

  private def BadRequestResponse(errorMessage: String) =
    Response
      .text(errorMessage)
      .withStatus(Status.BadRequest)
      .addHeader(
        Header.ContentType(MediaType.text.plain, None, Some(Charsets.Utf8))
      )


  // 손님은 이름, 휴대폰번호, 예약일시, 인원수를 입력하여 치즈식당을 예약할 수 있다.
  def registerReservation() = Http.collectZIO[Request] {
    case req@(Method.POST -> Root / "user" / "register" / "review") =>
      for {
        _ <- Console.printLine("/user/register review endpoint!")

        // request body 파싱
        reservation <- req.body.asString.map(_.fromJson[RegisterReservationDTO])

        // reservation 만들기
        result = ZIO.fromEither(reservation)
          .flatMap(r =>
            UserService.makeReservation(r.user, r.date, r.time, r.guestCount))

        // TODO: POST 요청에 응답하는 것에 대한 협의 필요
        response <- result.map(r => Response.text(s"reservation created: ${r.id}"))
          .catchAll(e => ZIO.succeed(BadRequestResponse(e)))

      } yield response
  }

  // 손님은 자신의 종료된 예약을 결제할 수 있다.
  def payReservation() = Http.collectZIO[Request] {
    case req@(Method.PATCH -> Root / "user" / "pay" / "reservation" / reservationId) =>
      for {
        _ <- Console.printLine("/user/pay reservation endpoint!")

        id <- ZIO.attempt(reservationId.toInt)
        reservation = UserService.pay(id)

        response <- reservation.map(r => Response.text(s"예약이 결제되었습니다.${r.id}"))
                .catchAll(e => ZIO.succeed(BadRequestResponse(s"결제에 실패했습니다. : ${e}")))

      } yield response
  }

  // 손님은 자신의 결제된 예약에 리뷰할 수 있다.
  def addReview() = Http.collectZIO[Request] {
    case req@(Method.POST -> Root / "user" / "review" / reservationId ) =>
      for {
        _ <- Console.printLine("/user/add review endpoint!")

        id <- ZIO.attempt(reservationId.toInt)
        review <- req.body.asString.map(_.fromJson[RegisterReviewDTO])
        result <- ZIO.fromEither(review)

        reservation = UserService.writeReview(id, result.point, result.content)

        response <- reservation.map(r => Response.text(s"리뷰를 작성하였습니다.${r.reservation_id}"))
          .catchAll(e => ZIO.succeed(BadRequestResponse(s"리뷰 작성에 실패했습니다. : ${e}")))

      } yield response
  }






}
