package file

import zio._

object FileManager {
  val path = os.pwd / "src" / "resources"

  def readJson(directoryName: String, fileName: String) = for {
    json <- ZIO
      .attempt(ujson.read(os.read(path / directoryName / fileName)))
      .catchAll(cause => ZIO.fail(s"Read Fail: ${cause}"))
  } yield json

  def writeJson(directoryName: String, fileName: String, contents: String) = for {
    _ <- ZIO
      .attempt(os.write(path / directoryName / fileName, contents))
      .catchAll(cause => ZIO.fail(s"Write Fail: ${cause}"))
  } yield ()
}
