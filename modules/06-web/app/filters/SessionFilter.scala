package filters.web

import domain._

import services.UserService

import akka.stream.Materializer
import controllers.web.SecurityConstant
import effect.Fail
import javax.inject._
import play.api.Configuration
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc._
import utils.TimeUtils
import web.wiring.ZioRuntime

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

import zio._

/**
 * BlackListCookieFilter is a filter that allow logout to disable session totally
 *
 * This require a link to a storage service so that not decentralized auth anymore but we can't do better right now
 */
@Singleton
class SessionFilter @Inject() (
  val mat:     Materializer,
  ec:          ExecutionContext,
  config:      Configuration,
  zio_runtime: ZioRuntime
) extends Filter
    with Logging {

  private[this] val white_list = List(
    "/",
    "/ping",
    "/login"
  )

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    if(isOnWhiteList(requestHeader.path)) {
      nextFilter(requestHeader)
    } else {
      val effect = extract_user_from_session(requestHeader).map {
        user => nextFilter(add_attributes_to_req(requestHeader, user))
      }.mapError { _ =>
        Future.successful(on_error)
      }.either.map(_.merge)

      zio_runtime.runtime.unsafeRun(effect)
    }
  }

  def isOnWhiteList(path: String): Boolean = {
    white_list.contains(path) || path.startsWith("/assets/")
  }

  val on_error = Redirect(controllers.web.routes.AuthenticationController.login()).withNewSession

  private[this] def extract_user_from_session(requestHeader: RequestHeader): ZIO[Has[UserService], Fail, User] = {
    val maybe_email = requestHeader.session
      .get(SecurityConstant.USER_EMAIL)
      .filter(_ => !has_expired(requestHeader.session))
      .map(email => new Email(email))

    maybe_email
      .map(email => UserService.load(email))
      .getOrElse(ZIO.succeed(None))
      .mapError { fail =>
        logger.error(fail.userMessage())
        fail
      }
      .collect(Fail("not_handled")) {
        case Some(user) => user
      }
  }

  private[this] def add_attributes_to_req(requestHeader: RequestHeader, user: User): RequestHeader = {
    requestHeader.addAttr(SecurityConstant.User, user)
  }

  private[this] val expiration_after_minutes = config.get[Long]("application.session.expire.after_minutes")
  private[this] val expire_check_is_active   = expiration_after_minutes != -1
  private[this] def has_expired(session: Session): Boolean = {
    if(!expire_check_is_active) {
      return false
    }

    session.get("expire_at")
      .flatMap(d => Try(d.toLong).toOption)
      .map(date => OffsetDateTime.ofInstant(Instant.ofEpochSecond(date), ZoneOffset.UTC))
      .map(_.isBefore(TimeUtils.now())) getOrElse (false)
  }
}
