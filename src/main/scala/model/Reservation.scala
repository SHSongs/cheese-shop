package model

import zio.json.{DeriveJsonCodec, JsonCodec}

case class Reservation(
    id: Int,
    user: User,
    date: String,
    time: String,
    guests: Int,
    isClosed: Boolean = false,
    isPaied: Boolean = false
) {
  def close() = Reservation(
    this.id,
    this.user,
    this.date,
    this.time,
    this.guests,
    true,
    this.isPaied
  )
}

object Reservation {
  implicit val codec: JsonCodec[Reservation] = DeriveJsonCodec.gen[Reservation]
}
