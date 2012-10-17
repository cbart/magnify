package magnify.core

import com.google.inject.AbstractModule
import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Names

/**
 *
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Core extends AbstractModule {
  def configure() {
    requireBinding(classOf[ActorSystem])
    bind(classOf[ActorRef])
      .annotatedWith(Names.named("project-graph"))
      .toProvider(classOf[ProjectGraph])
  }
}
