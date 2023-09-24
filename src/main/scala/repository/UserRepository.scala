package repository

import zio._
import model._
import file.FileManager._

object UserRepository {
  def getUsers() = for {
    users <- readJson[User](FILE_USER)
  } yield users

  def addUser(user: User) = for {
    users <- getUsers()
    nextUsers = users.appended(user)
    _ <- writeJson(FILE_USER, nextUsers)
  } yield ()
}
