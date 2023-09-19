package model

import zio.json.{DeriveJsonCodec, JsonCodec}

case class User(name: String,
                phone: String)

object User {
  implicit val codec: JsonCodec[User] = DeriveJsonCodec.gen[User]
}
