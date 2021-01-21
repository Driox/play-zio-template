package controllers.api

import domain._

import repository.UserRepository

import javax.inject._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class UserController extends MainController with JsonParser {

  protected val executionSearchForm = Form(
    mapping(
      "created_after"  -> optional(offsetDate),
      "created_before" -> optional(offsetDate),
      "gender"         -> optional(text).transform[Option[Gender]](_.flatMap(Gender.parse), _.map(_.productPrefix)),
      "email"          -> optional(text).transform[Option[Email]](_.map(new Email(_)), _.map(_.underlying)),
      "language"       -> optional(text)
    )(UserSearchCriteria.apply)(UserSearchCriteria.unapply)
  )

  def list() = Action.zio { implicit request =>
    for {
      criteria      <- executionSearchForm.bindFromRequest() ?| ()
      tableCriteria <- tableSearchForm.bindFromRequest()     ?| ()
      user_list     <- UserRepository.search(criteria, tableCriteria)
    } yield {
      Ok(Json.toJson(user_list))
    }
  }
}
