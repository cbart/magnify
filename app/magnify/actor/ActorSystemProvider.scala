package magnify.actor

import akka.actor.ActorSystem

/**
 * Provides new `ActorSystem` instance with shutdown hooks passed to external system.
 *
 *@author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[actor] final class ActorSystemProvider (addHook: (=> Unit) => Unit)
    extends (() => ActorSystem) {
  override def apply: ActorSystem = {
    val actorSystem = ActorSystem()
    addHook(actorSystem.shutdown())
    actorSystem
  }
}
