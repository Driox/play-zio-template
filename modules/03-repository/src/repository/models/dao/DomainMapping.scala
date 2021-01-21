package repository.models.dao

import domain._

import repository.models.dao.EnhancedPostgresDriver

import pl.iterators.kebs.tagged.slick.SlickSupport
import play.api.Logging
import play.api.db.slick.HasDatabaseConfig

trait DomainMapping extends SlickSupport with JsonParser with Logging {
  self: HasDatabaseConfig[EnhancedPostgresDriver] =>

  import profile.api._

  implicit def emailMapper = MappedColumnType.base[Email, String](email => email.underlying, str => new Email(str))

  implicit def genderMapper = MappedColumnType.base[Gender, String](
    x => x.productPrefix,
    d => Gender.parse(d).getOrElse(Gender.MEN)
  )
}
