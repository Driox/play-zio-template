package domain

import pl.iterators.kebs.tagged._
import play.api.libs.json.JsValue
import utils.{ StringUtils, TimeUtils }

import java.time.OffsetDateTime

object UserTag {
  trait UserIdTag
  type UserId = String @@ UserIdTag

  object UserId {
    def apply(arg: String) = from(arg)
    def from(arg:  String) = arg.taggedWith[UserIdTag]
  }
}

case class User(
  id:         UserTag.UserId         = UserTag.UserId(StringUtils.generateUuid()),
  uuid:       String                 = StringUtils.generateUuid(),
  created_at: OffsetDateTime         = TimeUtils.now(),
  deleted_at: Option[OffsetDateTime] = None,
  email:      Email,
  password:   String,
  gender:     Gender                 = Gender.MEN,
  first_name: Option[String]         = None,
  last_name:  Option[String]         = None,
  phone:      Option[String]         = None,
  language:   Option[String]         = None,
  birthday:   Option[OffsetDateTime] = None,
  tag:        List[String]           = List(),
  custom:     Option[JsValue]        = None
)

case class UserSearchCriteria(
  created_after:  Option[OffsetDateTime] = None,
  created_before: Option[OffsetDateTime] = None,
  gender:         Option[Gender]         = None,
  email:          Option[Email]          = None,
  language:       Option[String]         = None
)
