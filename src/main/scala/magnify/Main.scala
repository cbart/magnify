package magnify

import magnify.core.Core
import magnify.web.api.routes.Routes
import magnify.web.http.Http

import akka.actor.ActorSystem

object Main extends App {
  implicit val actorSystem = ActorSystem("Magnify")

  class Application(override implicit val actorSystem: ActorSystem)
      extends Core with Routes with Http

  new Application()

  sys.addShutdownHook {
    actorSystem.shutdown()
  }
}