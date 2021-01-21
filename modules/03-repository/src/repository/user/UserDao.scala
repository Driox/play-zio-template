package repository.user

import domain.UserTag.UserId
import domain._

import repository.models.dao._

import play.api.db.slick.HasDatabaseConfig

import java.time.OffsetDateTime
import play.api.libs.json.JsValue

trait UserDao extends DomainMapping {
  self: HasDatabaseConfig[EnhancedPostgresDriver] =>

  import profile.api._

  class UserTable(table_tag: Tag) extends Table[User](table_tag, "users") {

    val id: Rep[UserId] = column[UserId]("id", O.PrimaryKey)
    val uuid            = column[String]("uuid")
    val created_at      = column[OffsetDateTime]("created_at")
    val deleted_at      = column[Option[OffsetDateTime]]("deleted_at")
    val email           = column[Email]("email")
    val password        = column[String]("password")
    val gender          = column[Gender]("gender")
    val first_name      = column[Option[String]]("first_name")
    val last_name       = column[Option[String]]("last_name")
    val phone           = column[Option[String]]("phone")
    val language        = column[Option[String]]("language")
    val birthday        = column[Option[OffsetDateTime]]("birthday")
    val tag             = column[List[String]]("tag")
    val custom          = column[Option[JsValue]]("custom")

    def * =
      (
        id,
        uuid,
        created_at,
        deleted_at,
        email,
        password,
        gender,
        first_name,
        last_name,
        phone,
        language,
        birthday,
        tag,
        custom
      ).<>(User.tupled, User.unapply _)
  }

  /** This is usefull for typesafe dynamic sorting */
  implicit val columns: Map[String, UserTable => Rep[_]] = Map(
    "id"         -> { t => t.id },
    "uuid"       -> { t => t.uuid },
    "created_at" -> { t => t.created_at },
    "deleted_at" -> { t => t.deleted_at },
    "email"      -> { t => t.email },
    "password"   -> { t => t.password },
    "gender"     -> { t => t.gender },
    "first_name" -> { t => t.first_name },
    "last_name"  -> { t => t.last_name },
    "phone"      -> { t => t.phone },
    "language"   -> { t => t.language },
    "birthday"   -> { t => t.birthday },
    "tag"        -> { t => t.tag },
    "custom"     -> { t => t.custom }
  )
}
