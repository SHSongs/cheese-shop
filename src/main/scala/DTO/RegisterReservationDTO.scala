package DTO

import model.{User}
import zio.json.{DeriveJsonCodec, JsonCodec}

case class RegisterReservationDTO (
                                    user: User,
                                    date: String,
                                    time: String,
                                    guestCount: Int
                                  )


case class RegisterReviewDTO (point: Int, content: String)

object RegisterReservationDTO {
  implicit val codec: JsonCodec[RegisterReservationDTO] = DeriveJsonCodec.gen[RegisterReservationDTO]
}
object RegisterReviewDTO {
  implicit val codec: JsonCodec[RegisterReviewDTO] = DeriveJsonCodec.gen[RegisterReviewDTO]
}