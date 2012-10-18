package magnify.web.api.routes

import akka.actor.ActorSystem
import akka.util.Duration
import akka.util.duration._
import cc.spray._

/**
 * Routes allowing control over the application.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Control (system: ActorSystem) extends (() => Route) {
  val directives = Directives(system)

  import directives._

  override def apply: Route =
    path("control" / "stop") { ctx =>
      ctx.complete("Shutting down in 1 second...")
      in(1000.millis) {
        actorSystem.shutdown()
      }
    }

  private def in[U](duration: Duration)(body: => U) {
    actorSystem.scheduler.scheduleOnce(duration, new Runnable { def run() { body } })
  }
}
