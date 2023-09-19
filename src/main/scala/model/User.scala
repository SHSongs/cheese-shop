package model

import zio.json.{DeriveJsonCodec, JsonCodec}

sealed trait User extends Product with Serializable

case class NewUser(name: String, phone: String) extends User
case class ExistingUser(name: String, phone: String) extends User

object User {
  implicit val codec: JsonCodec[User] = DeriveJsonCodec.gen[User]
}

object NewUser {
  implicit val codec: JsonCodec[NewUser] = DeriveJsonCodec.gen[NewUser]
}

object ExistingUser {
  implicit val codec: JsonCodec[ExistingUser] =
    DeriveJsonCodec.gen[ExistingUser]
}
