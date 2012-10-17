package magnify.services

import com.google.inject.AbstractModule
import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Names
import magnify.services.project.repository.Repository

/**
 *
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Services extends AbstractModule {
  def configure() {
    requireBinding(classOf[ActorSystem])
    bind(classOf[ActorRef])
      .annotatedWith(Names.named("project-repository"))
      .toProvider(classOf[Repository])
  }
}
