package model

import zio.json.{DeriveJsonCodec, JsonCodec}

case class Reservation(
                        id: String,
                        user: User,
                        date: String,
                        time: String,
                        guests: Int,
                        isClosed: Boolean = false,
                        isPaid: Boolean = false
) {
  def close() = Reservation(
    this.id,
    this.user,
    this.date,
    this.time,
    this.guests,
    true,
    this.isPaid
  )
}

case class ReservationData(user: User, date: String, time: String, guests: Int)

object Reservation {
  implicit val codec: JsonCodec[Reservation] = DeriveJsonCodec.gen[Reservation]
}
