package magnify

import akka.actor.ActorSystem
import magnify.web.{api, http}

object Main extends App {
  implicit val actorSystem = ActorSystem("Magnify")

  class Application(override implicit val actorSystem: ActorSystem)
      extends core.Module with api.Module with http.Module

  new Application()

  sys.addShutdownHook {
    actorSystem.shutdown()
  }
}