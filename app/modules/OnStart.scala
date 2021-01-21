package modules

import javax.inject._
import play.api.Configuration
import play.api.Logging
import utils.TimeUtils
import wiring.ZioRuntime

import zio._
import zio.clock._

/**
 * OnStart code is executed in the constructor of this class
 */
@Singleton
class OnStart @Inject() (
  config:      Configuration,
  zio_runtime: ZioRuntime
) extends Logging {
  logger.info("--- App started ---")
  logServerTime()

  private[this] def logServerTime() = zio_runtime.runtime.unsafeRun {
    for {
      time <- currentDateTime
      _    <- ZIO.succeed(logger.info("Server clock : " + TimeUtils.toIso(time)))
    } yield {
      ()
    }
  }
}
