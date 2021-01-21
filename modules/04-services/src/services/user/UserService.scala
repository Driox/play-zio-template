package services

import domain._

import repository.UserRepository

import effect.Fail

import zio._
import services.user.UserOperation

trait UserService {

  def authenticate(email: Email, password: String, ip: String): ZIO[Any, Fail, User]
  def load(email:         Email): ZIO[Any, Fail, Option[User]]
  def create(user:        User): ZIO[Any, Fail, User]

  def search(
    criteria:    UserSearchCriteria,
    tableSearch: TableSearch
  ): ZIO[Any, Fail, SearchWithTotalSize[User]]
}

object UserService {

  val live: ZLayer[
    Has[UserRepository],
    Nothing,
    Has[UserService]
  ] = {
    ZLayer.fromService[
      UserRepository,
      UserService
    ](
      new UserOperation(_)
    )
  }

  def search(
    criteria:    UserSearchCriteria,
    tableSearch: TableSearch
  ): ZIO[Has[UserService], Fail, SearchWithTotalSize[User]] = {
    ZIO.accessM(_.get[UserService].search(criteria, tableSearch))
  }

  def create(user: User): ZIO[Has[UserService], Fail, User] = {
    ZIO.accessM(_.get[UserService].create(user))
  }

  def authenticate(email: Email, password: String, ip: String): ZIO[Has[UserService], Fail, User] = {
    ZIO.accessM(_.get[UserService].authenticate(email, password, ip))
  }

  def load(email: Email): ZIO[Has[UserService], Fail, Option[User]] = {
    ZIO.accessM(_.get[UserService].load(email))
  }
}
