package controllers.web

import javax.inject._

@Singleton
class ApplicationController @Inject() () extends MainController {

  def index() = Action {
    Redirect(controllers.web.routes.ApplicationController.app())
  }

  def app() = Action {
    Ok(views.html.app())
  }
}
