package file

import zio._
import zio.json.{DecoderOps, EncoderOps, JsonDecoder, JsonEncoder}

object FileManager {

  val FILE_RESERVATION = "reservation.json"
  val FILE_REVIEW = "review.json"

  val path = os.pwd / "src" / "resources"

  def readJson[A: JsonDecoder](fileName: String) = for {
    file <- ZIO
      .attempt(ujson.read(os.read(path / fileName)))
      .catchAll(cause => ZIO.fail(s"File Read Fail: ${cause}"))
    list <- ZIO
      .fromEither(file.toString().fromJson[List[A]])
  } yield list

  def writeJson[A: JsonEncoder](fileName: String, contents: List[A]) = for {
    _ <- ZIO.unit
    filePath = path / fileName
    _ <- ZIO
      .attempt(os.write.over(filePath, contents.toJson))
      .catchAll(cause => ZIO.fail(s"Write Fail: ${cause}"))
  } yield ()
}
