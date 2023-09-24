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
      case Method.PUT -> Root / "owner" / "reservations" / reservationId =>
        for {
          _ <- zio.Console.printLine(
            s"/owner/reservations/${reservationId} endpoint!"
          )
          data <- OwnerService.closeReservation(reservationId)
          res <- ZIO.succeed(Response.json(data.toJson))
        } yield res
      case Method.GET -> Root / "owner" / "reviews" =>
        for {
          _ <- zio.Console.printLine(s"/owner/reviews endpoint!")
          reviews <- OwnerService.getReviews()
          res <- ZIO.succeed(Response.json(reviews.toJson))
        } yield res
    }
}
