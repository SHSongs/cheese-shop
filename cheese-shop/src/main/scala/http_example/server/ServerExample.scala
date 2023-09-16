package http_example.server

import http_example.User
import zio.http._
import zio.json.EncoderOps
import zio.{Random, ZIO, ZIOAppDefault}

object ServerExample extends ZIOAppDefault {
  val app =
    Http.collectZIO[Request] {
      case Method.GET -> Root / "http-test" =>
        for {
          _ <- zio.Console.printLine("/http-test endpoint!")
          phone <- Random.nextIntBetween(1000, 9999)
          user = User("비회원", Integer.toString(phone))
          res <- ZIO.succeed(Response.text(user.toJson))
        } yield res
    }

  override val run =
    Server
      .serve(app.withDefaultErrorResponse)
      .provideLayer(Server.defaultWithPort(13333) ++ Client.default)
}
