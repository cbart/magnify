package magnify.actor

import akka.actor.ActorSystem
import com.google.inject._

/**
 * Module providing `ActorSystem`.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Actors extends PrivateModule {
  override protected def configure() {
    bind(new TypeLiteral[(=> Unit) => Unit]() {})
        .toInstance(body => sys.addShutdownHook(body))
    bind(classOf[ActorSystemProvider])
        .toConstructor(classOf[ActorSystemProvider].getConstructor(classOf[(=> Unit) => Unit]))
  }

  @Exposed
  @Provides
  @Singleton
  def actorSystem(provider: ActorSystemProvider): ActorSystem = provider()
}
