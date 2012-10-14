package magnify.actor

import com.google.inject.{TypeLiteral, Scopes, AbstractModule}
import akka.actor.ActorSystem
import com.google.inject.name.Names

/**
 * Module providing `ActorSystem`.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Actors extends AbstractModule {
  override protected def configure() {
    bind(classOf[ActorSystem])
        .toProvider(classOf[ActorSystemProvider])
        .in(Scopes.SINGLETON)
    bind(new TypeLiteral[(=> Unit) => Unit]() {})
        .annotatedWith(Names.named("shutdown-hooks"))
        .toInstance(body => sys.addShutdownHook(body))
  }
}
