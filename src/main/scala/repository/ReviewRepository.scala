package repository

import zio._
import model._
import file.FileManager._

object ReviewRepository {
  def getReviews() = for {
    reviews <- readJson[Review](FILE_REVIEW)
  } yield reviews

  def saveReviews(reviews: List[Review]) = for {
    _ <- writeJson(FILE_REVIEW, reviews)
  } yield ()
}
