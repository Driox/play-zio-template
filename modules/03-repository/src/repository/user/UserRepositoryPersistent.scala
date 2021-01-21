package repository.user

import domain.UserTag.UserId
import domain._

import repository.UserRepository
import repository.models.dao._

import effect.Fail
import effect.zio.slick.zioslick._
import effect.zio.sorus.ZioSorus
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import zio._

class UserRepositoryPersistent(
  protected val dbConfigProvider: DatabaseConfigProvider
) extends UserRepository
    with HasDatabaseConfigProvider[EnhancedPostgresDriver]
    with UserDao
    with ZioSorus
    with ZioSlick
    with SlickUtils {

  import profile.api._

  val tables        = TableQuery[UserTable]
  lazy val db_layer = SlickDatabase.Live(db)

  def load(id: UserId): ZIO[Any, Fail, User] = {
    val query = tables.filter(_.id === id)
    for {
      maybe_user <- query.result.headOption ?| "Error on db request"
      user       <- maybe_user              ?| s"No user with id $id"
    } yield {
      user
    }
  }

  def store(user: User): ZIO[Any, Fail, Int] = {
    tables += user
  }

  def search(
    criteria:    UserSearchCriteria,
    tableSearch: TableSearch
  ): ZIO[Any, Fail, SearchWithTotalSize[User]] = {
    val q = tables
      .filterOpt(criteria.created_after)(_.created_at.? >= _)
      .filterOpt(criteria.created_before)(_.created_at.? <= _)
      .filterOpt(criteria.gender)(_.gender === _)
      .filterOpt(criteria.language)(_.language === _)
      .filterOpt(tableSearch.global_search.filter(_.nonEmpty))((t, global_search) => {
        (t.email.asColumnOf[String].? ilike s"%${global_search}%") ||
          (t.first_name ilike s"%${global_search}%") ||
          (t.last_name ilike s"%${global_search}%") ||
          (t.phone ilike s"%${global_search}%")
      })
      .sort_it(tableSearch)

    toPaginate(q, tableSearch.offset, tableSearch.limit)
  }
}
