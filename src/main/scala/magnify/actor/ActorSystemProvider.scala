package magnify.actor

import akka.actor.ActorSystem
import com.google.inject.Provider

/**
 *
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[actor] final class ActorSystemProvider extends Provider[ActorSystem] {
  override def get: ActorSystem = {
    val actorSystem = ActorSystem()
    sys.addShutdownHook(actorSystem.shutdown())
    actorSystem
  }
}
