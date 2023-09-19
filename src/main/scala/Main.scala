import controller.SampleController
import zio._
import zio.http._

object Main extends ZIOAppDefault {

  val app =
    Http.collectZIO[Request] {
      case Method.GET -> Root =>
        for {
          _ <- zio.Console.printLine("/ endpoint!")
          res <- ZIO.succeed(Response.text("Hello World!"))
        } yield res
    }

  val apps = SampleController() ++ app  // 여기에 Controller 추가

  override val run =
    Server
      .serve(apps.withDefaultErrorResponse)
      .provideLayer(Server.defaultWithPort(13333) ++ Client.default)
}
