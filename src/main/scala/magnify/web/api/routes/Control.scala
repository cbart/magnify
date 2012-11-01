package magnify.web.api.routes

import akka.actor.ActorSystem
import akka.util.FiniteDuration
import akka.util.duration._
import spray.routing.Directives._
import spray.routing.{RequestContext, Route}

/**
 * Routes allowing control over the application.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Control (system: ActorSystem) extends (() => Route) {

  override def apply: Route =
    pathPrefix("control") {
      path("stop") { ctx: RequestContext =>
        ctx.complete("Shutting down in 1 second...")
        in(1000.millis) {
          system.shutdown()
        }
      }
    }

  private def in[U](duration: FiniteDuration)(body: => U) {
    system.scheduler.scheduleOnce(duration, new Runnable { def run() { body } })
  }
}
