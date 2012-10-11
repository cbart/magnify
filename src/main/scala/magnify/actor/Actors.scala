package magnify.actor

import com.google.inject.{Scopes, AbstractModule}
import akka.actor.ActorSystem

/**
 *
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Actors extends AbstractModule {
  override protected def configure() {
    bind(classOf[ActorSystem])
        .toProvider(classOf[ActorSystemProvider])
        .in(Scopes.SINGLETON)
  }
}
