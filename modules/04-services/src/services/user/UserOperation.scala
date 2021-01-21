package services.user

import domain._
import _root_.services.UserService
import repository.UserRepository
import zio.ZIO
import effect.Fail
import effect.zio.sorus.ZioSorus

class UserOperation(user_repository: UserRepository) extends UserService with ZioSorus {

  def create(user: User): ZIO[Any, Fail, User] = {
    val new_user = operation.UserOperation.update_password(user, user.password)
    user_repository.store(new_user).map(_ => new_user)
  }

  def search(
    criteria:    UserSearchCriteria,
    tableSearch: TableSearch
  ): ZIO[Any, Fail, SearchWithTotalSize[User]] = user_repository.search(criteria, tableSearch)

  def authenticate(email: Email, password: String, ip: String): ZIO[Any, Fail, User] = {
    for {
      maybe_user <-
        user_repository.search(
          UserSearchCriteria(email = Some(email)),
          TableSearch(limit        = Some(1))
        ) ?| s"Error searching user with email ${email.underlying}"
      user       <- maybe_user.data.headOption     ?| "wrong email or password"
      _          <- check_password(user, password) ?| "wrong email or password"
    } yield {
      user
    }
  }

  private[this] def check_password(user: User, candidate: String): ZIO[Any, Fail, Unit] = {
    operation.UserOperation.check_password(user, candidate) match {
      case true  => ZIO.succeed(())
      case false => ZIO.fail(Fail("wrong password"))
    }
  }

  def load(email: Email): ZIO[Any, Fail, Option[User]] = {
    for {
      maybe_user <-
        user_repository.search(
          UserSearchCriteria(email = Some(email)),
          TableSearch(limit        = Some(1))
        ) ?| s"Error searching user with email ${email.underlying}"
    } yield {
      maybe_user.data.headOption
    }
  }
}
