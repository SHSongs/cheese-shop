package controller

import zio._
import zio.http._

object SampleController {
  def apply() =
    Http.collectZIO[Request] {
      case Method.GET -> Root / "sample" =>
        for {
          _ <- zio.Console.printLine("/sample endpoint!")
          res <- ZIO.succeed(Response.text("안녕하세요"))
        } yield res
    }
}
