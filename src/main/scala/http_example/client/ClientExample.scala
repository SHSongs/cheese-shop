package http_example.client

import http_example.User
import sttp.client3._
import sttp.client3.ziojson.asJson
import zio.{ZIO, ZIOAppDefault}

object ClientExample extends ZIOAppDefault {
  val prog = for {
    _ <- ZIO.unit
    backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

    response = basicRequest
      .get(uri"http://localhost:13333/http-test")
      .response(asJson[User])
      .send(backend)
    f <- response.body match {
      case Right(user) => ZIO.succeed(user)
      case _ => ZIO.fail(new Exception("fail"))
    }
  } yield f

  override def run = for {
    user <- prog.debug
    _ = println(s"${user.name}(${user.phone})님으로 접속했습니다.")
  } yield ()
}
