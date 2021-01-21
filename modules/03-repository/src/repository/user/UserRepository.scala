package repository

import domain.UserTag.UserId
import domain._

import repository.user.{ UserRepositoryInMemory, UserRepositoryPersistent }

import effect.Fail
import play.api.db.slick.DatabaseConfigProvider

import zio._

trait UserRepository {
  def load(id:    UserId): ZIO[Any, Fail, User]
  def store(user: User): ZIO[Any, Fail, Int]
  def search(
    criteria:     UserSearchCriteria,
    tableSearch:  TableSearch
  ): ZIO[Any, Fail, SearchWithTotalSize[User]]
}

object UserRepository {

  val in_memory: ZLayer[Any, Nothing, Has[UserRepository]] = {
    ZLayer.succeed(new UserRepositoryInMemory())
  }

  val in_db: ZLayer[Has[DatabaseConfigProvider], Nothing, Has[UserRepository]] = {
    ZLayer.fromService[
      DatabaseConfigProvider,
      UserRepository
    ](
      new UserRepositoryPersistent(_)
    )
  }

  def search(criteria: UserSearchCriteria, tableSearch: TableSearch) = {
    ZIO.accessM[Has[UserRepository]](_.get.search(criteria, tableSearch))
  }
}
