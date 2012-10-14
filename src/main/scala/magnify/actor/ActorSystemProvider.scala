package magnify.actor

import akka.actor.ActorSystem
import com.google.inject.{Inject, Provider}
import com.google.inject.name.Named

/**
 * Provides new `ActorSystem` instance with shutdown hooks passed to external system.
 *
 *@author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[actor] final class ActorSystemProvider @Inject()
    (@Named("shutdown-hooks") addHook: (=> Unit) => Unit) extends Provider[ActorSystem] {
  override def get: ActorSystem = {
    val actorSystem = ActorSystem()
    addHook(actorSystem.shutdown())
    actorSystem
  }
}
