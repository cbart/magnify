package magnify.web.api.routes

import akka.actor.ActorSystem
import scala.concurrent.duration._
import spray.routing.{RequestContext, Directives, Route}

/**
 * Routes allowing control over the application.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Control (system: ActorSystem) extends (() => Route) {
  import Directives._

  override def apply: Route =
    path("control" / "stop") { ctx: RequestContext =>
      ctx.complete("Shutting down in 1 second...")
      in(1000.millis) {
        system.shutdown()
      }
    }

  private def in[U](duration: FiniteDuration)(body: => U) {
    system.scheduler.scheduleOnce(duration, new Runnable { def run() { body } })(system.dispatcher)
  }
}
