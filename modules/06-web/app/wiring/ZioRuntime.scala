package web.wiring

import repository._

import services._

import effect.zio.PlatformAppSpecific
import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import play.api.{ Configuration, Logging }

import zio.internal.Platform
import zio.{ Cause, Has, Runtime, ZLayer }

@Singleton
class ZioRuntime @Inject() (
  configuration:    Configuration,
  dbConfigProvider: DatabaseConfigProvider
) {

  lazy val live: ZLayer[Any, Nothing, ZioRuntime.AppContext] =
    PlatformAppSpecific.ZEnv.live andTo
      ZLayer.succeed(configuration) andTo
      ZLayer.succeed(dbConfigProvider) andTo
      UserRepository.in_db andTo
      UserService.live

  lazy val runtime: Runtime[ZioRuntime.AppContext] =
    Runtime.default.unsafeRun(live.toRuntime(ZioRuntime.platform).useNow)
}

object ZioRuntime extends Logging {

  type AppContext =
    PlatformAppSpecific.ZEnv with Has[Configuration] with Has[UserService]

  lazy val platform = Platform.default
    .withReportFailure { cause: Cause[Any] =>
      if(cause.died) {
        logger.error(cause.prettyPrint)
      }
    }
    .withReportFatal { t: Throwable =>
      logger.error("Fatal Error in ZIO", t)
      try {
        java.lang.System.exit(-1)
        throw t
      } catch { case _: Throwable => throw t }
    }
}
