package model

import zio.json._

case class Review(reservationId: String, point: Int, content: String)

object Review {
  implicit val codec: JsonCodec[Review] = DeriveJsonCodec.gen[Review]
}
