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
  }

  @Exposed
  @Provides
  @Singleton
  def actorSystem: ActorSystem = {
    val system = ActorSystem()
    sys.addShutdownHook(system.shutdown())
    system
  }
}
