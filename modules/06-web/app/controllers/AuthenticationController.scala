package controllers.web

import domain._

import services.UserService

import controllers.captcha.PlayRecaptchaVerifier
import effect.Fail
import javax.inject._
import play.api.Configuration
import play.api.Logging
import play.api.data.Forms._
import play.api.data._
import play.api.libs.typedmap.TypedKey
import play.api.mvc.Session
import utils.m
import utils.{ StringUtils, TimeUtils }

import scala.concurrent.ExecutionContext

case class AuthenticationCredentials(
  email:    String,
  password: String
)

@Singleton
class AuthenticationController @Inject() (
  config:      Configuration,
  verifier:    PlayRecaptchaVerifier
)(implicit ec: ExecutionContext)
  extends MainController
    with Logging {

  private[this] val loginForm = Form(
    mapping(
      "email"    -> email,
      "password" -> nonEmptyText(minLength = 8)
    )(AuthenticationCredentials.apply)(AuthenticationCredentials.unapply)
  )

  private[this] val captcha_enabled = config.get[Boolean]("recaptcha.enabled")
  private[this] val recaptcha_key   = config.get[String]("recaptcha.publicKey")

  def login = Action { implicit request =>
    Ok(views.html.security.login(loginForm, captcha_enabled, recaptcha_key))
  }

  def authenticate = Action.zio { implicit request =>
    for {
      form      <- verifier.bindFromRequestAndVerify(loginForm)                                  ?| m("error.user.wrong.captcha")
      auth_cred <- form                                                                          ?| { formWithErrors =>
                     Ok(views.html.security.login(formWithErrors, captcha_enabled, recaptcha_key))
                   }
      user      <- user_authenticate(auth_cred.email, auth_cred.password, request.remoteAddress) ?| { err: Fail =>
                     log_fail(err)
                     result2Fail(
                       Redirect(controllers.web.routes.AuthenticationController.login())
                         .flashing("error" -> m(
                           "error.user.wrong.password"
                         ))
                     )
                   }
    } yield {
      Redirect(controllers.web.routes.ApplicationController.app())
        .withSession(
          buildSessionOnLogin(user.email)
        )
    }
  }

  private[this] def user_authenticate(email: String, pwd: String, ip: String) = {
    UserService.authenticate(new Email(email), pwd, ip)
  }

  def logout = Action {
    Redirect(controllers.web.routes.AuthenticationController.login()).withNewSession
  }

  private[controllers] def buildSessionOnLogin(email: Email): Session =
    AuthenticationController.build_session(email, config)
}

object AuthenticationController {

  private[controllers] def build_session(email: Email, config: Configuration): Session = {
    val expiration_after_minutes: Long = config.get[Long]("application.session.expire.after_minutes")
    val data: Map[String, String]      = Map(
      SecurityConstant.USER_EMAIL -> email.underlying,
      "expire_at"                 -> TimeUtils.now().plusMinutes(expiration_after_minutes).toInstant.toEpochMilli.toString,
      "uuid"                      -> StringUtils.generateUuid()
    )
    Session(data)
  }
}

object SecurityConstant {
  val USER_EMAIL: String   = "user_email"
  val authentication_error = "error.login.required"
  val User: TypedKey[User] = TypedKey.apply[User](USER_EMAIL)
}
