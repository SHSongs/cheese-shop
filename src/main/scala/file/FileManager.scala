package file

import zio._
import ujson._

object FileManager {
  val path = os.pwd / "src" / "resources"

  def readJson(fileName: String) = for {
    json <- ZIO
      .attempt(ujson.read(os.read(path / fileName)).obj.get("data").get)
      .catchAll(cause => ZIO.fail(s"Read Fail: ${cause}"))
  } yield json

  def writeJson(fileName: String, contents: Value) = for {
    _ <- ZIO.unit
    filePath = path / fileName
    existingContents <- readJson(fileName).orElse(ZIO.succeed(Arr()))
    updatedContents: Arr = existingContents match {
      case arr: Arr => arr.arr :+ contents
      case _ => Arr(contents)
    }
    jsonContents = ujson.Obj("data" -> updatedContents)
    _ <- ZIO
      .attempt(os.write.over(filePath, jsonContents))
      .catchAll(cause => ZIO.fail(s"Write Fail: ${cause}"))
  } yield ()
}
