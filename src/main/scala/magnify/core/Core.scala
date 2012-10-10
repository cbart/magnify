package magnify.core

import akka.actor.ActorSystem

trait Core {
  implicit val actorSystem: ActorSystem
}
