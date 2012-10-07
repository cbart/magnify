package magnify.web.api.routes

import akka.actor.ActorSystem
import akka.util.Duration
import akka.util.duration._
import cc.spray.Directives

final class Control (override implicit val actorSystem: ActorSystem) extends Directives {
  val route = {
    path("control" / "stop") { ctx =>
      ctx.complete("Shutting down in 1 second...")
      in(1000.millis) {
        actorSystem.shutdown()
      }
    }
  }

  def in[U](duration: Duration)(body: => U) {
    actorSystem.scheduler.scheduleOnce(duration, new Runnable { def run() { body } })
  }
}
