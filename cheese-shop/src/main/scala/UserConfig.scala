import zio.config._
import zio.Config
import zio.config.magnolia.deriveConfig

case class UserConfig(name: String,
                phone: String)

object UserConfig {
  val config: Config[UserConfig] =
    deriveConfig[UserConfig].nested("UserConfig")
}
