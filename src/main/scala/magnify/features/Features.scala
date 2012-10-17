package magnify.features

import magnify.features.project.graph.ProjectGraph

import com.google.inject.{Key, AbstractModule}
import com.google.inject.name.Names
import akka.actor.{ActorRef, ActorSystem}

/**
 * Module grouping implementations of Magnify features.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Features extends AbstractModule {
  def configure() {
    requireBinding(classOf[ActorSystem])
    requireBinding(Key.get(classOf[ActorRef], Names.named("project-repository")))
    bind(classOf[ActorRef])
      .annotatedWith(Names.named("project-graph"))
      .toProvider(classOf[ProjectGraph])
  }
}
