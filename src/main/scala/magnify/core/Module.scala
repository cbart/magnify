package magnify.core

import akka.actor.ActorSystem

trait Module {
  implicit val actorSystem: ActorSystem
}
