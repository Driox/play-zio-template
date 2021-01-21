package repository.user

import domain.UserTag.UserId
import domain._

import repository.UserRepository

import effect.Fail
import effect.zio.sorus.ZioSorus

import scala.collection.mutable

import zio._

class UserRepositoryInMemory extends UserRepository with ZioSorus {
  private[this] var in_memory_repository: mutable.Map[UserId, User] = mutable.Map()

  def load(id: UserId): ZIO[Any, Fail, User] = {
    in_memory_repository.get(id) ?| "error.user.not_found"
  }

  def store(user: User): ZIO[Any, Fail, Int] = {
    ZIO.succeed(in_memory_repository.put(user.id, user)).map(_ => 1)
  }

  def search(
    criteria:    UserSearchCriteria,
    tableSearch: TableSearch
  ): ZIO[Any, Fail, SearchWithTotalSize[User]] = {
    val result = SearchWithTotalSize(
      total_size = in_memory_repository.values.toList.length,
      data       = in_memory_repository.values.toList
    )

    ZIO.succeed(result)
  }
}
