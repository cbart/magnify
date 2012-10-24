package magnify.services

import com.google.inject.AbstractModule
import akka.actor.ActorSystem

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Services extends AbstractModule {
  def configure() {
    requireBinding(classOf[ActorSystem])
  }
}
