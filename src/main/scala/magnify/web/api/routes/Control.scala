package magnify.web.api.routes

import akka.util.Duration
import akka.util.duration._
import cc.spray._
import com.google.inject.{Provider, Inject}
import akka.actor.ActorSystem

/**
 * Application controlling routes.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Control @Inject() (actorSystem: ActorSystem) extends Provider[Route] {
  val directives = Directives(actorSystem)

  import directives._

  override def get: Route =
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
