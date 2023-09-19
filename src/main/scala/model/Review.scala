package model

import zio.json.{DeriveJsonCodec, JsonCodec}

case class Review(reservation_id: Int, point: String, content: String)

object Review {
  implicit val codec: JsonCodec[Review] = DeriveJsonCodec.gen[Review]
}
