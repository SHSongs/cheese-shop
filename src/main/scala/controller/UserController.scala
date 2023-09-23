package controller

import zio._
import zio.http._
import zio.json.EncoderOps

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

        user = parseUserData(userData)

        result <- UserService.findReservationsByUser(user)

        response <- result match {
          case Left(error)  => ZIO.succeed(BadRequestResponse(error))
          case Right(value) => ZIO.succeed(Response.json(value.toJson))
        }

      } yield response
  }

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
}
