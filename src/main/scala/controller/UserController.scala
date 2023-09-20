package controller

import zio._
import zio.http._
import zio.json.EncoderOps

import model.{ExistingUser, NewUser}
import service.UserService

object UserController {
  def apply() = Http.collectZIO[Request] {
    case req @ (Method.GET -> Root / "user") =>
      for {
        _ <- Console.printLine("/user endpoint!")
        noQueryParams = req.url.queryParams.isEmpty

        userResponse <- noQueryParams match {
          case true  => ZIO.succeed(Response.status(Status.BadRequest))
          case false => login(req.url.queryParams)
        }

        result = userResponse.addHeader(
          Header
            .ContentType(MediaType.text.plain, None, Some(Charsets.Utf8))
        )

      } yield result
  }

  private def login(queryParams: QueryParams) = for {
    paramName <- ZIO.fromOption(queryParams.get("name"))
    paramPhone <- ZIO.fromOption(queryParams.get("phone"))

    name = paramName.head
    phone = paramPhone.head

    user <- UserService.login(name, phone)

    result <- user match {
      case ExistingUser(name, _) =>
        ZIO.succeed(Response.text(s"${name}님은 기존 사용자입니다."))
      case NewUser(name, _) =>
        ZIO.succeed(Response.text(s"${name}님은 새로운 사용자입니다."))
    }

  } yield result
}
