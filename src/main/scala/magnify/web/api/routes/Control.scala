package magnify.web.api.routes

import magnify.core.Core

import akka.util.Duration
import akka.util.duration._
import cc.spray.Directives

trait Control {
  this: Core =>

  private[routes] final class ControlDirectives extends Directives {
    override def actorSystem = Control.this.actorSystem

    val route = {
      path("control" / "stop") { ctx =>
        ctx.complete("Shutting down in 1 second...")
        in(1000.millis) {
          actorSystem.shutdown()
        }
      }
    }

    private def in[U](duration: Duration)(body: => U) {
      actorSystem.scheduler.scheduleOnce(duration, new Runnable { def run() { body } })
    }
  }

  private[routes] val controlDirectives = new ControlDirectives
}
