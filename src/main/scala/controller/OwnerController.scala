package controller

import service.OwnerService
import zio._
import zio.http._
import zio.json.EncoderOps

object OwnerController {
  def apply() =
    Http.collectZIO[Request] {
      case Method.GET -> Root / "owner" / "reservations" =>
        for {
          _ <- zio.Console.printLine("/owner/reservations endpoint!")
          reservations <- OwnerService.getReservations()
          res <- ZIO.succeed(Response.json(reservations.toJson))
        } yield res
    }
}
