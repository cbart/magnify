package magnify.core

import akka.actor.{Props, ActorRef, ActorSystem}
import akka.testkit.TestKit

trait Core {
  implicit val actorSystem: ActorSystem

  def projects: ActorRef = actorSystem.actorOf(Props())  // TODO(cbart) fix
}

trait TestCore extends Core {
  this: TestKit =>

  override val actorSystem = system

  override def projects = testActor
}
