package magnify.features

import akka.actor.ActorSystem
import com.google.inject.AbstractModule

/**
 * Module grouping implementations of Magnify features.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Features extends AbstractModule {
  def configure() {
    requireBinding(classOf[ActorSystem])
  }
}
