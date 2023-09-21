package controller

import zio._
import zio.http._
import zio.json.EncoderOps

import service.UserService
import model._

object UserController {
  def apply() = Http.collectZIO[Request] {
    case req @ (Method.GET -> Root / "user") =>
      for {
        _ <- Console.printLine("/user endpoint!")
        queryParams = req.url.queryParams

        user <- login(queryParams)

        response <- user match {
          case Left(error) =>
            ZIO.succeed(BadRequestResponse(error))
          case Right(value) =>
            ZIO.succeed(value match {
              case ExistingUser(name, _) => Response.json(value.toJson)
              case NewUser(name, _)      => Response.json(value.toJson)
            })
        }

      } yield response

    // case req @ (Method.GET -> Root / "user" / "reservations") =>
    //   for {
    //     _ <- Console.printLine("/user/reservations endpoint!")
    //     queryParams = req.url.queryParams

    //     user <- login(queryParams)

    //     response <- user match {
    //       case Left(error) => ZIO.succeed(BadRequestResponse(error))
    //       case Right(value) =>
    //         for {
    //           findResult <- UserService.findReservationsByUser(value)

    //           result <- findResult match {
    //             case Left(error) => ZIO.succeed(BadRequestResponse(error))
    //             case Right(reservations) =>
    //               ZIO.succeed(Response.json(reservations.toJson))
    //           }
    //         } yield result
    //     }

    //   } yield response
  }

  private def parseUserData(queryParams: Either[String, QueryParams]) = for {
    result <- ZIO.attempt(
      queryParams match {
        case Left(error) => Left(error)
        case Right(params) =>
          for {
            name <- params.get("name") match {
              case None => Left("이름을 입력해주세요.")
              case Some(nameParam) =>
                nameParam.headOption match {
                  case None        => Left("이름을 입력해주세요.")
                  case Some(value) => Right(value)
                }
            }
            phone <- params.get("phone") match {
              case None => Left("전화번호 뒷자리를 입력해주세요.")
              case Some(nameParam) =>
                nameParam.headOption match {
                  case None        => Left("전화번호 뒷자리를 입력해주세요.")
                  case Some(value) => Right(value)
                }
            }
          } yield (name, phone)
      }
    )
  } yield result

  private def login(queryParams: QueryParams) = for {
    nonEmptyParams <- queryParams.isEmpty match {
      case true  => ZIO.left("사용자 정보를 입력해주세요.")
      case false => ZIO.right(queryParams)
    }

    userData <- parseUserData(nonEmptyParams)

    user <- UserService.login(userData)

  } yield user

  private def BadRequestResponse(errorMessage: String) =
    Response
      .text(errorMessage)
      .withStatus(Status.BadRequest)
      .addHeader(
        Header.ContentType(MediaType.text.plain, None, Some(Charsets.Utf8))
      )
}
